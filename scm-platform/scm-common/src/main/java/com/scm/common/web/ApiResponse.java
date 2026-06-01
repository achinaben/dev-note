package com.scm.common.web;

public record ApiResponse<T>(String code, String message, String requestId, T data) {
    public static <T> ApiResponse<T> ok(String requestId, T data) {
        return new ApiResponse<>("0", "OK", requestId, data);
    }

    public static <T> ApiResponse<T> error(String code, String message, String requestId) {
        return new ApiResponse<>(code, message, requestId, null);
    }
}
