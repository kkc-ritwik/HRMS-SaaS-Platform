package com.hrms.payroll.mapper;

import com.hrms.payroll.dto.SalaryComponentDto;
import com.hrms.payroll.entity.SalaryComponent;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SalaryComponentMapper {

    @Mapping(target = "id",                       ignore = true)
    @Mapping(target = "tenantId",                 ignore = true)
    @Mapping(target = "createdBy",                ignore = true)
    @Mapping(target = "updatedBy",                ignore = true)
    @Mapping(target = "createdAt",                ignore = true)
    @Mapping(target = "updatedAt",                ignore = true)
    @Mapping(target = "deleted",                  ignore = true)
    @Mapping(target = "active",                   constant = "true")
    SalaryComponent toEntity(SalaryComponentDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "code",      ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    void updateEntity(SalaryComponentDto.UpdateRequest req, @MappingTarget SalaryComponent entity);

    @Mapping(target = "percentageOfComponentName", ignore = true)  // enriched in service
    SalaryComponentDto.Response toResponse(SalaryComponent entity);
}
