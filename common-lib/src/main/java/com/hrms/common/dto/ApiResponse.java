package com.hrms.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private PaginationMeta pagination;
    private List<ApiError> errors;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    public static <T> ApiResponse<T> ok(T data, PaginationMeta pagination) {
        return ApiResponse.<T>builder().success(true).data(data).pagination(pagination).build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .errors(List.of(new ApiError(code, message, null)))
                .build();
    }

    public static <T> ApiResponse<T> error(List<ApiError> errors) {
        return ApiResponse.<T>builder().success(false).errors(errors).build();
    }
}
