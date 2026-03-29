package com.hrms.corehr.mapper;

import com.hrms.corehr.dto.DesignationDto;
import com.hrms.corehr.entity.Designation;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DesignationMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "active",    ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    Designation toEntity(DesignationDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "code",      ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    void updateEntity(DesignationDto.UpdateRequest req, @MappingTarget Designation entity);

    DesignationDto.Response toResponse(Designation entity);
}
