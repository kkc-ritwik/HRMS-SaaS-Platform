-- Seed a "default" dashboard per tenant with common HR KPIs. Tenants can clone + customize.
-- Idempotent — only seeds if no default dashboard exists for the tenant.

-- A "system" tenant_id 'default' lets new tenants inherit out of the box.
INSERT INTO dashboards (id, tenant_id, name, description, is_default, is_public, created_at, updated_at, deleted)
VALUES (gen_random_uuid(), 'default', 'HRMS Default Dashboard', 'Out-of-the-box workforce KPIs', true, true, NOW(), NOW(), false)
ON CONFLICT DO NOTHING;

WITH dash AS (
    SELECT id FROM dashboards WHERE tenant_id = 'default' AND name = 'HRMS Default Dashboard' LIMIT 1
)
INSERT INTO dashboard_widgets (id, tenant_id, dashboard_id, widget_type, title, position, config, created_at, updated_at, deleted)
SELECT gen_random_uuid(), 'default', dash.id, w.widget_type, w.title, w.position, w.config::jsonb,
       NOW(), NOW(), false
FROM dash,
(VALUES
    ('METRIC', 'Total Active Employees', '{"x":0,"y":0,"w":3,"h":2}',
     '{"query":"SELECT COUNT(*) FROM employees WHERE deleted=false AND employment_status=''ACTIVE''"}'),
    ('METRIC', 'New Hires This Month',   '{"x":3,"y":0,"w":3,"h":2}',
     '{"query":"SELECT COUNT(*) FROM employees WHERE join_date >= date_trunc(''month'',CURRENT_DATE)"}'),
    ('METRIC', 'Exits This Month',       '{"x":6,"y":0,"w":3,"h":2}',
     '{"query":"SELECT COUNT(*) FROM employees WHERE exit_date >= date_trunc(''month'',CURRENT_DATE)"}'),
    ('METRIC', 'Headcount Change %',     '{"x":9,"y":0,"w":3,"h":2}',
     '{"query":"WITH last_month AS (SELECT COUNT(*) c FROM employees WHERE join_date < date_trunc(''month'',CURRENT_DATE) AND (exit_date IS NULL OR exit_date >= date_trunc(''month'',CURRENT_DATE))), now AS (SELECT COUNT(*) c FROM employees WHERE deleted=false AND employment_status=''ACTIVE'') SELECT ROUND((now.c::numeric - last_month.c) * 100.0 / NULLIF(last_month.c,0), 2) FROM now, last_month"}'),
    ('CHART',  'Headcount by Department','{"x":0,"y":2,"w":6,"h":4}',
     '{"chart":"bar","query":"SELECT d.name as label, COUNT(e.id) as value FROM employees e JOIN departments d ON e.department_id=d.id WHERE e.deleted=false AND e.employment_status=''ACTIVE'' GROUP BY d.name ORDER BY value DESC"}'),
    ('CHART',  'Headcount by Location',  '{"x":6,"y":2,"w":6,"h":4}',
     '{"chart":"pie","query":"SELECT l.name as label, COUNT(e.id) as value FROM employees e JOIN locations l ON e.location_id=l.id WHERE e.deleted=false AND e.employment_status=''ACTIVE'' GROUP BY l.name"}'),
    ('CHART',  'Attrition % Trend (12m)', '{"x":0,"y":6,"w":12,"h":4}',
     '{"chart":"line","query":"SELECT to_char(date_trunc(''month'',exit_date),''YYYY-MM'') as label, COUNT(*) as value FROM employees WHERE exit_date >= NOW() - INTERVAL ''12 months'' GROUP BY 1 ORDER BY 1"}'),
    ('TABLE',  'Top 10 Pending Approvals','{"x":0,"y":10,"w":6,"h":4}',
     '{"query":"SELECT entity_type as type, COUNT(*) as count FROM workflow_instances WHERE status IN (''PENDING'',''IN_PROGRESS'') GROUP BY entity_type ORDER BY count DESC LIMIT 10"}'),
    ('TABLE',  'Recent Onboardings (10)', '{"x":6,"y":10,"w":6,"h":4}',
     '{"query":"SELECT first_name || '' '' || last_name as name, join_date FROM employees WHERE join_date >= NOW() - INTERVAL ''30 days'' ORDER BY join_date DESC LIMIT 10"}')
) AS w(widget_type, title, position, config)
ON CONFLICT DO NOTHING;
