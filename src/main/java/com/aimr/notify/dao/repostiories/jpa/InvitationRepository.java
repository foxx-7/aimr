package com.aimr.notify.dao.repostiories.jpa;

import com.aimr.notify.models.entity.Invitation;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<@NonNull Invitation, @NonNull String> {
    Optional<Invitation> findByEmailAndTenantId(String email, String tenantId);
}
