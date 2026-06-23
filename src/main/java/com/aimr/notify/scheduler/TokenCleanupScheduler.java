package com.aimr.notify.scheduler;

import com.aimr.notify.infra.postgres.dao.RefreshTokenDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenDao refreshTokenDao;

    // Runs at 3:00 AM every day
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeRevokedTokens() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        log.info("[TokenCleanup] Purging revoked refresh tokens older than {}", cutoff);
        refreshTokenDao.deleteRevokedBefore(cutoff);
    }
}
