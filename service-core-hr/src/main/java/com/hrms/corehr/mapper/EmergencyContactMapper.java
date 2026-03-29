package com.hrms.corehr.mapper;

import com.hrms.corehr.dto.EmergencyContactDto;
import com.hrms.corehr.entity.EmergencyContact;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmergencyContactMapper {

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "tenantId",   ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "updatedBy",  ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "deleted",    ignore = true)
    @Mapping(target = "primary",    source = "isPrimary")
    EmergencyContact toEntity(EmergencyContactDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "tenantId",   ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "updatedBy",  ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "deleted",    ignore = true)
    @Mapping(target = "primary",    source = "isPrimary")
    void updateEntity(EmergencyContactDto.CreateRequest req, @MappingTarget EmergencyContact entity);

    @Mapping(target = "isPrimary", source = "primary")
    EmergencyContactDto.Response toResponse(EmergencyContact entity);
}
