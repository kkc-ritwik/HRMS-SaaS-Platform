# HRMS Platform — Production Runbook

For on-call engineers. Pair this with your alerting (Grafana) and tracing (Jaeger) UIs.

## Severity ladder

| Sev | What | Response time | Examples |
|---|---|---|---|
| SEV-1 | Multi-tenant data loss, payroll-day outage | < 15 min | Postgres primary down, Kafka cluster lost |
| SEV-2 | Single critical service down, payroll partially blocked | < 30 min | `service-payroll` crashing, MinIO unreachable |
| SEV-3 | Non-critical service down, async features paused | < 4 hours | `service-social` down, `service-helpdesk` down |
| SEV-4 | Cosmetic / single-tenant impact | < 1 business day | UI glitch, one tenant's notifications missing |

## Common incidents

### "Payroll run stuck in PROCESSING"
1. Check `service-payroll` logs: `kubectl logs -l app=service-payroll --tail=200`.
2. Verify Postgres has no long-running locks on `payslips` or `payroll_runs`.
3. Confirm `outbox_events` is draining (Grafana: outbox lag panel).
4. If a single payslip is stuck, mark it `FAILED` and re-run; do not delete.

### "OTP emails not arriving"
1. Confirm `MAIL_ENABLED=true` and SMTP creds in the running pod's env.
2. Check `service-auth` logs for `Mail send attempt … failed`.
3. Try a manual SMTP from the pod: `nc -vz smtp.host port`.
4. Look at MailHog (staging) / SMTP-provider dashboard (prod) for blocked sends.

### "Webhook deliveries stuck"
1. Look at `service-integrations` `webhook_deliveries` table for `status='PENDING'` with `attempts >= retry_max` → bumped to `ABANDONED`.
2. Reset by clearing `attempts` if the target endpoint is now reachable.
3. Check Kafka consumer lag — `kafka-consumer-groups --describe --group service-integrations`.

### "Kafka outbox not draining"
1. Likely consumer error or schema mismatch — check `service-integrations` logs.
2. DLQ topic: `<topic>.dlq` — inspect with `kafka-console-consumer`.
3. As a last resort, mark `outbox_events.sent_at = NOW()` for poison records after capturing the payload to a ticket.

### "Tenant data leakage suspected"
1. **Page security immediately.** SEV-1.
2. Query `audit_logs` for the offending entity → identify actor + IP.
3. Confirm `TenantContext.get()` returned the right value at the time (correlation ID → request log).
4. Rotate affected tenant's JWT secret + force re-login.

## Routine ops

- **Quarterly:** rotate `JWT_SECRET`. Roll restart all services with overlap-grace.
- **Monthly:** prune `audit_logs` older than 7 years (compliance window varies).
- **Weekly:** verify backup restore from cold storage — run on a staging DB.
- **Daily:** check Grafana for: outbox lag &lt; 1000, p95 latency &lt; 500ms, error rate &lt; 1%.

## Backups

- **Postgres**: `pg_dump` daily → S3 versioned bucket. Retention 90 days.
- **MinIO**: bucket replication to a cross-region target.
- **Kafka**: not backed up (transient). Outbox is the durable source.

## Disaster recovery

Recovery Time Objective (RTO): 4 hours. Recovery Point Objective (RPO): 24 hours.

1. Spin up new infra via Helm: `helm install hrms ./deploy/helm/hrms -n hrms`.
2. Restore Postgres from latest backup: `pg_restore -d hrms_db backup.dump`.
3. Restore MinIO bucket from replicated bucket.
4. Restart services in this order: discovery → config → all business services.
5. Replay outbox for the gap window (manual op — see `scripts/replay-outbox.sh`).
