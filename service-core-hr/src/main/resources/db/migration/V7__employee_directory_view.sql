-- View backing the DEI / diversity analytics in service-reports.
-- Maps available employee columns; supplies derived "generation" and NULLs for
-- attributes not yet captured on the employee record (ethnicity, disability, grade, ctc).
CREATE OR REPLACE VIEW employee_directory_view AS
SELECT
    e.id,
    e.tenant_id,
    e.gender,
    e.nationality,
    e.employment_status,
    e.exit_date,
    NULL::varchar  AS ethnicity,
    NULL::varchar  AS disability_status,
    NULL::integer  AS grade_level,
    NULL::numeric  AS annual_ctc,
    CASE
        WHEN e.date_of_birth IS NULL                          THEN 'Unknown'
        WHEN EXTRACT(YEAR FROM e.date_of_birth) >= 1997       THEN 'Gen Z'
        WHEN EXTRACT(YEAR FROM e.date_of_birth) >= 1981       THEN 'Millennial'
        WHEN EXTRACT(YEAR FROM e.date_of_birth) >= 1965       THEN 'Gen X'
        ELSE 'Boomer'
    END AS generation
FROM employees e
WHERE e.is_deleted = false;
