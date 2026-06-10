package com.aimr.notify.dao.interfaces;

import com.aimr.notify.models.entity.Tenant;

import java.util.List;
import java.util.Optional;

public interface TenantDao {


    Tenant saveTenant(Tenant tenant);

    Optional<Tenant> findTenantById(String id);


    Optional<Tenant> findTenantByNameAndOwnerId(String name, String ownerId);

    Optional<Tenant> findTenantByIdAndOwnerId(String id, String ownerId);

    void deleteTenant(Tenant tenant);

    boolean tenantExistsByName(String name);
}
