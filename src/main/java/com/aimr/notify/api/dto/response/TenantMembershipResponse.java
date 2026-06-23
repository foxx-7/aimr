package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.entity.TenantMembership;
import com.aimr.notify.domain.enums.Role;
import java.time.Instant;

public record TenantMembershipResponse(
    String UserId,
    String membershipName,
    Role membershipRole,
    Instant joinedAt,
    Instant updatedAt
) {
    public static TenantMembershipResponse from(TenantMembership membership) {
        return new TenantMembershipResponse(
                membership.getUserId(),
                membership.getDisplayName(),
                membership.getRole(),
                membership.getJoinedAt(), membership.getUpdatedAt());
    }
}