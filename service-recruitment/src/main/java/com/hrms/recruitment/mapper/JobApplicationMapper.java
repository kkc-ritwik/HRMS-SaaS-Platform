package com.hrms.recruitment.mapper;

import com.hrms.recruitment.dto.ApplicationDto;
import com.hrms.recruitment.entity.Application;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface JobApplicationMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "tenantId",         ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "stage",            ignore = true)
    @Mapping(target = "rejectionReason",  ignore = true)
    @Mapping(target = "appliedAt",        ignore = true)
    @Mapping(target = "stageChangedAt",   ignore = true)
    Application toEntity(ApplicationDto.CreateRequest req);

    @Mapping(target = "requisitionTitle", ignore = true)  // enriched in service
    @Mapping(target = "candidate",        ignore = true)  // enriched in service
    ApplicationDto.Response toResponse(Application entity);
}
