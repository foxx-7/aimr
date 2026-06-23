package com.aimr.notify.consumer.channel.email.provider.impl;

import com.aimr.notify.exception.DataTransportException;
import com.aimr.notify.consumer.dto.EmailDispatchDto;
import com.aimr.notify.consumer.channel.email.provider.interfaces.PrimaryEmailProvider;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenericPrimaryEmailProvider {

    private final List<PrimaryEmailProvider> providers;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public String sendEmail(EmailDispatchDto dto) {
        String tenantId = dto.getTenantId();
        String requestId = dto.getRequestId();

        for (PrimaryEmailProvider provider : providers) {
            // FIX: Isolate the circuit breaker per provider per tenant
            String tenantCbKey = provider.getName() + "_" + tenantId;
            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(tenantCbKey);

            try {
                String mailId = cb.executeSupplier(() -> provider.deliver(dto));
                if (mailId != null && !mailId.isEmpty()) {
                    log.info("[GenericPrimaryEmailProvider] Delivered via {} for tenant={}, requestId={}", 
                            provider.getName(), tenantId, requestId);
                    return mailId;
                }
            } catch (CallNotPermittedException e) {
                log.warn("[GenericPrimaryEmailProvider] Circuit breaker '{}' is OPEN. Skipping provider '{}' for tenant={}", 
                        tenantCbKey, provider.getName(), tenantId);
            } catch (DataTransportException e) {
                log.error("[GenericPrimaryEmailProvider] Provider '{}' failed transit for tenant={}, requestId={}. Error: {}", 
                        provider.getName(), tenantId, requestId, e.getMessage());
            } catch (Exception e) {
                log.error("[GenericPrimaryEmailProvider] Unexpected failure in provider '{}' for tenant={}, requestId={}", 
                        provider.getName(), tenantId, requestId, e);
            }
        }
        throw new DataTransportException("All primary email providers were exhausted or tripped for tenant: " + tenantId);
    }
}