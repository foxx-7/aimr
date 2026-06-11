package com.aimr.notify.config;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Configuration
public class RateLimitConfig {

    /**
     * Atomic Lua script that performs both dedup check and rate limit increment
     * in a single Redis round trip.
     * <p>
     * KEYS:
     *   KEYS[1] = dedup key     (notif:dedup:{tenantId}:{channel}:{hash})
     *   KEYS[2] = rate limit key (notif:rl:{tenantId}:{channel}:{hourWindow})
     * <p>
     * ARGV:
     *   ARGV[1] = rate limit max (int)
     *   ARGV[2] = dedup TTL in seconds (int)
     *   ARGV[3] = rate limit window TTL in seconds (int)
     * <p>
     * Returns List<Long> [status, count]:
     *   [ 1,  count] = OK — request is clean, proceed
     *   [ 0,   -1  ] = DUPLICATE — dedup key already exists
     *   [-1,  count] = RATE LIMITED — counter exceeded max
     */
    @Bean
    @SuppressWarnings("unchecked")
    public RedisScript<@NonNull List<Long>> notificationGuardScript() {
        String script = """
                local dup_key = KEYS[1]
                local rl_key  = KEYS[2]
                local rl_max  = tonumber(ARGV[1])
                local dup_ttl = tonumber(ARGV[2])
                local rl_ttl  = tonumber(ARGV[3])
                               \s
                -- Attempt atomic dedup: SET key only if it does NOT exist (NX)
                -- If the key already exists, SET returns nil (false) → duplicate detected
                local set_result = redis.call('SET', dup_key, '1', 'NX', 'EX', dup_ttl)
                if not set_result then
                    return {0, -1}
                end
                               \s
                -- Increment rate limit counter for tenant+channel+hour window
                local count = redis.call('INCR', rl_key)
                               \s
                -- Set TTL only on first increment (avoids overwriting a running expiry)
                if count == 1 then
                    redis.call('EXPIRE', rl_key, rl_ttl)
                end
                               \s
                -- If limit exceeded, rollback the dedup key so the slot isn't consumed
                if count > rl_max then
                    redis.call('DEL', dup_key)
                    return {-1, count}
                end
                               \s
                return {1, count}
               \s""";

        return RedisScript.of(script, (Class<List<Long>>) (Class<?>) List.class);
    }
}
