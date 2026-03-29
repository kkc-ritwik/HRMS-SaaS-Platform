package com.hrms.payroll.mapper;

import com.hrms.payroll.dto.PayrollRunDto;
import com.hrms.payroll.entity.PayrollRun;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PayrollRunMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "tenantId",         ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "status",           ignore = true)
    @Mapping(target = "totalGross",       ignore = true)
    @Mapping(target = "totalDeductions",  ignore = true)
    @Mapping(target = "totalNet",         ignore = true)
    @Mapping(target = "totalEmployerPf",  ignore = true)
    @Mapping(target = "totalEmployerEsi", ignore = true)
    @Mapping(target = "employeeCount",    ignore = true)
    @Mapping(target = "processedBy",      ignore = true)
    @Mapping(target = "processedAt",      ignore = true)
    @Mapping(target = "lockedBy",         ignore = true)
    @Mapping(target = "lockedAt",         ignore = true)
    PayrollRun toEntity(PayrollRunDto.CreateRequest req);

    PayrollRunDto.Response toResponse(PayrollRun entity);
}
