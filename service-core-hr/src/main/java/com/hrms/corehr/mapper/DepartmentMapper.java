package com.hrms.corehr.mapper;

import com.hrms.corehr.dto.DepartmentDto;
import com.hrms.corehr.entity.Department;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "headCount", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    @Mapping(target = "active",    constant = "true")
    Department toEntity(DepartmentDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "code",      ignore = true)
    @Mapping(target = "headCount", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    void updateEntity(DepartmentDto.UpdateRequest req, @MappingTarget Department entity);

    @Mapping(target = "parentName",  ignore = true)
    @Mapping(target = "managerName", ignore = true)
    @Mapping(target = "children",    ignore = true)
    DepartmentDto.Response toResponse(Department entity);

    DepartmentDto.ListItem toListItem(Department entity);
}
