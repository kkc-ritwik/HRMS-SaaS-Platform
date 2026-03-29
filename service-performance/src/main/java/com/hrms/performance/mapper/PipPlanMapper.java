package com.hrms.performance.mapper;

import com.hrms.performance.dto.PipPlanDto;
import com.hrms.performance.entity.PipPlan;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PipPlanMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "tenantId",         ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "status",           ignore = true)
    @Mapping(target = "outcomeNotes",     ignore = true)
    @Mapping(target = "closedAt",         ignore = true)
    PipPlan toEntity(PipPlanDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "tenantId",      ignore = true)
    @Mapping(target = "employeeId",    ignore = true)
    @Mapping(target = "managerId",     ignore = true)
    @Mapping(target = "hrManagerId",   ignore = true)
    @Mapping(target = "reviewCycleId", ignore = true)
    @Mapping(target = "startDate",     ignore = true)
    @Mapping(target = "createdBy",     ignore = true)
    @Mapping(target = "updatedBy",     ignore = true)
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "updatedAt",     ignore = true)
    @Mapping(target = "deleted",       ignore = true)
    @Mapping(target = "closedAt",      ignore = true)
    void updateEntity(PipPlanDto.UpdateRequest req, @MappingTarget PipPlan entity);

    PipPlanDto.Response toResponse(PipPlan entity);
}
