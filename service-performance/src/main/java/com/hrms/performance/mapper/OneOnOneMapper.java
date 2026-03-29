package com.hrms.performance.mapper;

import com.hrms.performance.dto.OneOnOneDto;
import com.hrms.performance.entity.OneOnOne;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OneOnOneMapper {

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "tenantId",        ignore = true)
    @Mapping(target = "managerId",       ignore = true)  // set from security principal in service
    @Mapping(target = "createdBy",       ignore = true)
    @Mapping(target = "updatedBy",       ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "deleted",         ignore = true)
    @Mapping(target = "status",          ignore = true)
    @Mapping(target = "managerNotes",    ignore = true)
    @Mapping(target = "employeeNotes",   ignore = true)
    @Mapping(target = "actionItems",     ignore = true)
    OneOnOne toEntity(OneOnOneDto.ScheduleRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "tenantId",    ignore = true)
    @Mapping(target = "managerId",   ignore = true)
    @Mapping(target = "employeeId",  ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "updatedBy",   ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    void updateEntity(OneOnOneDto.UpdateRequest req, @MappingTarget OneOnOne entity);

    OneOnOneDto.Response toResponse(OneOnOne entity);
}
