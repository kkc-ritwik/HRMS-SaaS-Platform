package com.hrms.performance.mapper;

import com.hrms.performance.dto.CompetencyDto;
import com.hrms.performance.entity.Competency;
import com.hrms.performance.entity.RoleCompetency;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CompetencyMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    @Mapping(target = "active",    constant = "true")
    Competency toEntity(CompetencyDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    void updateEntity(CompetencyDto.UpdateRequest req, @MappingTarget Competency entity);

    CompetencyDto.Response toResponse(Competency entity);

    @Mapping(target = "competencyName", ignore = true)  // enriched in service
    CompetencyDto.RoleMappingResponse toMappingResponse(RoleCompetency entity);
}
