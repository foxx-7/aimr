package com.aimr.notify.api.dto.response;

import com.aimr.notify.domain.entity.Binding;
import java.time.Instant;

import lombok.Builder;
@Builder
public record BindingResponse(
    String id,
    String tenantId,
    String name,
    String bindingAddress,
    Instant createdAt,
    Instant updatedAt
) {

    public static BindingResponse from(Binding binding) {
        return new BindingResponse(
            binding.getId(),
            binding.getTenantId(),
            binding.getName(),
            binding.getBindingAddress(),
            binding.getCreatedAt(),
            binding.getUpdatedAt()
        );
    }
}