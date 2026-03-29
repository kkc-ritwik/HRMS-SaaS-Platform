package com.hrms.payroll.mapper;

import com.hrms.payroll.dto.SalaryStructureDto;
import com.hrms.payroll.entity.SalaryStructure;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SalaryStructureMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    @Mapping(target = "active",    constant = "true")
    SalaryStructure toEntity(SalaryStructureDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    void updateEntity(SalaryStructureDto.UpdateRequest req, @MappingTarget SalaryStructure entity);

    @Mapping(target = "components", ignore = true)  // enriched in service
    SalaryStructureDto.Response toResponse(SalaryStructure entity);
}
