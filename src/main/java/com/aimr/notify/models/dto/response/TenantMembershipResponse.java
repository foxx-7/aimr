package com.aimr.notify.models.dto.response;

import com.aimr.notify.models.entity.TenantMembership;
import com.aimr.notify.models.enums.Role;
import lombok.Data;

import java.time.Instant;

@Data
public class TenantMembershipResponse {
    private String UserId;
    private String membershipName;
    private Role membershipRole;
    private Instant joinedAt;
    private Instant updatedAt;

    public TenantMembershipResponse(final TenantMembership membership){
        setUserId(membership.getUserId());
        setMembershipName(membership.getDisplayName());
        setMembershipRole(membership.getRole());
        setJoinedAt(membership.getJoinedAt());
        setUpdatedAt(membership.getUpdatedAt());
    }
}
