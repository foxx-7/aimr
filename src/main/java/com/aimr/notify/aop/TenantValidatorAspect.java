package com.aimr.notify.aop;

import com.aimr.notify.exception.ValidationException;
<<<<<<< HEAD
import com.aimr.notify.models.entity.TenantAware;
=======
import com.aimr.notify.models.interfaces.TenantAware;
>>>>>>> a24ab90 (feat: initial testing)
import com.aimr.notify.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Aspect
@Component
@Slf4j
public class TenantValidatorAspect {

    /**
     * Intercepts methods annotated with @ValidateTenant.
     * Generically validates any object implementing TenantAware or collections of them.
     */
    @Before("@annotation(com.aimr.notify.aop.ValidateTenant)")
    public void validateTenantAccess(JoinPoint joinPoint) {
        String currentTenantId = CommonUtils.getCurrentTenantId();
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            validateObject(arg, currentTenantId);
        }
    }

    private void validateObject(Object arg, String currentTenantId) {
        if (arg instanceof TenantAware tenantAware) {
            validate(tenantAware.getId(), currentTenantId);
        } else if (arg instanceof Optional<?> opt && opt.isPresent()) {
            validateObject(opt.get(), currentTenantId);
        } else if (arg instanceof Collection<?> collection) {
            collection.forEach(item -> validateObject(item, currentTenantId));
        }
    }

    private void validate(String resourceTenantId, String currentTenantId) {
        if (resourceTenantId != null && !resourceTenantId.equals(currentTenantId)) {
            log.error("Security Breach Attempt: Tenant {} tried to access resource belonging to Tenant {}", 
                      currentTenantId, resourceTenantId);
            throw new ValidationException("Access Denied: Resource belongs to another tenant", HttpStatus.FORBIDDEN.value());
        }
    }
}
