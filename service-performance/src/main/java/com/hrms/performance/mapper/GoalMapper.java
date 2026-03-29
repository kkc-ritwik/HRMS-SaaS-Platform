package com.hrms.performance.mapper;

import com.hrms.performance.dto.GoalDto;
import com.hrms.performance.entity.Goal;
import com.hrms.performance.entity.GoalUpdate;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", imports = BigDecimal.class)
public interface GoalMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "tenantId",     ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "updatedBy",    ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    @Mapping(target = "status",       constant = "DRAFT")
    @Mapping(target = "progress",     expression = "java(BigDecimal.ZERO)")
    @Mapping(target = "currentValue", expression = "java(BigDecimal.ZERO)")
    Goal toEntity(GoalDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "tenantId",     ignore = true)
    @Mapping(target = "employeeId",   ignore = true)
    @Mapping(target = "managerId",    ignore = true)
    @Mapping(target = "cycleId",      ignore = true)
    @Mapping(target = "parentGoalId", ignore = true)
    @Mapping(target = "goalType",     ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "updatedBy",    ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    @Mapping(target = "progress",     ignore = true)
    @Mapping(target = "currentValue", ignore = true)
    void updateEntity(GoalDto.UpdateRequest req, @MappingTarget Goal entity);

    @Mapping(target = "keyResults", ignore = true)  // enriched in service for OBJECTIVE goals
    GoalDto.Response toResponse(Goal entity);

    @Mapping(target = "goalId", source = "goalId")
    GoalDto.UpdateResponse toUpdateResponse(GoalUpdate entity);
}
