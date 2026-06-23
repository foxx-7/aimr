package com.aimr.notify.api.controller;

import com.aimr.notify.api.dto.request.RegisterTenantRequest;
import com.aimr.notify.api.dto.request.InviteUserRequest;
import com.aimr.notify.api.dto.request.UpdateTenantRequest;
import com.aimr.notify.api.dto.response.ApiResponse;
import com.aimr.notify.api.dto.response.TenantMembershipResponse;
import com.aimr.notify.api.dto.response.TenantResponse;
import com.aimr.notify.api.dto.response.AuthenticatedUserDetails;
import com.aimr.notify.service.TenantService;
import com.aimr.notify.service.UserService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.aimr.notify.constant.ApplicationConstants.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final UserService userService;

   @GetMapping("/debug")
   public ApiResponse<Object> debug(){
       return ApiResponse.success(HttpStatus.OK.value(),
               OBJECT_RETRIEVAL_SUCCESS_MESSAGE,null);
   }

    @PostMapping("/register")
    public ApiResponse<@NonNull TenantResponse> registerNewTenant (
            @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails ,
            @Valid @RequestBody final RegisterTenantRequest request){

        return ApiResponse.success(HttpStatus.CREATED.value(),
                OBJECT_CREATED_SUCCESS_MESSAGE, tenantService.
                        registerTenant(authenticatedUserDetails.userId(),request));
    }

    @PatchMapping
    public ApiResponse<@NonNull TenantResponse> updateTenant(
            @RequestHeader("x-tenant-id") String tenantId,
            @RequestBody UpdateTenantRequest tenantUpdate){
        return ApiResponse.success(HttpStatus.OK.value(), OBJECT_UPDATED_SUCCESS_MESSAGE
                , tenantService.updateTenant(tenantUpdate));
    }

    @DeleteMapping
    public ApiResponse<?> deleteTenant(
            @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails,
            @RequestHeader("x-tenant-id") String tenantId){
        tenantService.deleteTenant(tenantId, authenticatedUserDetails.userId());
        return ApiResponse.success(HttpStatus.NO_CONTENT.value(),
                OBJECT_DELETION_SUCCESS_MESSAGE, null);
    }

    @PostMapping("/memberships")
    public ApiResponse<?> inviteUser(@RequestBody InviteUserRequest request){
        userService.sendInvitation(request);
        return ApiResponse.success(HttpStatus.CREATED.value(),
                OBJECT_CREATED_SUCCESS_MESSAGE, null);
    }

    //admin & users privilege
    @GetMapping("/memberships")
    public ApiResponse<TenantMembershipResponse> getUserMembershipDetails(
            @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails,
            @RequestHeader("x-tenant-id") String tenantId){
        return ApiResponse.success(HttpStatus.OK.value(),
                OBJECT_RETRIEVAL_SUCCESS_MESSAGE,
                userService.loadMembershipByUserIdAndTenantId(authenticatedUserDetails.userId(), tenantId));
    }

    @DeleteMapping("/memberships")
    public ApiResponse<?> renounceMembership(@RequestHeader("x-tenant-id")
                                     String tenantId, @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){
        userService.renounceUserMembership(authenticatedUserDetails.userId(), tenantId);
        return ApiResponse.success(HttpStatus.NO_CONTENT.value(),
                OBJECT_DELETION_SUCCESS_MESSAGE, null);
    }
    //also add terminate membership endpoint for admins only
}
