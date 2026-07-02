-- Seed a "default" dashboard per tenant with common HR KPIs. Tenants can clone + customize.
-- A "system" tenant_id 'default' lets new tenants inherit out of the box.
-- Guarded with NOT EXISTS so it is safe to re-run.

INSERT INTO dashboards (id, tenant_id, name, description, owner_id, default_dashboard, shared, created_at, updated_at, is_deleted)
SELECT gen_random_uuid(), 'default', 'HRMS Default Dashboard', 'Out-of-the-box workforce KPIs',
       '00000000-0000-0000-0000-000000000000', true, true, NOW(), NOW(), false
WHERE NOT EXISTS (
    SELECT 1 FROM dashboards WHERE tenant_id = 'default' AND name = 'HRMS Default Dashboard'
);

WITH dash AS (
    SELECT id FROM dashboards WHERE tenant_id = 'default' AND name = 'HRMS Default Dashboard' LIMIT 1
)
INSERT INTO dashboard_widgets (id, tenant_id, dashboard_id, widget_type, title, query_config,
                               position_x, position_y, width, height, created_at, updated_at, is_deleted)
SELECT gen_random_uuid(), 'default', dash.id, w.widget_type, w.title, w.query_config::jsonb,
       w.position_x, w.position_y, w.width, w.height, NOW(), NOW(), false
FROM dash,
(VALUES
    ('KPI',   'Total Active Employees', 0, 0, 3, 2,
     '{"query":"SELECT COUNT(*) FROM employees WHERE deleted=false AND employment_status=''ACTIVE''"}'),
    ('KPI',   'New Hires This Month',   3, 0, 3, 2,
     '{"query":"SELECT COUNT(*) FROM employees WHERE join_date >= date_trunc(''month'',CURRENT_DATE)"}'),
    ('KPI',   'Exits This Month',       6, 0, 3, 2,
     '{"query":"SELECT COUNT(*) FROM employees WHERE exit_date >= date_trunc(''month'',CURRENT_DATE)"}'),
    ('KPI',   'Headcount Change %',     9, 0, 3, 2,
     '{"query":"WITH last_month AS (SELECT COUNT(*) c FROM employees WHERE join_date < date_trunc(''month'',CURRENT_DATE) AND (exit_date IS NULL OR exit_date >= date_trunc(''month'',CURRENT_DATE))), now AS (SELECT COUNT(*) c FROM employees WHERE deleted=false AND employment_status=''ACTIVE'') SELECT ROUND((now.c::numeric - last_month.c) * 100.0 / NULLIF(last_month.c,0), 2) FROM now, last_month"}'),
    ('CHART', 'Headcount by Department', 0, 2, 6, 4,
     '{"chart":"bar","query":"SELECT d.name as label, COUNT(e.id) as value FROM employees e JOIN departments d ON e.department_id=d.id WHERE e.deleted=false AND e.employment_status=''ACTIVE'' GROUP BY d.name ORDER BY value DESC"}'),
    ('CHART', 'Headcount by Location',  6, 2, 6, 4,
     '{"chart":"pie","query":"SELECT l.name as label, COUNT(e.id) as value FROM employees e JOIN locations l ON e.location_id=l.id WHERE e.deleted=false AND e.employment_status=''ACTIVE'' GROUP BY l.name"}'),
    ('CHART', 'Attrition % Trend (12m)', 0, 6, 12, 4,
     '{"chart":"line","query":"SELECT to_char(date_trunc(''month'',exit_date),''YYYY-MM'') as label, COUNT(*) as value FROM employees WHERE exit_date >= NOW() - INTERVAL ''12 months'' GROUP BY 1 ORDER BY 1"}'),
    ('TABLE', 'Top 10 Pending Approvals', 0, 10, 6, 4,
     '{"query":"SELECT entity_type as type, COUNT(*) as count FROM workflow_instances WHERE status IN (''PENDING'',''IN_PROGRESS'') GROUP BY entity_type ORDER BY count DESC LIMIT 10"}'),
    ('TABLE', 'Recent Onboardings (10)', 6, 10, 6, 4,
     '{"query":"SELECT first_name || '' '' || last_name as name, join_date FROM employees WHERE join_date >= NOW() - INTERVAL ''30 days'' ORDER BY join_date DESC LIMIT 10"}')
) AS w(widget_type, title, position_x, position_y, width, height, query_config)
WHERE NOT EXISTS (
    SELECT 1 FROM dashboard_widgets dw JOIN dash ON dw.dashboard_id = dash.id
);
