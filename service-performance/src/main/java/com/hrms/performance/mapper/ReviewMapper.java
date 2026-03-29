package com.hrms.performance.mapper;

import com.hrms.performance.dto.ReviewDto;
import com.hrms.performance.entity.Review;
import com.hrms.performance.entity.ReviewRating;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "id",                  ignore = true)
    @Mapping(target = "tenantId",            ignore = true)
    @Mapping(target = "createdBy",           ignore = true)
    @Mapping(target = "updatedBy",           ignore = true)
    @Mapping(target = "createdAt",           ignore = true)
    @Mapping(target = "updatedAt",           ignore = true)
    @Mapping(target = "deleted",             ignore = true)
    @Mapping(target = "status",              ignore = true)
    @Mapping(target = "overallRating",       ignore = true)
    @Mapping(target = "potentialRating",     ignore = true)
    @Mapping(target = "performanceRating",   ignore = true)
    @Mapping(target = "goalScore",           ignore = true)
    @Mapping(target = "competencyScore",     ignore = true)
    @Mapping(target = "strengths",           ignore = true)
    @Mapping(target = "developmentAreas",    ignore = true)
    @Mapping(target = "managerComments",     ignore = true)
    @Mapping(target = "finalComments",       ignore = true)
    @Mapping(target = "submittedAt",         ignore = true)
    @Mapping(target = "acknowledgedAt",      ignore = true)
    Review toEntity(ReviewDto.CreateRequest req);

    @Mapping(target = "cycleName", ignore = true)  // enriched in service
    @Mapping(target = "ratings",   ignore = true)  // enriched in service
    ReviewDto.Response toResponse(Review entity);

    @Mapping(target = "competencyName", ignore = true)  // enriched in service
    @Mapping(target = "goalTitle",      ignore = true)  // enriched in service
    ReviewDto.RatingResponse toRatingResponse(ReviewRating entity);
}
