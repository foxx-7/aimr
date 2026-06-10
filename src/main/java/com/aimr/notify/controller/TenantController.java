package com.aimr.notify.controller;

import com.aimr.notify.models.dto.request.RegisterTenantRequest;
import com.aimr.notify.models.dto.request.InviteUserRequest;
import com.aimr.notify.models.dto.response.ApiResponse;
import com.aimr.notify.models.dto.response.TenantMembershipResponse;
import com.aimr.notify.models.dto.response.TenantResponse;
import com.aimr.notify.service.interfaces.TenantService;
import com.aimr.notify.service.interfaces.UserService;
import com.aimr.notify.models.dto.response.AuthenticatedUserDetails;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.aimr.notify.constants.ApplicationConstants.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final UserService userService;

   @GetMapping("/debug")
   public ApiResponse<Object> debug(){
       return ApiResponse.success(HttpStatus.OK.value(),
               OBJECT_RETRIEVAL_SUCCESS_MESSAGE,null);
   }

    @PostMapping("/tenants")
    public ApiResponse<@NonNull TenantResponse> registerNewTenant (
            @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails ,
            @Valid @RequestBody final RegisterTenantRequest request){

        return ApiResponse.success(HttpStatus.CREATED.value(),
                OBJECT_CREATED_SUCCESS_MESSAGE, tenantService.
                        registerTenant(authenticatedUserDetails.getUserId(),request));
    }

    @PatchMapping("/tenants")
    public ApiResponse<@NonNull TenantResponse> updateTenant(
            @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails,
            @RequestHeader("x-tenant-id") String tenantId,
            @RequestBody Map<String , Object> tenantUpdate){
        return ApiResponse.success(HttpStatus.OK.value(), OBJECT_UPDATED_SUCCESS_MESSAGE
                , tenantService.updateTenant(tenantId, authenticatedUserDetails.getUserId(), tenantUpdate));
    }

    @DeleteMapping("/tenants")
    public ApiResponse<?> deleteTenant(
            @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails,
            @RequestHeader("x-tenant-id") String tenantId){
        tenantService.deleteTenant(tenantId, authenticatedUserDetails.getUserId());
        return ApiResponse.success(HttpStatus.NO_CONTENT.value(),
                OBJECT_DELETION_SUCCESS_MESSAGE, null);
    }

    @PostMapping("/tenants/memberships")
    public ApiResponse<?> inviteUser(@RequestBody InviteUserRequest request){
        userService.sendInvitation(request);
        return ApiResponse.success(HttpStatus.CREATED.value(),
                OBJECT_CREATED_SUCCESS_MESSAGE, null);
    }

    //admin & users privilege
    @GetMapping("/tenants/memberships")
    public ApiResponse<TenantMembershipResponse> getUserMembershipDetails(
            @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails,
            @RequestHeader("x-tenant-id") String tenantId){
        return ApiResponse.success(HttpStatus.OK.value(),
                OBJECT_RETRIEVAL_SUCCESS_MESSAGE,
                userService.fetchMembershipByUserIdAndTenantId(authenticatedUserDetails.getUserId(), tenantId));
    }

    @DeleteMapping("/tenants/memberships")
    public ApiResponse<?> renounceMembership(@RequestHeader("x-tenant-id")
                                     String tenantId, @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){
        userService.renounceUserMembership(authenticatedUserDetails.getUserId(), tenantId);
        return ApiResponse.success(HttpStatus.NO_CONTENT.value(),
                OBJECT_DELETION_SUCCESS_MESSAGE, null);
    }
    //also add terminate membership endpoint for admins only
}
