package com.hrms.payroll.mapper;

import com.hrms.payroll.dto.LoanDto;
import com.hrms.payroll.entity.Loan;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "id",                   ignore = true)
    @Mapping(target = "tenantId",             ignore = true)
    @Mapping(target = "employeeId",           ignore = true)
    @Mapping(target = "createdBy",            ignore = true)
    @Mapping(target = "updatedBy",            ignore = true)
    @Mapping(target = "createdAt",            ignore = true)
    @Mapping(target = "updatedAt",            ignore = true)
    @Mapping(target = "deleted",              ignore = true)
    @Mapping(target = "emiAmount",            ignore = true)
    @Mapping(target = "outstandingBalance",   ignore = true)
    @Mapping(target = "status",               ignore = true)
    Loan toEntity(LoanDto.CreateRequest req);

    @Mapping(target = "repaymentsMade", ignore = true)  // enriched in service
    LoanDto.Response toResponse(Loan entity);
}
