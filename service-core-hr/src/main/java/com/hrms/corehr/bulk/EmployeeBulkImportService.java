package com.hrms.corehr.bulk;

import com.hrms.corehr.entity.Employee;
import com.hrms.corehr.entity.Employee.EmploymentStatus;
import com.hrms.corehr.entity.Employee.EmploymentType;
import com.hrms.corehr.repository.EmployeeRepository;
import com.hrms.security.model.TenantContext;
import com.opencsv.CSVReader;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a CSV file of employees and creates / updates records in bulk.
 * Required columns: employee_code, first_name, last_name, email, join_date.
 * Optional columns: middle_name, work_email, phone, date_of_birth, pan_number,
 * aadhar_number, uan_number, esi_number, bank_account_number, ifsc_code, bank_name,
 * employment_type (default FULL_TIME), employment_status (default ACTIVE),
 * gender, marital_status, nationality, blood_group, display_name, cost_center_code.
 *
 * Rows that fail validation are reported in the result; partial success is committed
 * (each row in its own transaction would multiply roundtrips — we batch in chunks of 100).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeBulkImportService {

    private static final int BATCH = 100;
    private final EmployeeRepository employees;

    @Transactional
    public ImportResult importCsv(InputStream csvIn) {
        ImportResult result = ImportResult.builder()
                .errors(new ArrayList<>()).build();
        try (CSVReader reader = new CSVReader(new InputStreamReader(csvIn, StandardCharsets.UTF_8))) {
            String[] header = reader.readNext();
            if (header == null) return result;
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < header.length; i++) idx.put(header[i].trim().toLowerCase(), i);

            ensureColumn(idx, "employee_code");
            ensureColumn(idx, "first_name");
            ensureColumn(idx, "last_name");
            ensureColumn(idx, "email");
            ensureColumn(idx, "join_date");

            List<Employee> batch = new ArrayList<>();
            int lineNo = 1;
            String[] row;
            while ((row = reader.readNext()) != null) {
                lineNo++;
                try {
                    Employee e = toEmployee(row, idx);
                    batch.add(e);
                    if (batch.size() >= BATCH) {
                        employees.saveAll(batch);
                        result.imported += batch.size();
                        batch.clear();
                    }
                } catch (Exception ex) {
                    result.errors.add(new RowError(lineNo, ex.getMessage()));
                    result.failed++;
                }
            }
            if (!batch.isEmpty()) {
                employees.saveAll(batch);
                result.imported += batch.size();
            }
            log.info("Bulk import done: imported={} failed={}", result.imported, result.failed);
        } catch (Exception e) {
            log.error("Bulk import I/O failure: {}", e.getMessage(), e);
            result.errors.add(new RowError(0, "I/O failure: " + e.getMessage()));
        }
        return result;
    }

    private Employee toEmployee(String[] row, Map<String, Integer> idx) {
        Employee e = new Employee();
        e.setTenantId(TenantContext.get());
        e.setEmployeeCode(req(row, idx, "employee_code"));
        e.setFirstName(req(row, idx, "first_name"));
        e.setLastName(req(row, idx, "last_name"));
        e.setEmail(req(row, idx, "email"));
        e.setJoinDate(LocalDate.parse(req(row, idx, "join_date")));

        opt(row, idx, "middle_name", e::setMiddleName);
        opt(row, idx, "work_email", e::setWorkEmail);
        opt(row, idx, "phone", e::setPhone);
        opt(row, idx, "date_of_birth", v -> e.setDateOfBirth(LocalDate.parse(v)));
        opt(row, idx, "pan_number", e::setPanNumber);
        opt(row, idx, "aadhar_number", e::setAadharNumber);
        opt(row, idx, "uan_number", e::setUanNumber);
        opt(row, idx, "esi_number", e::setEsiNumber);
        opt(row, idx, "bank_account_number", e::setBankAccountNumber);
        opt(row, idx, "ifsc_code", e::setIfscCode);
        opt(row, idx, "bank_name", e::setBankName);
        opt(row, idx, "nationality", e::setNationality);
        opt(row, idx, "blood_group", e::setBloodGroup);
        opt(row, idx, "display_name", e::setDisplayName);
        opt(row, idx, "employment_type", v -> e.setEmploymentType(EmploymentType.valueOf(v.toUpperCase())));
        opt(row, idx, "employment_status", v -> e.setEmploymentStatus(EmploymentStatus.valueOf(v.toUpperCase())));

        if (e.getEmploymentType() == null) e.setEmploymentType(EmploymentType.FULL_TIME);
        if (e.getEmploymentStatus() == null) e.setEmploymentStatus(EmploymentStatus.ACTIVE);
        return e;
    }

    private void ensureColumn(Map<String, Integer> idx, String col) {
        if (!idx.containsKey(col)) throw new IllegalArgumentException("Required column missing: " + col);
    }
    private String req(String[] row, Map<String, Integer> idx, String col) {
        Integer i = idx.get(col);
        if (i == null || i >= row.length) throw new IllegalArgumentException("Missing required " + col);
        String v = row[i].trim();
        if (v.isEmpty()) throw new IllegalArgumentException("Empty required " + col);
        return v;
    }
    private void opt(String[] row, Map<String, Integer> idx, String col, java.util.function.Consumer<String> setter) {
        Integer i = idx.get(col);
        if (i == null || i >= row.length) return;
        String v = row[i].trim();
        if (!v.isEmpty()) setter.accept(v);
    }

    @Data @Builder
    public static class ImportResult {
        @Builder.Default private int imported = 0;
        @Builder.Default private int failed = 0;
        private List<RowError> errors;
    }
    public record RowError(int line, String message) {}
}
