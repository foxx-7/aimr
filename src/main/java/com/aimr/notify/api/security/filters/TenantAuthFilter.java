package com.aimr.notify.api.security.filters;

import com.aimr.notify.domain.context.NotificationContext;
import com.aimr.notify.domain.context.NotificationContextHolder;
import com.aimr.notify.util.CommonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;


import org.slf4j.MDC;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.aimr.notify.constant.ApplicationConstants.*;


@Component
@Slf4j
public class TenantAuthFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher=new AntPathMatcher();
   private final List<String> NO_FILTER_ALLOWED_PATHS=List.of(AUTH_V1_PATHS, REGISTER_TENANT_V1_PATH);

   @Override
   protected boolean shouldNotFilter(HttpServletRequest request){
       final String path=request.getServletPath();
       boolean skip=NO_FILTER_ALLOWED_PATHS.stream().anyMatch(pattern-> pathMatcher.match(pattern , path));
       log.debug("TenantAuthFilter path: '{}', skipping: {}", path, skip);
       return skip;
   }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(X_REQUEST_ID);
        if (CommonUtils.isEmpty(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(X_REQUEST_ID, requestId);
        response.setHeader(X_REQUEST_ID, requestId);

        boolean isValidApi = isValidAPI(request.getRequestURI());
        try {
            if (isValidApi) {
                String xTenantId = request.getHeader(X_TENANT_ID);

                if (CommonUtils.isEmpty(xTenantId)) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write("UnAuthorized! Tenant Identity Is Required");
                    return;
                }
                NotificationContextHolder.setContext(new NotificationContext(xTenantId, false));
            }
            filterChain.doFilter(request, response);
        } finally {
            NotificationContextHolder.clear();
            MDC.clear();
        }
    }

    static boolean isValidAPI(final String apiPath) {
        return apiPath.startsWith("/api");
    }
}
