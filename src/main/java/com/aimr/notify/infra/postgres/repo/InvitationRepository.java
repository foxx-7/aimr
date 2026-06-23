package com.aimr.notify.infra.postgres.repo;

import com.aimr.notify.domain.entity.TeamInvitation;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<@NonNull TeamInvitation, @NonNull String> {
    Optional<TeamInvitation> findByEmailAndTenantId(String email, String tenantId);
}
