package com.aimr.notify.dao.impl;

import com.aimr.notify.dao.interfaces.CacheService;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.model.entity.annotations.CachePrefix;
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
class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JsonMapper jsonMapper;

    @Override
    public <T> void putById(final String keySuffix, final String id, T value) {
        put(keySuffix, "BY_ID:".concat(id), value);
    }

    @Override
    public <T> void putByName(final String keySuffix, final String name, T value) {
        put(keySuffix, "BY_NAME:".concat(name), value);
    }

    @Override
    public <T> void deleteById(final String keySuffix, final String id, Class<T> clazz) {
        delete(keySuffix, "BY_ID:".concat(id), clazz);
    }

    @Override
    public <T> void deleteByName(final String keySuffix, final String name, Class<T> clazz) {
        delete(keySuffix, "BY_NAME:".concat(name), clazz);
    }

    @Override
    public <T> Optional<T> getById(final String keySuffix, final String id, Class<T> clazz) {
        return get(keySuffix, "BY_ID:".concat(id), clazz);
    }

    @Override
    public <T> Optional<T> getByName(final String keySuffix, final String name, Class<T> clazz) { // fixed param name
        return get(keySuffix, "BY_NAME:".concat(name), clazz);
    }

    private <T> Optional<T> get(final String keySuffix, final String hashKey, Class<T> clazz) {
        CachePrefix annotation = requireCachePrefix(clazz);
        String primaryKey = annotation.value().concat(keySuffix);

        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        String jsonData = ops.get(primaryKey, hashKey);
        if (jsonData == null) return Optional.empty();

        try {
            return Optional.ofNullable(jsonMapper.readValue(jsonData, clazz));
        } catch (Exception e) {
            throw new ValidationException(CACHE_PARSING_ERROR, HttpStatus.BAD_REQUEST.value());
        }
    }

    private <T> void delete(final String keySuffix, final String hashKey, Class<T> clazz) {
        CachePrefix annotation = requireCachePrefix(clazz);
        String primaryKey = annotation.value().concat(keySuffix);
        redisTemplate.opsForHash().delete(primaryKey, hashKey);
    }

    private <T> void put(final String keySuffix, final String hashKey, final T data) {
        CachePrefix annotation = requireCachePrefix(data.getClass());
        String primaryKey = annotation.value().concat(keySuffix);

        try {
            String json = jsonMapper.writeValueAsString(data);
            redisTemplate.opsForHash().put(primaryKey, hashKey, json);
            redisTemplate.expire(primaryKey, Duration.ofMinutes(annotation.cacheTTL()));
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
