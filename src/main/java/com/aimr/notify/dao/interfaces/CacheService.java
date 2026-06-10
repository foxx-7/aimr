package com.aimr.notify.dao.interfaces;

import java.util.Optional;

public interface CacheService {

    <T> void putById(String tenantId, String id, T value);

    <T> void putByName(String tenantId, String name, T value);

    <T> void deleteById(String prefix, String id, Class<T> clazz);

    <T> void deleteByName(String prefix, String name, Class<T> clazz);

    <T> Optional<T> getById(String prefix, String id, Class<T> clazz);

    <T> Optional<T> getByName(String tenantId, String name, Class<T> clazz);
}
