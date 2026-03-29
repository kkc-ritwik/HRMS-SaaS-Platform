package com.hrms.leave.repository;

import com.hrms.leave.entity.LeaveApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, UUID> {

    List<LeaveApproval> findByLeaveApplicationIdOrderByLevelAsc(UUID leaveApplicationId);

    Optional<LeaveApproval> findByLeaveApplicationIdAndApproverIdAndDeletedFalse(
            UUID leaveApplicationId, UUID approverId);

    List<LeaveApproval> findByApproverIdAndStatusAndDeletedFalse(
            UUID approverId, LeaveApproval.ApprovalStatus status);
}
