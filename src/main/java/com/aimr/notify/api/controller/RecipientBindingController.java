package com.aimr.notify.api.controller;

import com.aimr.notify.api.dto.request.CreateBindingRequest;
import com.aimr.notify.api.dto.request.UpdateBindingRequest;
import com.aimr.notify.api.dto.response.ApiResponse;
import com.aimr.notify.api.dto.response.BindingResponse;
import com.aimr.notify.service.RecipientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.aimr.notify.constant.ApplicationConstants.*;

@RestController
@RequestMapping("/api/v1/recipients")
@RequiredArgsConstructor
public class RecipientBindingController {
    private final RecipientService recipientService;

    @PostMapping
    public ApiResponse<BindingResponse> createBinding(@Valid @RequestBody CreateBindingRequest request){
        return ApiResponse.success(HttpStatus.CREATED.value(), OBJECT_CREATED_SUCCESS_MESSAGE,
                recipientService.createNewRecipientBinding(request));
    }

    @GetMapping()
    public ApiResponse<List<BindingResponse>> getAllRecipientBindings(){
        return ApiResponse.success(HttpStatus.CREATED.value(), OBJECT_RETRIEVAL_SUCCESS_MESSAGE,
                recipientService.getAllBindings());
    }

    @PatchMapping("{id}")
    public ApiResponse<BindingResponse> updateRecipientBinding(
            @PathVariable String id,
            @RequestBody UpdateBindingRequest request
    ){
        return ApiResponse.success(HttpStatus.OK.value(), OBJECT_UPDATED_SUCCESS_MESSAGE,
                recipientService.updateBinding(id,request));
    }

    @DeleteMapping
    public ApiResponse<Void> deleteRecipientBinding(@RequestParam String id){
        recipientService.deleteBindingById(id);
        return ApiResponse.success(HttpStatus.OK.value(),
                OBJECT_DELETION_SUCCESS_MESSAGE,null);
    }
}
