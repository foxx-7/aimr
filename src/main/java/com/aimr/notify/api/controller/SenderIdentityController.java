package com.aimr.notify.api.controller;

import com.aimr.notify.api.dto.request.CreateSenderIdentityRequest;
import com.aimr.notify.api.dto.request.UpdateSenderIdentityRequest;
import com.aimr.notify.api.dto.response.ApiResponse;
import com.aimr.notify.api.dto.response.SenderIdentityResponse;
import com.aimr.notify.service.SenderIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.aimr.notify.constant.ApplicationConstants.*;

@RestController
@RequestMapping("/api/senders")
@RequiredArgsConstructor
public class SenderIdentityController {
    private final SenderIdentityService senderIdentityService;

    @PostMapping
    public ApiResponse<SenderIdentityResponse> hcreateNewSenderIdentity(
            @RequestBody CreateSenderIdentityRequest request){
        return ApiResponse.success(HttpStatus.CREATED.value(), OBJECT_CREATED_SUCCESS_MESSAGE,
                senderIdentityService.createSenderIdentity(request));
    }

    @GetMapping
    public ApiResponse<List<SenderIdentityResponse>> getAllIdentities(){
        return ApiResponse.success(HttpStatus.CREATED.value(), OBJECT_RETRIEVAL_SUCCESS_MESSAGE,
                senderIdentityService.getAllSenderIdentities());
    }

    @PatchMapping("{id}")
    public ApiResponse<SenderIdentityResponse> updateSenderIdentity(
            @PathVariable String id,
            @RequestBody UpdateSenderIdentityRequest request){
        return ApiResponse.success(HttpStatus.OK.value(), OBJECT_UPDATED_SUCCESS_MESSAGE,
                senderIdentityService.updateSenderIdentity(id,request));
    }

    @DeleteMapping("{id}")
    public ApiResponse<Void> updateSenderIdentity(@PathVariable String id){
        senderIdentityService.deleteIdentityById(id);
        return ApiResponse.success(HttpStatus.OK.value(),
                OBJECT_DELETION_SUCCESS_MESSAGE, null);
    }

}
