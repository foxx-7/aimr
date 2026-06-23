package com.aimr.notify.domain.dao;

import java.util.Optional;

public interface UpdatableDao<T> {
    Optional<T> fetchEntity(String tenantId, String id);
    T saveEntity(T entity);
}