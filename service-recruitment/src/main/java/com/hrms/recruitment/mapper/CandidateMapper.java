package com.hrms.recruitment.mapper;

import com.hrms.recruitment.dto.CandidateDto;
import com.hrms.recruitment.entity.Candidate;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CandidateMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    Candidate toEntity(CandidateDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "email",     ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    void updateEntity(CandidateDto.UpdateRequest req, @MappingTarget Candidate entity);

    @Mapping(target = "fullName",
             expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    @Mapping(target = "applicationCount", ignore = true)  // enriched in service
    CandidateDto.Response toResponse(Candidate entity);

    @Mapping(target = "fullName",
             expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    CandidateDto.Summary toSummary(Candidate entity);
}
