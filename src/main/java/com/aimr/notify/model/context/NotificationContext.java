package com.aimr.notify.model.context;

public record NotificationContext(String tenantId, boolean ignoreTenantIdInjection) {
}
