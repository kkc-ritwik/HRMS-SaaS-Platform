# HRMS — Disaster Recovery Plan

## Recovery objectives

| Metric | Target |
|--------|--------|
| RTO (full platform restore) | **2 hours** from declaration of disaster |
| RPO (data loss tolerance)   | **15 minutes** (Postgres WAL shipping) / **24 hours** (object storage) |
| Annual DR drill cadence     | Quarterly (full restore from cold backup) |

## Backup matrix

| Data store      | Frequency      | Retention | Storage                              | Restore tool |
|-----------------|----------------|-----------|--------------------------------------|--------------|
| Postgres (DDL+data) | Daily `pg_dump`+ continuous WAL | 30 days (logical) + 7 days (WAL) | S3 `s3://hrms-backups/postgres/` | `pg_restore` |
| MinIO objects   | Nightly mirror | 90 days (versioned) | S3 `s3://hrms-backups/minio/` | `mc mirror` |
| Kafka topics    | N/A (replayable via outbox + S3 archive) | 7 days in-cluster | Cluster only | Outbox replay |
| Redis cache     | None (cache-only data, rebuildable) | N/A | N/A | N/A |
| Elasticsearch   | Snapshot to S3 nightly | 14 days | S3 `s3://hrms-backups/es/` | `_snapshot/restore` |
| Configmaps/Secrets | Git + external-secrets | Indefinite (git) | GitHub + AWS Secrets Manager | `kubectl apply` + `kubectl rollout` |
| Helm chart      | Git tags | Indefinite | GitHub | `helm install` |

## Scenarios

### S1 — Single Postgres primary failure (most common)
- Streaming replica is automatically promoted by Patroni / Bitnami chart `repmgr`.
- RTO: < 60 seconds (failover). RPO: 0 (sync replication enabled).
- Action: monitor `pg_is_in_recovery()` on new primary; reset broken old primary.

### S2 — Postgres cluster lost (rare)
- Restore from latest base backup + WAL replay:
```bash
aws s3 cp s3://hrms-backups/postgres/hrms-LATEST.sql.gz /tmp/
gunzip /tmp/hrms-LATEST.sql.gz
pg_restore -h <new-primary> -U postgres -d hrms_db /tmp/hrms-LATEST.sql
# Then replay WAL archive up to disaster timestamp
```
- RTO: ~90 minutes for 100 GB DB. RPO: depends on WAL ship lag (target ≤ 5 min).

### S3 — Object storage corruption / loss
- MinIO is distributed (4 nodes); single-node loss heals automatically (`mc admin heal`).
- Full cluster loss: restore from S3 mirror:
```bash
mc alias set src s3://hrms-backups/minio ...
mc alias set dst http://minio-new:9000 ...
mc mirror src/ dst/
```
- RTO: dependent on object count (~1 TB takes ~3 hours).

### S4 — Entire K8s cluster lost
1. Stand up replacement cluster (Terraform: `cd infra/eks && terraform apply`).
2. Install cert-manager, external-secrets-operator, kube-prometheus-stack, ingress-nginx, istio.
3. `helm upgrade --install hrms ./deploy/helm/hrms --values values-prod.yaml --atomic --wait`.
4. Wait for `hrms-secrets` to populate from Secrets Manager.
5. Restore Postgres (S2 path), MinIO (S3 path).
6. Re-point DNS once `curl https://...health` returns UP.
- RTO: 2 hours end-to-end. RPO: as per backups.

### S5 — Region-wide AWS outage
- Cross-region replication enabled for S3 backup bucket (eu-west-1 ← us-east-1).
- Standby K8s cluster in secondary region kept warm with autoscaler min=0.
- Process: re-execute S4 against secondary region, restore from cross-region S3.
- RTO: 4 hours. RPO: 30 minutes (S3 replication lag).

## Runbook drills (quarterly)

| Quarter | Drill | Owner |
|---------|-------|-------|
| Q1 | Postgres replica failover | DBA |
| Q2 | MinIO bucket restore from S3 | Platform |
| Q3 | Cross-region failover (S5) | SRE lead |
| Q4 | Full cluster rebuild from cold (S4) | Whole team |

## Communications

- **War-room channel**: `#incident-hrms` in Slack
- **Status page**: `https://status.hrms.yourcompany.com` (Statuspage.io)
- **Customer notification**: HR/CS team triggers via Statuspage at SEV-1
- **Post-mortem**: Within 5 business days, document in `/docs/postmortems/YYYY-MM-DD-summary.md`
