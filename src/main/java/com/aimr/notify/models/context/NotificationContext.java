package com.aimr.notify.models.context;

public record NotificationContext(String tenantId, boolean ignoreTenantIdInjection) {
}
