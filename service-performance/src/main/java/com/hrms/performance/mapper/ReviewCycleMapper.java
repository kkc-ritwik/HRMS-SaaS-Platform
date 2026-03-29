package com.hrms.performance.mapper;

import com.hrms.performance.dto.ReviewCycleDto;
import com.hrms.performance.entity.ReviewCycle;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReviewCycleMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    @Mapping(target = "status",    ignore = true)
    ReviewCycle toEntity(ReviewCycleDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted",   ignore = true)
    @Mapping(target = "status",    ignore = true)
    @Mapping(target = "cycleType", ignore = true)
    @Mapping(target = "fiscalYear", ignore = true)
    void updateEntity(ReviewCycleDto.UpdateRequest req, @MappingTarget ReviewCycle entity);

    ReviewCycleDto.Response toResponse(ReviewCycle entity);
}
