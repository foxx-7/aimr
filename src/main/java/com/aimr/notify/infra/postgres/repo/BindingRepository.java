package com.aimr.notify.infra.postgres.repo;

import com.aimr.notify.domain.entity.Binding;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BindingRepository extends JpaRepository<@NonNull Binding, @NonNull String> {
    Optional<Binding> findByTenantIdAndName(String tenantId, String name);
    Optional<Binding> findByTenantIdAndId(String tenantId, String id);
    List<Binding> findAllByTenantId(String tenantId);
}
