package com.hrms.payroll.mapper;

import com.hrms.payroll.dto.PayslipDto;
import com.hrms.payroll.entity.Payslip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayslipMapper {

    @Mapping(target = "components", source = "componentsJson")
    PayslipDto.Response toResponse(Payslip entity);

    @Mapping(target = "grossEarnings", source = "grossEarnings")
    @Mapping(target = "netPay",        source = "netPay")
    @Mapping(target = "tds",           source = "tds")
    @Mapping(target = "status",        source = "status")
    @Mapping(target = "pdfUrl",        source = "pdfUrl")
    PayslipDto.Summary toSummary(Payslip entity);
}
