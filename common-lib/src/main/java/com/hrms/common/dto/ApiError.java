package com.hrms.common.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class ApiError {
    private String code;
    private String message;
    private String field;
}
