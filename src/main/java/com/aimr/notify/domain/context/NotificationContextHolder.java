package com.aimr.notify.domain.context;

public final class NotificationContextHolder {

    private static final ThreadLocal<NotificationContext> NOTIFICATION_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static void setContext(NotificationContext context) {
        NOTIFICATION_CONTEXT_THREAD_LOCAL.set(context);
    }

    public static NotificationContext getContext() {
        NotificationContext context = NOTIFICATION_CONTEXT_THREAD_LOCAL.get();
        if (context == null) {
            // Provide a default empty context or handle as needed to avoid null pointer exception
            return new NotificationContext("UNKNOWN", false);
        }
        return context;
    }

    public static void clear() {
        NOTIFICATION_CONTEXT_THREAD_LOCAL.remove();
    }

    public static void ignoreTenantIdInjection() {
        NotificationContext current = getContext();
        NOTIFICATION_CONTEXT_THREAD_LOCAL.set(new NotificationContext(current.tenantId(), true));
    }

    public static void ignoreTenantIdInjection(final boolean input) {
        NotificationContext current = getContext();
        NOTIFICATION_CONTEXT_THREAD_LOCAL.set(new NotificationContext(current.tenantId(), input));
    }

}
