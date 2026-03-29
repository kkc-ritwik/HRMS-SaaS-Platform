package com.hrms.recruitment.mapper;

import com.hrms.recruitment.dto.InterviewDto;
import com.hrms.recruitment.entity.Interview;
import com.hrms.recruitment.entity.InterviewPanelist;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InterviewMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "tenantId",         ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "status",           ignore = true)
    @Mapping(target = "overallRating",    ignore = true)
    @Mapping(target = "recommendation",   ignore = true)
    @Mapping(target = "feedback",         ignore = true)
    Interview toEntity(InterviewDto.ScheduleRequest req);

    @Mapping(target = "panelists", ignore = true)  // enriched in service
    InterviewDto.Response toResponse(Interview entity);

    InterviewDto.PanelistSummary toPanelistSummary(InterviewPanelist panelist);
}
