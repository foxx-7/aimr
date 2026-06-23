package com.aimr.notify.domain.context;

public record NotificationContext(String tenantId, boolean ignoreTenantIdInjection) {
}
