package com.aimr.notify.dao.impl;

import com.aimr.notify.dao.interfaces.InvitationDao;
import com.aimr.notify.dao.repostiories.jpa.InvitationRepository;
import com.aimr.notify.models.entity.Invitation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InvitationDaoImpl implements InvitationDao {
    private final InvitationRepository invitationRepository;

    @Override
    public Optional<Invitation> findInvitationByEmailAndTenantId(final String email, final String tenantId){
        return invitationRepository.findByEmailAndTenantId(email, tenantId);
    }

    @Override
    public void saveInvitation(final Invitation invitation){
        invitationRepository.save(invitation);
    }

    @Override
    public void deleteInvitation(final Invitation invitation){
        invitationRepository.delete(invitation);
    }
}
