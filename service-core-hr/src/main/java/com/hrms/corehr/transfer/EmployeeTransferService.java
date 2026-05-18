package com.hrms.corehr.transfer;

import com.hrms.corehr.entity.Employee;
import com.hrms.corehr.repository.EmployeeRepository;
import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeTransferService {

    public interface TransferRepo extends JpaRepository<EmployeeTransfer, UUID> {}

    private final TransferRepo transfers;
    private final EmployeeRepository employees;
    private final EventPublisher events;

    @Transactional
    public EmployeeTransfer create(EmployeeTransfer t) {
        t.setTenantId(TenantContext.get());
        if (t.getEffectiveDate() == null) t.setEffectiveDate(LocalDate.now());
        EmployeeTransfer saved = transfers.save(t);
        events.publish(Topics.EMPLOYEE, DomainEvent.of(
                "employee.transfer.created", "core-hr",
                saved.getTenantId(), saved.getId().toString(), "EmployeeTransfer",
                Map.of("employeeId", t.getEmployeeId(), "type", t.getType())));
        return saved;
    }

    @Transactional
    public EmployeeTransfer approve(UUID transferId, UUID approverId) {
        EmployeeTransfer t = transfers.findById(transferId).orElseThrow();
        t.setStatus(EmployeeTransfer.Status.APPROVED);
        t.setApprovedBy(approverId);
        return transfers.save(t);
    }

    /**
     * Apply the transfer (effective today or earlier) — updates the Employee row in place,
     * marks the transfer APPLIED, publishes employee.transfer.applied for downstream
     * recipients (payroll, leave, asset, search index, etc.).
     */
    @Transactional
    public EmployeeTransfer apply(UUID transferId) {
        EmployeeTransfer t = transfers.findById(transferId).orElseThrow();
        if (t.getStatus() != EmployeeTransfer.Status.APPROVED) {
            throw new IllegalStateException("Transfer must be APPROVED before apply");
        }
        Employee e = employees.findById(t.getEmployeeId()).orElseThrow();

        if (t.getToDepartmentId() != null)  e.setDepartmentId(t.getToDepartmentId());
        if (t.getToDesignationId() != null) e.setDesignationId(t.getToDesignationId());
        if (t.getToLocationId() != null)    e.setLocationId(t.getToLocationId());
        if (t.getToManagerId() != null)     e.setManagerId(t.getToManagerId());
        if (t.getToPayGradeId() != null)    e.setPayGradeId(t.getToPayGradeId());
        if (t.getType() == EmployeeTransfer.Type.CONFIRMATION) {
            e.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
            e.setConfirmationDate(LocalDate.now());
        }
        employees.save(e);

        t.setStatus(EmployeeTransfer.Status.APPLIED);
        t.setAppliedAt(Instant.now());
        EmployeeTransfer saved = transfers.save(t);

        events.publish(Topics.EMPLOYEE, DomainEvent.of(
                "employee.transfer.applied", "core-hr",
                saved.getTenantId(), e.getId().toString(), "Employee",
                Map.of("transferType", t.getType(),
                       "toDepartmentId", n(t.getToDepartmentId()),
                       "toDesignationId", n(t.getToDesignationId()),
                       "toManagerId", n(t.getToManagerId()),
                       "salaryChangePercentage", n(t.getSalaryChangePercentage()))));
        return saved;
    }

    private Object n(Object v) { return v == null ? "" : v; }
}
