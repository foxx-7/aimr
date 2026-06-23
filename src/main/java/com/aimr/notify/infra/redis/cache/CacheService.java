package com.aimr.notify.infra.redis.cache;

import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.domain.entity.annotations.CachePrefix;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

import static com.aimr.notify.constant.ErrorConstants.CACHE_PARSING_ERROR;
import static com.aimr.notify.constant.ErrorConstants.PUT_CACHING_ERROR;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JsonMapper jsonMapper;

    public <T> void putById(final String keySuffix, final String id, T value) {
        put(keySuffix, "BY_ID:".concat(id), value);
    }

    public <T> void putByName(final String keySuffix, final String name, T value) {
        put(keySuffix, "BY_NAME:".concat(name), value);
    }

    public <T> void deleteById(final String keySuffix, final String id, Class<T> clazz) {
        delete(keySuffix, "BY_ID:".concat(id), clazz);
    }

    public <T> void deleteByName(final String keySuffix, final String name, Class<T> clazz) {
        delete(keySuffix, "BY_NAME:".concat(name), clazz);
    }

    public <T> Optional<T> getById(final String keySuffix, final String id, Class<T> clazz) {
        return get(keySuffix, "BY_ID:".concat(id), clazz);
    }

    public <T> Optional<T> getByName(final String keySuffix, final String name, Class<T> clazz) { // fixed param name
        return get(keySuffix, "BY_NAME:".concat(name), clazz);
    }

    /**
     * Bulk deletes all cached items for a specific tenant and entity type.
     * Uses a pattern match (e.g. "TEMPLATE.tenant123:*") to find and remove the flat keys.
     */
    public <T> void deleteAll(final String keySuffix, Class<T> clazz) {
        CachePrefix annotation = requireCachePrefix(clazz);
        String pattern = annotation.value() + keySuffix + ":*";
        java.util.Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private <T> Optional<T> get(final String keySuffix, final String identifier, Class<T> clazz) {
        CachePrefix annotation = requireCachePrefix(clazz);
        String redisKey = annotation.value() + keySuffix + ":" + identifier;

        String jsonData = redisTemplate.opsForValue().get(redisKey);
        if (jsonData == null) return Optional.empty();

        try {
            return Optional.ofNullable(jsonMapper.readValue(jsonData, clazz));
        } catch (Exception e) {
            throw new ValidationException(CACHE_PARSING_ERROR, HttpStatus.BAD_REQUEST.value());
        }
    }

    private <T> void delete(final String keySuffix, final String identifier, Class<T> clazz) {
        CachePrefix annotation = requireCachePrefix(clazz);
        String redisKey = annotation.value() + keySuffix + ":" + identifier;
        redisTemplate.delete(redisKey);
    }

    private <T> void put(final String keySuffix, final String identifier, final T data) {
        CachePrefix annotation = requireCachePrefix(data.getClass());
        String redisKey = annotation.value() + keySuffix + ":" + identifier;

        try {
            String json = jsonMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(redisKey, json, Duration.ofMinutes(annotation.cacheTTL()));
        } catch (Exception e) {
            throw new ValidationException(PUT_CACHING_ERROR, HttpStatus.BAD_REQUEST.value());
        }
    }

    private CachePrefix requireCachePrefix(Class<?> clazz) {
        CachePrefix annotation = clazz.getAnnotation(CachePrefix.class);
        if (annotation == null) {
            throw new ValidationException(
                    clazz.getSimpleName() + " is missing @CachePrefix annotation"
            );
        }
        return annotation;
    }
}
