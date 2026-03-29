-- ─────────────────────────────────────────────────────────────────────────────
-- V2  ·  Seed standard Indian salary components + statutory configs
--
-- Uses tenant_id = '00000000-0000-0000-0000-000000000001' as the system/demo
-- tenant. In production, replicate these rows for each provisioned tenant.
-- ─────────────────────────────────────────────────────────────────────────────

DO $$
DECLARE
    v_tenant  VARCHAR := '00000000-0000-0000-0000-000000000001';

    -- Component UUIDs (stable so idempotent re-runs are safe)
    c_basic   UUID := 'aaaaaaaa-0001-0000-0000-000000000001';
    c_hra     UUID := 'aaaaaaaa-0001-0000-0000-000000000002';
    c_spl     UUID := 'aaaaaaaa-0001-0000-0000-000000000003';
    c_conv    UUID := 'aaaaaaaa-0001-0000-0000-000000000004';
    c_med     UUID := 'aaaaaaaa-0001-0000-0000-000000000005';
    c_lta     UUID := 'aaaaaaaa-0001-0000-0000-000000000006';
    c_da      UUID := 'aaaaaaaa-0001-0000-0000-000000000007';
    c_bonus   UUID := 'aaaaaaaa-0001-0000-0000-000000000008';
    c_pf_emp  UUID := 'aaaaaaaa-0001-0000-0000-000000000009';
    c_pf_er   UUID := 'aaaaaaaa-0001-0000-0000-000000000010';
    c_esi_emp UUID := 'aaaaaaaa-0001-0000-0000-000000000011';
    c_esi_er  UUID := 'aaaaaaaa-0001-0000-0000-000000000012';
BEGIN

-- ── Salary Components ─────────────────────────────────────────────────────────

INSERT INTO salary_components
    (id, tenant_id, name, code, type, calculation_type,
     is_taxable, is_part_of_ctc, is_part_of_gross, is_pro_rata, display_order)
VALUES
    -- 1. Basic Salary — fixed, taxable, part of gross
    (c_basic, v_tenant, 'Basic Salary', 'BASIC', 'EARNING', 'FIXED',
     true, true, true, true, 10),

    -- 3. Special Allowance — earning, fixed, taxable (inserted before HRA so HRA can reference BASIC)
    (c_spl, v_tenant, 'Special Allowance', 'SPL_ALLOW', 'EARNING', 'FIXED',
     true, true, true, true, 30),

    -- 4. Conveyance Allowance — fixed, tax-exempt up to ₹1,600/month
    (c_conv, v_tenant, 'Conveyance Allowance', 'CONV_ALLOW', 'EARNING', 'FIXED',
     false, true, true, true, 40),

    -- 5. Medical Allowance — fixed, tax-exempt up to ₹1,250/month
    (c_med, v_tenant, 'Medical Allowance', 'MED_ALLOW', 'EARNING', 'FIXED',
     false, true, true, true, 50),

    -- 6. LTA — reimbursement, conditionally exempt under Section 10(5)
    (c_lta, v_tenant, 'Leave Travel Allowance', 'LTA', 'REIMBURSEMENT', 'FIXED',
     false, true, false, false, 60),

    -- 8. Statutory Bonus — 8.33% of Basic, capped at ₹7,000/month per Payment of Bonus Act
    (c_bonus, v_tenant, 'Statutory Bonus', 'STAT_BONUS', 'EARNING', 'FIXED',
     true, true, false, true, 80)
ON CONFLICT (id) DO NOTHING;

-- 2. HRA — 50% of Basic (references c_basic)
INSERT INTO salary_components
    (id, tenant_id, name, code, type, calculation_type,
     percentage_of_component_id, percentage_value,
     is_taxable, is_part_of_ctc, is_part_of_gross, is_pro_rata, display_order)
VALUES
    (c_hra, v_tenant, 'House Rent Allowance', 'HRA', 'EARNING', 'PERCENTAGE',
     c_basic, 50.0000, true, true, true, true, 20)
ON CONFLICT (id) DO NOTHING;

-- 7. DA — 5% of Basic (references c_basic)
INSERT INTO salary_components
    (id, tenant_id, name, code, type, calculation_type,
     percentage_of_component_id, percentage_value,
     is_taxable, is_part_of_ctc, is_part_of_gross, is_pro_rata, display_order)
VALUES
    (c_da, v_tenant, 'Dearness Allowance', 'DA', 'EARNING', 'PERCENTAGE',
     c_basic, 5.0000, true, true, true, true, 70)
ON CONFLICT (id) DO NOTHING;

-- 9. PF Employee — 12% of Basic (deduction)
INSERT INTO salary_components
    (id, tenant_id, name, code, type, calculation_type,
     percentage_of_component_id, percentage_value,
     is_taxable, is_part_of_ctc, is_part_of_gross, is_pro_rata, display_order)
VALUES
    (c_pf_emp, v_tenant, 'PF Employee Contribution', 'PF_EMP', 'DEDUCTION', 'PERCENTAGE',
     c_basic, 12.0000, false, false, false, true, 90)
ON CONFLICT (id) DO NOTHING;

-- 10. PF Employer — 12% of Basic (employer contribution, part of CTC)
INSERT INTO salary_components
    (id, tenant_id, name, code, type, calculation_type,
     percentage_of_component_id, percentage_value,
     is_taxable, is_part_of_ctc, is_part_of_gross, is_pro_rata, display_order)
