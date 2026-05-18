package com.hrms.audit.retention;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Nightly retention purge. Soft-deletes rows older than the per-table retention window
 * defined in {@link RetentionPolicy}. Soft-delete preferred over DELETE so daily backup
 * still has them for the retention period of the backup, but live queries no longer return them.
 *
 * Disable per-service by setting hrms.retention.enabled=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hrms.retention.enabled", havingValue = "true", matchIfMissing = true)
public class RetentionScheduler {

    private final RetentionPolicy policy;
    private final ObjectProvider<JdbcTemplate> jdbcProvider;

    @Scheduled(cron = "${hrms.retention.cron:0 30 2 * * *}")
    public void runDailyPurge() {
        JdbcTemplate jdbc = jdbcProvider.getIfAvailable();
        if (jdbc == null) {
            log.warn("Retention scheduler: no JdbcTemplate bean — skipping");
            return;
        }
        for (var entry : policy.getTableDays().entrySet()) {
            String table = entry.getKey();
            int days = entry.getValue();
            try {
                if (!tableExists(jdbc, table)) continue;
                String column = pickTimestampColumn(jdbc, table);
                if (column == null) {
                    log.debug("No timestamp column for {}, skipping", table);
                    continue;
                }
                OffsetDateTime cutoff = OffsetDateTime.now().minusDays(days);
                boolean hasDeleted = columnExists(jdbc, table, "deleted");
                int affected;
                // Postgres-compatible batched DML using CTE
                if (hasDeleted) {
                    affected = jdbc.update(
                            "WITH victims AS (SELECT id FROM " + table +
                                    " WHERE " + column + " < ? AND deleted = false LIMIT " + policy.getBatchSize() + ") " +
                                    "UPDATE " + table + " t SET deleted = true FROM victims v WHERE t.id = v.id",
                            cutoff);
                } else {
                    affected = jdbc.update(
                            "WITH victims AS (SELECT id FROM " + table +
                                    " WHERE " + column + " < ? LIMIT " + policy.getBatchSize() + ") " +
                                    "DELETE FROM " + table + " t USING victims v WHERE t.id = v.id",
                            cutoff);
                }
                if (affected > 0) {
                    log.info("Retention purge: {} → {} rows older than {} days", table, affected, days);
                }
            } catch (Exception e) {
                log.warn("Retention purge failed for {}: {}", table, e.getMessage());
            }
        }
    }

    private boolean tableExists(JdbcTemplate jdbc, String table) {
        Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = ?",
                Integer.class, table);
        return count != null && count > 0;
    }

    private boolean columnExists(JdbcTemplate jdbc, String table, String column) {
        Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                Integer.class, table, column);
        return count != null && count > 0;
    }

    /** Pick the best timestamp column to age rows out by. */
    private String pickTimestampColumn(JdbcTemplate jdbc, String table) {
        for (String c : new String[]{"created_at", "occurred_at", "sent_at", "delivered_at", "punched_at"}) {
            if (columnExists(jdbc, table, c)) return c;
        }
        return null;
    }
}
