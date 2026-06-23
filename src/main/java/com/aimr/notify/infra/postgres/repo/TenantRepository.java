package com.aimr.notify.infra.postgres.repo;

import com.aimr.notify.domain.entity.Tenant;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<@NonNull Tenant, @NonNull String> {
    Optional<Tenant> findByIdAndOwnerId(String id, String ownerId);
    Optional<Tenant> findByNameAndOwnerId(String name, String ownerId);
    List<Tenant> findAllByOwnerId(String ownerId);
    boolean existsByName(String name);
}