VALUES
    (c_pf_er, v_tenant, 'PF Employer Contribution', 'PF_ER', 'EMPLOYER_CONTRIBUTION', 'PERCENTAGE',
     c_basic, 12.0000, false, true, false, true, 100)
ON CONFLICT (id) DO NOTHING;

-- 11. ESI Employee — 0.75% of Gross (deduction, applicable if gross <= 21000)
INSERT INTO salary_components
    (id, tenant_id, name, code, type, calculation_type,
     percentage_value,
     is_taxable, is_part_of_ctc, is_part_of_gross, is_pro_rata, display_order)
VALUES
    (c_esi_emp, v_tenant, 'ESI Employee Contribution', 'ESI_EMP', 'DEDUCTION', 'PERCENTAGE',
     0.7500, false, false, false, true, 110)
ON CONFLICT (id) DO NOTHING;

-- 12. ESI Employer — 3.25% of Gross (employer contribution)
INSERT INTO salary_components
    (id, tenant_id, name, code, type, calculation_type,
     percentage_value,
     is_taxable, is_part_of_ctc, is_part_of_gross, is_pro_rata, display_order)
VALUES
    (c_esi_er, v_tenant, 'ESI Employer Contribution', 'ESI_ER', 'EMPLOYER_CONTRIBUTION', 'PERCENTAGE',
     3.2500, false, true, false, true, 120)
ON CONFLICT (id) DO NOTHING;


-- ── Default PF Config ──────────────────────────────────────────────────────────
INSERT INTO pf_config
    (id, tenant_id, basic_wage_ceiling, pf_rate_employee, pf_rate_employer,
     eps_rate, edli_rate, admin_charge_rate, include_employer_pf_in_ctc, effective_from)
VALUES
    ('bbbbbbbb-0001-0000-0000-000000000001', v_tenant,
     15000, 12, 12, 8.33, 0.5, 0.5, true, '2024-04-01')
ON CONFLICT (id) DO NOTHING;


-- ── Default ESI Config ─────────────────────────────────────────────────────────
INSERT INTO esi_config
    (id, tenant_id, wage_ceiling, employee_rate, employer_rate, effective_from)
VALUES
    ('bbbbbbbb-0002-0000-0000-000000000001', v_tenant,
     21000, 0.75, 3.25, '2024-04-01')
ON CONFLICT (id) DO NOTHING;


-- ── Professional Tax Slabs — Karnataka ────────────────────────────────────────
-- Karnataka: monthly salary → PT per month
-- Note: Karnataka slabs are gender-neutral; women earning < ₹25,000 are exempt from 2023 onwards
INSERT INTO pt_slabs
    (id, tenant_id, state, slab_from, slab_to, monthly_tax, gender, effective_from)
VALUES
    ('cccccccc-0001-0000-0000-000000000001', v_tenant, 'KARNATAKA',      0,  9999, 0,   NULL, '2023-04-01'),
    ('cccccccc-0001-0000-0000-000000000002', v_tenant, 'KARNATAKA',  10000, 14999, 150, NULL, '2023-04-01'),
    ('cccccccc-0001-0000-0000-000000000003', v_tenant, 'KARNATAKA',  15000,  NULL, 200, 'M',  '2023-04-01'),
    -- Women earning 15000+ are exempt from PT in Karnataka (Notification 2023)
    ('cccccccc-0001-0000-0000-000000000004', v_tenant, 'KARNATAKA',  15000,  NULL, 0,   'F',  '2023-04-01')
ON CONFLICT (id) DO NOTHING;


-- ── Professional Tax Slabs — Maharashtra ──────────────────────────────────────
-- Maharashtra: monthly salary → PT per month (gender-based for some slabs)
INSERT INTO pt_slabs
    (id, tenant_id, state, slab_from, slab_to, monthly_tax, gender, effective_from)
VALUES
    -- All genders: 0–7500 → ₹0
    ('cccccccc-0002-0000-0000-000000000001', v_tenant, 'MAHARASHTRA',    0,  7500,   0, NULL, '2023-04-01'),
    -- Male: 7501–10000 → ₹175
    ('cccccccc-0002-0000-0000-000000000002', v_tenant, 'MAHARASHTRA', 7501, 10000, 175, 'M',  '2023-04-01'),
    -- Female: 7501–10000 → ₹0 (women earning < ₹25000 exempt)
    ('cccccccc-0002-0000-0000-000000000003', v_tenant, 'MAHARASHTRA', 7501, 24999,   0, 'F',  '2023-04-01'),
    -- Male: 10001–25000 → ₹300 (but ₹200/month + ₹300 in February — modelled as ₹200 average)
    ('cccccccc-0002-0000-0000-000000000004', v_tenant, 'MAHARASHTRA',10001, 25000, 200, 'M',  '2023-04-01'),
    -- Male: 25001+ → ₹200
    ('cccccccc-0002-0000-0000-000000000005', v_tenant, 'MAHARASHTRA',25001,  NULL, 200, 'M',  '2023-04-01'),
    -- Female: 25000+ → ₹200
    ('cccccccc-0002-0000-0000-000000000006', v_tenant, 'MAHARASHTRA',25000,  NULL, 200, 'F',  '2023-04-01')
ON CONFLICT (id) DO NOTHING;

END $$;
