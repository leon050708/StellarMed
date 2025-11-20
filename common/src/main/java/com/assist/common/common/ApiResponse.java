package com.assist.common.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应结构
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(0, "success", data);
    }

    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<T>(-1, msg, null);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<T>(code, msg, null);
    }
}

