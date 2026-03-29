package com.hrms.leave.mapper;

import com.hrms.leave.dto.LeaveApplicationDto;
import com.hrms.leave.entity.LeaveApproval;
import com.hrms.leave.entity.LeaveApplication;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LeaveApplicationMapper {

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "tenantId",        ignore = true)
    @Mapping(target = "employeeId",      ignore = true)
    @Mapping(target = "createdBy",       ignore = true)
    @Mapping(target = "updatedBy",       ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "deleted",         ignore = true)
    @Mapping(target = "status",          ignore = true)
    @Mapping(target = "durationDays",    ignore = true)
    @Mapping(target = "appliedAt",       ignore = true)
    @Mapping(target = "cancelledAt",     ignore = true)
    @Mapping(target = "cancelReason",    ignore = true)
    LeaveApplication toEntity(LeaveApplicationDto.ApplyRequest req);

    @Mapping(target = "employeeName",   ignore = true)
    @Mapping(target = "leaveTypeName",  ignore = true)
    @Mapping(target = "leaveTypeColor", ignore = true)
    @Mapping(target = "approvals",      ignore = true)
    LeaveApplicationDto.Response toResponse(LeaveApplication entity);

    @Mapping(target = "approverName", ignore = true)
    @Mapping(target = "status",       expression = "java(entity.getStatus().name())")
    LeaveApplicationDto.ApprovalInfo toApprovalInfo(LeaveApproval entity);
}
