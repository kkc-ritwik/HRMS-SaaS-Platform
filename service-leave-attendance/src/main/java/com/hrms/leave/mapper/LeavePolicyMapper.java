package com.hrms.leave.mapper;

import com.hrms.leave.dto.LeavePolicyDto;
import com.hrms.leave.entity.LeavePolicy;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LeavePolicyMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "tenantId",         ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "active",           constant = "true")
    LeavePolicy toEntity(LeavePolicyDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "tenantId",         ignore = true)
    @Mapping(target = "leaveTypeId",      ignore = true)
    @Mapping(target = "proRataOnJoining", ignore = true)
    @Mapping(target = "proRataOnExit",    ignore = true)
    @Mapping(target = "effectiveFrom",    ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    void updateEntity(LeavePolicyDto.UpdateRequest req, @MappingTarget LeavePolicy entity);

    @Mapping(target = "leaveTypeName", ignore = true)  // enriched in service
    LeavePolicyDto.Response toResponse(LeavePolicy entity);
}
