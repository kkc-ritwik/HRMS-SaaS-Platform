package com.hrms.corehr.repository;

import com.hrms.corehr.entity.EmployeeLifecycleEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeLifecycleEventRepository extends JpaRepository<EmployeeLifecycleEvent, UUID> {

    List<EmployeeLifecycleEvent> findByEmployeeIdAndDeletedFalseOrderByEventDateDesc(UUID employeeId);

    Page<EmployeeLifecycleEvent> findByEmployeeIdAndDeletedFalse(UUID employeeId, Pageable pageable);

    List<EmployeeLifecycleEvent> findByEmployeeIdAndEventTypeAndDeletedFalse(
            UUID employeeId, EmployeeLifecycleEvent.EventType eventType);
}
