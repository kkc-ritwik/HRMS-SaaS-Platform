package com.hrms.payroll.mapper;

import com.hrms.payroll.dto.TaxConfigDto;
import com.hrms.payroll.entity.EsiConfig;
import com.hrms.payroll.entity.PfConfig;
import com.hrms.payroll.entity.PtSlab;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TaxConfigMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    PfConfig toEntity(TaxConfigDto.PfConfigRequest req);

    TaxConfigDto.PfConfigResponse toPfResponse(PfConfig entity);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    EsiConfig toEntity(TaxConfigDto.EsiConfigRequest req);

    TaxConfigDto.EsiConfigResponse toEsiResponse(EsiConfig entity);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    PtSlab toEntity(TaxConfigDto.PtSlabRequest req);

    TaxConfigDto.PtSlabResponse toPtResponse(PtSlab entity);
}
