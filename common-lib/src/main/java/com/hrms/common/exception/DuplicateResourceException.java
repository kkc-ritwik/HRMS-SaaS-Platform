package com.hrms.common.exception;
import lombok.Getter;
@Getter
public class DuplicateResourceException extends RuntimeException {
    private final String resourceType;
    private final String fieldName;
    private final Object fieldValue;
    public DuplicateResourceException(String resourceType, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceType, fieldName, fieldValue));
        this.resourceType = resourceType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
