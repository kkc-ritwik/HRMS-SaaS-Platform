package com.hrms.corehr.mapper;

import com.hrms.corehr.dto.FamilyMemberDto;
import com.hrms.corehr.entity.FamilyMember;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FamilyMemberMapper {

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "tenantId",   ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "updatedBy",  ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "deleted",    ignore = true)
    @Mapping(target = "dependent",  source = "isDependent")
    FamilyMember toEntity(FamilyMemberDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "tenantId",   ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "updatedBy",  ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "deleted",    ignore = true)
    @Mapping(target = "dependent",  source = "isDependent")
    void updateEntity(FamilyMemberDto.CreateRequest req, @MappingTarget FamilyMember entity);

    @Mapping(target = "isDependent", source = "dependent")
    FamilyMemberDto.Response toResponse(FamilyMember entity);
}
