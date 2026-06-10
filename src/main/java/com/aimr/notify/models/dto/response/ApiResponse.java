package com.aimr.notify.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T>{
    private boolean success;
    private int statusCode;
    private Instant timeStamp;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(int statusCode,String message, T data){
        return new ApiResponse<>(true, statusCode, Instant.now(), message, data);
    }
    public static <T> ApiResponse<T> error(int statusCode,String message){
        return new ApiResponse<>(false,statusCode,Instant.now(),  message, null);
    }
}
