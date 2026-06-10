package com.aimr.notify.dao.interfaces;

import com.aimr.notify.models.entity.Invitation;

import java.util.Optional;

public interface InvitationDao {
    public abstract Optional<Invitation> findInvitationByEmailAndTenantId(String email, String tenantId);

    void saveInvitation(Invitation invitation);

    void deleteInvitation(Invitation invitation);
}
