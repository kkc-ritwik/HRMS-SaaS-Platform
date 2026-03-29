package com.hrms.performance.mapper;

import com.hrms.performance.dto.FeedbackDto;
import com.hrms.performance.entity.Feedback;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "tenantId",       ignore = true)
    @Mapping(target = "fromEmployeeId", ignore = true)  // set from security principal in service
    @Mapping(target = "createdBy",      ignore = true)
    @Mapping(target = "updatedBy",      ignore = true)
    @Mapping(target = "createdAt",      ignore = true)
    @Mapping(target = "updatedAt",      ignore = true)
    @Mapping(target = "deleted",        ignore = true)
    Feedback toEntity(FeedbackDto.CreateRequest req);

    @Mapping(target = "fromEmployeeName", ignore = true)  // anonymization handled in service
    FeedbackDto.Response toResponse(Feedback entity);

    @Mapping(target = "fromEmployeeName", ignore = true)
    FeedbackDto.Summary toSummary(Feedback entity);
}
