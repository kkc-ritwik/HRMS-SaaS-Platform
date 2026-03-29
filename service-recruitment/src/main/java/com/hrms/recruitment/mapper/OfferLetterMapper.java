package com.hrms.recruitment.mapper;

import com.hrms.recruitment.dto.OfferLetterDto;
import com.hrms.recruitment.entity.OfferLetter;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OfferLetterMapper {

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "tenantId",       ignore = true)
    @Mapping(target = "createdBy",      ignore = true)
    @Mapping(target = "updatedBy",      ignore = true)
    @Mapping(target = "createdAt",      ignore = true)
    @Mapping(target = "updatedAt",      ignore = true)
    @Mapping(target = "deleted",        ignore = true)
    @Mapping(target = "status",         ignore = true)
    @Mapping(target = "content",        ignore = true)
    @Mapping(target = "sentAt",         ignore = true)
    @Mapping(target = "respondedAt",    ignore = true)
    @Mapping(target = "responseNotes",  ignore = true)
    OfferLetter toEntity(OfferLetterDto.CreateRequest req);

    OfferLetterDto.Response toResponse(OfferLetter entity);
}
