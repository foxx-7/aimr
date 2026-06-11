package com.aimr.notify.service.interfaces;

import com.aimr.notify.model.dto.response.TenantMembershipResponse;
import com.aimr.notify.model.enums.Role;
import com.aimr.notify.model.dto.request.InviteUserRequest;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {

    @Transactional
    void sendInvitation(InviteUserRequest request);

    @Transactional

    TenantMembershipResponse fetchMembershipByUserIdAndTenantId(String userId, String tenantId);

    void renounceUserMembership(String userId, String tenantId);

    void updateMembershipPrivilege(String userId, Role role);
}
