package com.aimr.notify.infra.postgres.dao;

import com.aimr.notify.infra.postgres.repo.RefreshTokenRepository;
import com.aimr.notify.domain.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RefreshTokenDao {
    private final RefreshTokenRepository refreshTokenRepository;

    public List<RefreshToken> fetchToken(final String userId){
        return refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId);
    }

    public void saveToken(final RefreshToken refreshToken){
        refreshTokenRepository.save(refreshToken);
    }

    public void deleteRevokedBefore(Instant cutoff) {
        refreshTokenRepository.deleteRevokedBefore(cutoff);
    }
}
