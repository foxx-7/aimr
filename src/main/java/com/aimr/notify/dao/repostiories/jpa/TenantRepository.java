package com.aimr.notify.dao.repostiories.jpa;

import com.aimr.notify.model.entity.Tenant;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<@NonNull Tenant, @NonNull String> {
    Optional<Tenant> findByIdAndOwnerId(String id, String ownerId);
    Optional<Tenant> findByNameAndOwnerId(String name, String ownerId);
    boolean existsByName(String name);
}
