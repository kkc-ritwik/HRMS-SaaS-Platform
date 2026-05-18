package com.hrms.timesheet.repository;

import com.hrms.timesheet.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TimesheetRepositories {
    private TimesheetRepositories() {}

    public interface ProjectRepository extends JpaRepository<Project, UUID> {
        Page<Project> findByTenantIdAndStatus(String tenantId, Project.Status s, Pageable p);
        Optional<Project> findByTenantIdAndCode(String tenantId, String code);
    }
    public interface TaskRepository extends JpaRepository<Task, UUID> {
        List<Task> findByTenantIdAndProjectIdAndIsActiveTrue(String tenantId, UUID projectId);
    }
    public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, UUID> {
        List<TimesheetEntry> findByTenantIdAndEmployeeIdAndWorkDateBetween(
                String tenantId, UUID employeeId, LocalDate from, LocalDate to);
        List<TimesheetEntry> findByTenantIdAndProjectIdAndWorkDateBetween(
                String tenantId, UUID projectId, LocalDate from, LocalDate to);
    }
    public interface WeeklyTimesheetRepository extends JpaRepository<WeeklyTimesheet, UUID> {
        Optional<WeeklyTimesheet> findByTenantIdAndEmployeeIdAndWeekStart(String tenantId, UUID employeeId, LocalDate weekStart);
        Page<WeeklyTimesheet> findByTenantIdAndStatus(String tenantId, WeeklyTimesheet.Status s, Pageable p);
    }
}
