package com.hrms.expense.repository;

import com.hrms.expense.entity.ExpenseItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, UUID> {

    Optional<ExpenseItem> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<ExpenseItem> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ExpenseItem> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<ExpenseItem> findByTenantIdAndReportIdAndDeletedFalse(String tenantId, UUID reportId);
}
