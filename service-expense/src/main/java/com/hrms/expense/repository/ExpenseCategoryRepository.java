package com.hrms.expense.repository;

import com.hrms.expense.entity.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, UUID> {

    Optional<ExpenseCategory> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<ExpenseCategory> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ExpenseCategory> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);
}
