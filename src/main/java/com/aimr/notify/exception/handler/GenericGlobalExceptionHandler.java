package com.aimr.notify.exception.handler;

import com.aimr.notify.exception.*;
import com.aimr.notify.model.dto.response.ApiResponse;
import com.aimr.notify.util.CommonUtils;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.function.Supplier;

@RestControllerAdvice
public class GenericGlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ApiResponse<@NonNull String> handleValidationException(ValidationException exception){
        return genericExceptionHandler(exception, ()-> ApiResponse.error(HttpStatus.
                BAD_REQUEST.value(), exception.getErrorMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<@NonNull String> handleResourceNotFoundException(ResourceNotFoundException exception){
        return genericExceptionHandler(exception, ()-> ApiResponse.error(HttpStatus
                .NOT_FOUND.value(), exception.getErrorMessage()));
    }

    @ExceptionHandler(DataTransportException.class)
    public ApiResponse<@NonNull String> handleNotificationDispatchException(DataTransportException exception){
        return genericExceptionHandler(exception, ()->ApiResponse.error(HttpStatus.
                EXPECTATION_FAILED.value(), exception.getErrorMessage()));
    }

    @ExceptionHandler(DataConversionException.class)
    public ApiResponse<@NonNull String> handleDataConversionException(DataConversionException exception){
        return genericExceptionHandler(exception, ()->ApiResponse.error(HttpStatus
                .UNPROCESSABLE_CONTENT.value(), exception.getErrorMessage()));
    }



    /*
        generic exception handler helps get rid of code duplication checks for the status code
     */
    private ApiResponse<@NonNull String> genericExceptionHandler(AbstractException exception, Supplier<ApiResponse<@NonNull String>> runner){
        if(CommonUtils.isNotEmpty(exception.getStatusCode())){
            return ApiResponse.error(exception.getStatusCode(), exception.getErrorMessage());
        }
        return runner.get();
    }
}
