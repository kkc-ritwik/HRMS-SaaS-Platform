-- Richer directory view backing DEI analytics (service-reports) and engagement heatmaps
-- (service-engagement). Adds employee_id alias, department/location/manager names and hire_date.
DROP VIEW IF EXISTS employee_directory_view;
CREATE VIEW employee_directory_view AS
SELECT
    e.id,
    e.id            AS employee_id,
    e.tenant_id,
    e.gender,
    e.nationality,
    e.employment_status,
    e.exit_date,
    e.join_date     AS hire_date,
    e.department_id,
    d.name          AS department_name,
    e.location_id,
    l.name          AS location_name,
    e.manager_id,
    NULLIF(TRIM(COALESCE(m.first_name,'') || ' ' || COALESCE(m.last_name,'')), '') AS manager_name,
    NULL::varchar   AS ethnicity,
    NULL::varchar   AS disability_status,
    NULL::integer   AS grade_level,
    NULL::numeric   AS annual_ctc,
    CASE
        WHEN e.date_of_birth IS NULL                    THEN 'Unknown'
        WHEN EXTRACT(YEAR FROM e.date_of_birth) >= 1997 THEN 'Gen Z'
        WHEN EXTRACT(YEAR FROM e.date_of_birth) >= 1981 THEN 'Millennial'
        WHEN EXTRACT(YEAR FROM e.date_of_birth) >= 1965 THEN 'Gen X'
        ELSE 'Boomer'
    END AS generation
FROM employees e
LEFT JOIN departments d ON d.id = e.department_id AND d.tenant_id = e.tenant_id
LEFT JOIN locations   l ON l.id = e.location_id   AND l.tenant_id = e.tenant_id
LEFT JOIN employees   m ON m.id = e.manager_id
WHERE e.is_deleted = false;
