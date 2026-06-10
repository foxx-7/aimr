package com.aimr.notify.service.interfaces;

import com.aimr.notify.models.dto.response.TenantMembershipResponse;
import com.aimr.notify.models.enums.Role;
import com.aimr.notify.models.dto.request.InviteUserRequest;
import com.aimr.notify.models.dto.response.UserResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public interface UserService {

    @Transactional
    void sendInvitation(InviteUserRequest request);

    @Transactional

    TenantMembershipResponse fetchMembershipByUserIdAndTenantId(String userId, String tenantId);

    void renounceUserMembership(String userId, String tenantId);

    void updateMembershipPrivilege(String userId, Role role);
}
