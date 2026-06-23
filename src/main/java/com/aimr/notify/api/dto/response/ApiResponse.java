package com.aimr.notify.api.dto.response;

import java.time.Instant;

public record ApiResponse<T>(
    boolean success,
    int statusCode,
    Instant timeStamp,
    String message,
    T data
) {
     public static <T> ApiResponse<T> success(int statusCode,String message, T data){
        return new ApiResponse<>(true, statusCode, Instant.now(), message, data);
    }
    public static <T> ApiResponse<T> error(int statusCode,String message){
        return new ApiResponse<>(false,statusCode,Instant.now(),  message, null);
    }
}