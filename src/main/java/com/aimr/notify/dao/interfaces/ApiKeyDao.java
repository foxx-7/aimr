package com.aimr.notify.dao.interfaces;

public interface ApiKeyDao {
    void saveNewApiKey(String name, String prefix, String tenantId, String userId, String hashedKey);
}
