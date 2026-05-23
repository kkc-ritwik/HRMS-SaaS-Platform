# HRMS — Deployment Guide

Three supported deployment paths, in increasing order of operational complexity:

1. **Local dev**          — `docker compose up` against the host's Spring Boot processes
2. **Single-host prod**   — `docker compose -f docker-compose.prod.yml up -d` (one VM, all images from GHCR)
3. **Kubernetes / Helm**  — full HA stack with Postgres replication, Redis Sentinel, Kafka 5-broker, MinIO 4-node, Istio mTLS, Prometheus Operator

---

## 0. Prerequisites

| Tool       | Min version | Notes |
|------------|-------------|-------|
| Java       | 17          | Eclipse Temurin recommended |
| Maven      | 3.9         | for local builds |
| Docker     | 24+         | with Compose v2 |
| kubectl    | 1.28+       | for K8s deploys |
| helm       | 3.14+       | for K8s deploys |
| openssl    | 3+          | to generate JWT/PII keys |
| awscli     | 2+          | for S3 backups |

Generate runtime secrets once:
```bash
openssl rand -base64 64           # JWT_SECRET
openssl rand -base64 32           # PII_ENCRYPTION_KEY
openssl rand -base64 24           # DB_PASSWORD
```

---

## 1. Local development

```bash
cp .env.example .env             # defaults work for local
docker compose up -d              # postgres, redis, kafka, minio, mailhog, prometheus, grafana, jaeger, loki
mvn -B -DskipTests=true install   # 46 modules in ~90s
scripts/start.bat                 # launches all services with profile=dev
```

Smoke test: `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`.
Browse Swagger: `http://localhost:8080/swagger-ui.html` (gateway aggregates each service's docs).

---

## 2. Single-host production (Docker Compose)

For a single VPS / staging-on-VM scenario (≤ 200 concurrent users).

### a. Build & push images (one-time, from CI)
GitHub Actions `release.yml` builds 30 images on `v*` tags. To build locally:
```bash
for s in $(ls -d service-* api-gateway config-server | sed 's|/||g'); do
  docker build --build-arg SERVICE=$s -t ghcr.io/your-org/$s:1.0.0 .
done
docker push ghcr.io/your-org/*:1.0.0
```

### b. Configure environment
```bash
cp .env.example .env
nano .env             # fill DB_PASSWORD, JWT_SECRET, SMTP creds, etc.
```

### c. Place TLS certs
```bash
mkdir -p deploy/nginx/certs
# Copy fullchain.pem and privkey.pem from Let's Encrypt / your CA
```

### d. Launch
```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d
docker compose -f docker-compose.prod.yml logs -f api-gateway
```

### e. Verify
```bash
curl -k https://hrms.yourcompany.com/actuator/health
# Open Grafana on http://<host>:3001  (admin / .env GRAFANA_ADMIN_PASSWORD)
# Open Jaeger on  http://<host>:16686
```

---

## 3. Kubernetes (Helm)

### a. Cluster prerequisites
- Kubernetes 1.28+ with at least 3 worker nodes (8 vCPU / 32 GB each for prod)
- Storage class with dynamic provisioning (`gp3` on EKS, `pd-ssd` on GKE, `managed-premium` on AKS)
- Ingress: `nginx-ingress` or `istio-ingressgateway`
- Cert-manager with a `letsencrypt-prod` ClusterIssuer
- For prod: install `kube-prometheus-stack`, `loki-stack`, `external-secrets-operator`, and Istio 1.20+

### b. Bootstrap namespace + secrets

```bash
kubectl create ns hrms
kubectl create ns data         # postgres/redis/kafka/minio live here
kubectl create ns monitoring   # prometheus/grafana

# Option A: in-cluster bootstrap (DEV only)
helm upgrade --install hrms ./deploy/helm/hrms \
  --namespace hrms \
  --values ./deploy/helm/hrms/values.yaml \
  --values ./deploy/helm/hrms/values-dev.yaml \
  --set bootstrapSecrets.enabled=true \
  --set bootstrapSecrets.dbPassword="$DB_PASSWORD" \
  --set bootstrapSecrets.jwtSecret="$JWT_SECRET"

# Option B: externalSecrets (PROD) — populate AWS Secrets Manager first
# Keys: hrms/prod/db, hrms/prod/jwt, hrms/prod/storage, hrms/prod/smtp, ...
helm upgrade --install hrms ./deploy/helm/hrms \
  --namespace hrms \
  --values ./deploy/helm/hrms/values.yaml \
  --values ./deploy/helm/hrms/values-prod.yaml \
  --set externalSecrets.enabled=true \
  --atomic --wait --timeout 20m
```

### c. Verify

```bash
kubectl get pods -n hrms
kubectl logs -n hrms -l app=service-discovery --tail=50
kubectl port-forward -n hrms svc/api-gateway 8080:8080
curl http://localhost:8080/actuator/health
```

### d. Day-2 operations

| Task | Command |
|---|---|
| Roll a single service | `kubectl rollout restart -n hrms deploy/service-payroll` |
| Scale manually | `kubectl scale -n hrms deploy/service-core-hr --replicas=10` |
| Tail logs | `kubectl logs -n hrms -l app=service-auth --tail=200 -f` |
| Run DB migration job | `helm test hrms -n hrms` |
| Trigger backup now | `kubectl create job --from=cronjob/postgres-backup manual-backup-$(date +%s) -n hrms` |
| Upgrade image tag | `helm upgrade hrms ./deploy/helm/hrms -n hrms --reuse-values --set global.imageTag=1.2.0` |
| Rollback | `helm rollback hrms 1 -n hrms` |

---

## 4. Observability

| URL                                  | Purpose |
|--------------------------------------|---------|
| /actuator/prometheus (per service)   | Metrics scrape endpoint |
| Grafana                              | Pre-built dashboards: hrms-overview, hrms-jvm, hrms-http, hrms-kafka |
| Jaeger / Tempo                       | Distributed traces (OTLP) |
| Loki / Kibana                        | Logs (JSON-structured in prod) |
| AlertManager                         | Routes alerts → Slack/PagerDuty (see PrometheusRule) |

---

## 5. Rolling upgrade (zero-downtime)

1. Tag release: `git tag v1.2.0 && git push --tags` — triggers image build.
2. PR to bump `values-prod.yaml` `global.imageTag: 1.2.0`.
3. CI runs `helm upgrade --atomic` — pods rolled one at a time per `maxUnavailable: 0`.
4. Health-check gates the rollout: failed readiness probe → rollback.
5. Verify by tailing `kubectl rollout status -n hrms deploy/api-gateway`.

If anything breaks: `helm rollback hrms <previous-revision> -n hrms --wait`.

---

## 6. Production checklist

Before going live:

- [ ] All `.env` secrets generated with `openssl rand`
- [ ] `JWT_SECRET` rotated (different per environment)
- [ ] `PII_ENCRYPTION_KEY` backed up in offline secure storage
- [ ] TLS certificates installed (cert-manager OR manual)
- [ ] CSP/HSTS verified at the edge (`curl -I https://hrms.yourcompany.com`)
- [ ] Postgres backup cronjob scheduled and tested (run once manually, verify S3 object exists)
- [ ] Restore drill performed (see DR.md)
- [ ] Prometheus alerts wired to Slack / PagerDuty / Opsgenie
- [ ] Grafana dashboards imported and pinned
- [ ] OAuth/SAML/SCIM federation tested end-to-end
- [ ] Smoke-test script in CI (`/api/v1/auth/login → 200`)
- [ ] DR runbook reviewed by on-call team
- [ ] On-call rotation set up in PagerDuty
