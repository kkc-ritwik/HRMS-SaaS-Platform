package com.hrms.recruitment.mapper;

import com.hrms.recruitment.dto.JobRequisitionDto;
import com.hrms.recruitment.entity.JobRequisition;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface JobRequisitionMapper {

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "tenantId",        ignore = true)
    @Mapping(target = "createdBy",       ignore = true)
    @Mapping(target = "updatedBy",       ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "deleted",         ignore = true)
    @Mapping(target = "status",          ignore = true)
    @Mapping(target = "positionsFilled", ignore = true)
    @Mapping(target = "requestedBy",     ignore = true)
    @Mapping(target = "approvedBy",      ignore = true)
    @Mapping(target = "approvedAt",      ignore = true)
    JobRequisition toEntity(JobRequisitionDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "tenantId",        ignore = true)
    @Mapping(target = "createdBy",       ignore = true)
    @Mapping(target = "updatedBy",       ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "deleted",         ignore = true)
    @Mapping(target = "status",          ignore = true)
    @Mapping(target = "positionsFilled", ignore = true)
    @Mapping(target = "requestedBy",     ignore = true)
    @Mapping(target = "approvedBy",      ignore = true)
    @Mapping(target = "approvedAt",      ignore = true)
    void updateEntity(JobRequisitionDto.UpdateRequest req, @MappingTarget JobRequisition entity);

    @Mapping(target = "openPositions",
             expression = "java(entity.getPositionsCount() - entity.getPositionsFilled())")
    JobRequisitionDto.Response toResponse(JobRequisition entity);
}
