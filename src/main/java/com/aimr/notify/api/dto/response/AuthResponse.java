package com.aimr.notify.api.dto.response;

import java.util.List;

import lombok.Builder;
@Builder
public record AuthResponse(
    String token,
    String email,
    List<TenantMembershipResponse> membershipDetails
) {
}