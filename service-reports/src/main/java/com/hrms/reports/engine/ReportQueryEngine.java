package com.hrms.reports.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes whitelisted, parameterized SQL queries (named report definitions) against the
 * read replica. NEVER accepts raw SQL from the API — only the report definition's stored
 * query is run, with bind parameters resolved from filter inputs. This guards against
 * SQL injection while still allowing fully custom analytics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportQueryEngine {

    private final JdbcTemplate jdbc;

    public ResultSet run(String sql, Map<String, Object> bindings, int maxRows) {
        if (sql == null || sql.isBlank()) return ResultSet.empty();
        if (containsForbidden(sql)) {
            throw new IllegalArgumentException("Report query contains forbidden DDL/DML keywords");
        }

        // Convert :named bindings to ? for JdbcTemplate
        List<Object> args = new java.util.ArrayList<>();
        String sqlOut = sql;
        for (var e : bindings.entrySet()) {
            String token = ":" + e.getKey();
            while (sqlOut.contains(token)) {
                sqlOut = sqlOut.replaceFirst(java.util.regex.Pattern.quote(token), "?");
                args.add(e.getValue());
            }
        }
        String capped = sqlOut + " LIMIT " + Math.min(Math.max(maxRows, 1), 50_000);

        List<Map<String, Object>> rows = jdbc.queryForList(capped, args.toArray());
        List<String> columns = rows.isEmpty() ? List.of() : List.copyOf(rows.get(0).keySet());
        return new ResultSet(columns, rows);
    }

    private boolean containsForbidden(String sql) {
        String upper = sql.toUpperCase();
        for (String kw : new String[]{" INSERT ", " UPDATE ", " DELETE ", " DROP ",
                " ALTER ", " TRUNCATE ", " GRANT ", " REVOKE ", " CREATE ", "; --"}) {
            if (upper.contains(kw)) return true;
        }
        return false;
    }

    public record ResultSet(List<String> columns, List<Map<String, Object>> rows) {
        public static ResultSet empty() { return new ResultSet(List.of(), List.of()); }
        public Map<String, Object> toMap() {
            Map<String, Object> m = new HashMap<>();
            m.put("columns", columns); m.put("rows", rows); m.put("totalRows", rows.size());
            return m;
        }
    }
}
