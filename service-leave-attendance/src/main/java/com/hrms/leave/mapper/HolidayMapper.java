package com.hrms.leave.mapper;

import com.hrms.leave.dto.HolidayDto;
import com.hrms.leave.entity.Holiday;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HolidayMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    @Mapping(target = "active",    constant = "true")
    @Mapping(target = "year",      ignore = true)
    Holiday toEntity(HolidayDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    @Mapping(target = "year",      ignore = true)
    void updateEntity(HolidayDto.UpdateRequest req, @MappingTarget Holiday entity);

    HolidayDto.Response toResponse(Holiday entity);
}
