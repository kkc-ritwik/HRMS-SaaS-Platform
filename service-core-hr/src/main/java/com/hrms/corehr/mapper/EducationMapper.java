package com.hrms.corehr.mapper;

import com.hrms.corehr.dto.EducationDto;
import com.hrms.corehr.entity.Education;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EducationMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "tenantId",     ignore = true)
    @Mapping(target = "employeeId",   ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "updatedBy",    ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    @Mapping(target = "startYear",    ignore = true)
    @Mapping(target = "endYear",      ignore = true)
    @Mapping(target = "grade",        ignore = true)
    @Mapping(target = "description",  ignore = true)
    @Mapping(target = "fieldOfStudy", source = "specialization")
    Education toEntity(EducationDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "tenantId",     ignore = true)
    @Mapping(target = "employeeId",   ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "updatedBy",    ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    @Mapping(target = "startYear",    ignore = true)
    @Mapping(target = "endYear",      ignore = true)
    @Mapping(target = "grade",        ignore = true)
    @Mapping(target = "description",  ignore = true)
    @Mapping(target = "fieldOfStudy", source = "specialization")
    void updateEntity(EducationDto.CreateRequest req, @MappingTarget Education entity);

    @Mapping(target = "specialization", source = "fieldOfStudy")
    EducationDto.Response toResponse(Education entity);
}
