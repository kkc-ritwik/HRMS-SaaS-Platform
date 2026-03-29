package com.hrms.common.exception;
import lombok.Getter;
@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final String fieldName;
    private final Object fieldValue;
    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceType, fieldName, fieldValue));
        this.resourceType = resourceType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
