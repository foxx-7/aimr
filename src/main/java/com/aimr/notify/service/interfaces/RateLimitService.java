package com.aimr.notify.service.interfaces;

import java.util.Map;

public interface RateLimitService {
    void checkAndGuard(String tenantId,
                       String channel,
                       String templateId,
                       Map<String, String> variables);
}
