package com.aimr.notify.infra.postgres.repo;

import com.aimr.notify.domain.entity.RefreshToken;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<@NonNull RefreshToken, @NonNull String> {
    List<RefreshToken> findAllByUserIdAndRevokedFalse(String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken t WHERE t.revoked = true AND t.createdAt < :cutoff")
    void deleteRevokedBefore(@Param("cutoff") Instant cutoff);
}
