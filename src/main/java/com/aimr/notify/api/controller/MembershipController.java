package com.aimr.notify.api.controller;

import com.aimr.notify.api.dto.request.InviteUserRequest;
import com.aimr.notify.api.dto.response.ApiResponse;
import com.aimr.notify.api.dto.response.AuthenticatedUserDetails;
import com.aimr.notify.api.dto.response.TenantMembershipResponse;
import com.aimr.notify.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.aimr.notify.constant.ApplicationConstants.*;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {
    private final UserService userService;

    @PostMapping
    public ApiResponse<?> inviteUser(@RequestBody InviteUserRequest request){
        userService.sendInvitation(request);
        return ApiResponse.success(HttpStatus.CREATED.value(),
                OBJECT_CREATED_SUCCESS_MESSAGE, null);
    }

    @PostMapping("/accept")
    public ApiResponse<?> acceptInvitation(){
        return ApiResponse.success(HttpStatus.OK.value(),
                OBJECT_CREATED_SUCCESS_MESSAGE, null);
    }

    //admin & users privilege
    @GetMapping
    public ApiResponse<TenantMembershipResponse> getUserMembershipDetails(
            @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails,
            @RequestHeader("x-tenant-id") String tenantId){
        return ApiResponse.success(HttpStatus.OK.value(),
                OBJECT_RETRIEVAL_SUCCESS_MESSAGE,
                userService.loadMembershipByUserIdAndTenantId(authenticatedUserDetails.userId(), tenantId));
    }

    @DeleteMapping
    public ApiResponse<?> renounceMembership(@RequestHeader("x-tenant-id")
                                             String tenantId, @AuthenticationPrincipal AuthenticatedUserDetails authenticatedUserDetails){
        userService.renounceUserMembership(authenticatedUserDetails.userId(), tenantId);
        return ApiResponse.success(HttpStatus.NO_CONTENT.value(),
                OBJECT_DELETION_SUCCESS_MESSAGE, null);
    }
}
