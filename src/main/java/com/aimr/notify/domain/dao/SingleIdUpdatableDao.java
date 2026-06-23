package com.aimr.notify.domain.dao;

import java.util.Optional;

public interface SingleIdUpdatableDao<T> {
    Optional<T> fetchEntity(String id);
    T saveEntity(T entity);
}