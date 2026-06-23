package com.aimr.notify.infra.postgres.dao;

import com.aimr.notify.infra.postgres.repo.InvitationRepository;
import com.aimr.notify.domain.entity.TeamInvitation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InvitationDao {
    private final InvitationRepository invitationRepository;

    public Optional<TeamInvitation> fetchInvitationByEmailAndTenantId(final String email, final String tenantId){
        return invitationRepository.findByEmailAndTenantId(email, tenantId);
    }

    public void saveInvitation(final TeamInvitation teamInvitation){
        invitationRepository.save(teamInvitation);
    }

    public void deleteInvitation(final TeamInvitation teamInvitation){
        invitationRepository.delete(teamInvitation);
    }
}
