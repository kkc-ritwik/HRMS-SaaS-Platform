package com.hrms.corehr.mapper;

import com.hrms.corehr.dto.EmployeeDto;
import com.hrms.corehr.entity.Address;
import com.hrms.corehr.entity.Employee;
import com.hrms.corehr.entity.EmployeeLifecycleEvent;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "id",                  ignore = true)
    @Mapping(target = "tenantId",            ignore = true)
    @Mapping(target = "employeeCode",        ignore = true)
    @Mapping(target = "employmentStatus",    constant = "ACTIVE")
    @Mapping(target = "profilePictureUrl",   ignore = true)
    @Mapping(target = "confirmationDate",    ignore = true)
    @Mapping(target = "exitDate",            ignore = true)
    @Mapping(target = "displayName",         ignore = true)  // set in service
    @Mapping(target = "probationEndDate",    ignore = true)
    @Mapping(target = "resignationDate",     ignore = true)
    @Mapping(target = "lastWorkingDate",     ignore = true)
    @Mapping(target = "esiNumber",           ignore = true)
    @Mapping(target = "bankBranch",          ignore = true)
    @Mapping(target = "aboutMe",             ignore = true)
    @Mapping(target = "shiftId",             ignore = true)
    @Mapping(target = "payGradeId",          ignore = true)
    @Mapping(target = "costCenterCode",      ignore = true)
    @Mapping(target = "tags",                ignore = true)
    @Mapping(target = "createdBy",           ignore = true)
    @Mapping(target = "updatedBy",           ignore = true)
    @Mapping(target = "createdAt",           ignore = true)
    @Mapping(target = "updatedAt",           ignore = true)
    @Mapping(target = "deleted",             ignore = true)
    Employee toEntity(EmployeeDto.CreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "tenantId",     ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "email",        ignore = true)
    @Mapping(target = "userId",       ignore = true)
    @Mapping(target = "joinDate",     ignore = true)
    @Mapping(target = "displayName",  ignore = true)  // set in service
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "updatedBy",    ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    void updateEntity(EmployeeDto.UpdateRequest req, @MappingTarget Employee entity);

    @Mapping(target = "fullName",        expression = "java(entity.getFirstName() + (entity.getMiddleName() != null ? \" \" + entity.getMiddleName() : \"\") + \" \" + entity.getLastName())")
    @Mapping(target = "departmentName",  ignore = true)
    @Mapping(target = "designationName", ignore = true)
    @Mapping(target = "locationName",    ignore = true)
    @Mapping(target = "managerName",     ignore = true)
    @Mapping(target = "addresses",       ignore = true)
    EmployeeDto.Response toResponse(Employee entity);

    @Mapping(target = "fullName",        expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    @Mapping(target = "departmentName",  ignore = true)
    @Mapping(target = "designationName", ignore = true)
    @Mapping(target = "locationName",    ignore = true)
    EmployeeDto.ListItem toListItem(Employee entity);

    @Mapping(target = "fullName",        expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    @Mapping(target = "emailWork",       ignore = true)  // set in service
    @Mapping(target = "phonePrimary",    ignore = true)  // set in service
    @Mapping(target = "photoUrl",        ignore = true)  // set in service
    @Mapping(target = "departmentName",  ignore = true)
    @Mapping(target = "designationName", ignore = true)
    @Mapping(target = "locationName",    ignore = true)
    EmployeeDto.DirectoryItem toDirectoryItem(Employee entity);

    @Mapping(target = "addressType", expression = "java(entity.getAddressType().name())")
    EmployeeDto.AddressInfo toAddressInfo(Address entity);

    @Mapping(target = "eventType", expression = "java(entity.getEventType().name())")
    @Mapping(target = "reason",   source = "reason")
    @Mapping(target = "comments", source = "comments")
    @Mapping(target = "approvedBy", source = "approvedBy")
    @Mapping(target = "effectiveDate", source = "effectiveDate")
    EmployeeDto.LifecycleEventResponse toLifecycleResponse(EmployeeLifecycleEvent entity);
}
