package com.hrms.corehr.mapper;

import com.hrms.corehr.dto.AddressDto;
import com.hrms.corehr.entity.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring", imports = Address.class)
public interface AddressMapper {

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "tenantId",    ignore = true)
    @Mapping(target = "employeeId",  ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "updatedBy",   ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "addressType",
             expression = "java(Address.AddressType.valueOf(req.getType().toUpperCase()))")
    Address toEntity(AddressDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "tenantId",   ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "updatedBy",  ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "deleted",    ignore = true)
    @Mapping(target = "addressType",
             expression = "java(req.getType() != null ? Address.AddressType.valueOf(req.getType().toUpperCase()) : entity.getAddressType())")
    void updateEntity(AddressDto.CreateRequest req, @MappingTarget Address entity);

    @Mapping(target = "type",
             expression = "java(entity.getAddressType() != null ? entity.getAddressType().name() : null)")
    AddressDto.Response toResponse(Address entity);
}
