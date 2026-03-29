package com.hrms.recruitment.mapper;

import com.hrms.recruitment.dto.AgencyDto;
import com.hrms.recruitment.entity.RecruitmentAgency;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AgencyMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    @Mapping(target = "active",    constant = "true")
    RecruitmentAgency toEntity(AgencyDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    void updateEntity(AgencyDto.UpdateRequest req, @MappingTarget RecruitmentAgency entity);

    AgencyDto.Response toResponse(RecruitmentAgency entity);
}
