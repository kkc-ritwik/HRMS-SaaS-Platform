package com.hrms.expense.repository;

import com.hrms.expense.entity.ExpensePolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpensePolicyRepository extends JpaRepository<ExpensePolicy, UUID> {

    Optional<ExpensePolicy> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<ExpensePolicy> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ExpensePolicy> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);
}
