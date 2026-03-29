package com.hrms.corehr.mapper;

import com.hrms.corehr.dto.EmploymentHistoryDto;
import com.hrms.corehr.entity.EmploymentHistory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmploymentHistoryMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "tenantId",         ignore = true)
    @Mapping(target = "employeeId",       ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "responsibilities", ignore = true)
    @Mapping(target = "startDate",        source = "fromDate")
    @Mapping(target = "endDate",          source = "toDate")
    EmploymentHistory toEntity(EmploymentHistoryDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "tenantId",         ignore = true)
    @Mapping(target = "employeeId",       ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "responsibilities", ignore = true)
    @Mapping(target = "startDate",        source = "fromDate")
    @Mapping(target = "endDate",          source = "toDate")
    void updateEntity(EmploymentHistoryDto.CreateRequest req, @MappingTarget EmploymentHistory entity);

    @Mapping(target = "fromDate", source = "startDate")
    @Mapping(target = "toDate",   source = "endDate")
    EmploymentHistoryDto.Response toResponse(EmploymentHistory entity);
}
