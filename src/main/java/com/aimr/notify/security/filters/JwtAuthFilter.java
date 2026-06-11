package com.aimr.notify.security.filters;

import com.aimr.notify.security.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.aimr.notify.model.dto.response.AuthenticatedUserDetails;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer=")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        String email;

        try {
            email = jwtUtil.extractUsername(token);
        } catch (ExpiredJwtException e) {
            log.warn("[JwtAuthFilter] JWT expired: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return; // Stop processing the filter chain
        } catch (JwtException e) {
            log.warn("[JwtAuthFilter]Invalid JWT: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return; // Stop processing the filter chain
        }

        //maybe since default is always set to anonymous user the security contexts's
        // authentication is never null so this our if function never executes
        if (email != null) {
            AuthenticatedUserDetails authenticatedUserDetails =
                    (AuthenticatedUserDetails) userDetailsService.loadUserByUsername(email);

            if (jwtUtil.isTokenValid(token, authenticatedUserDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUserDetails,
                                null,
                                authenticatedUserDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
                /*
                    create new security context instead of using SecurityContextHolder.getContext()
                    to avoid race conditions across multiple threads that might try accessing the
                     current security context at the same time;
                 */
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                //set new context authentication to authToken
                securityContext.setAuthentication(authToken);

                //save new context to SecurityContextHolder's thread locale
                SecurityContextHolder.setContext(securityContext);
            }
        }

        filterChain.doFilter(request, response); // Continue processing the filter chain
    }
}