package com.aimr.notify.util;

import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.domain.context.NotificationContextHolder;
import com.aimr.notify.api.dto.response.AuthenticatedUserDetails;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import com.github.f4b6a3.uuid.UuidCreator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import static com.aimr.notify.constant.ApplicationConstants.X_REQUEST_ID;

public final class CommonUtils {

    public static Instant getCurrentTimeStamp() {
        return Instant.now();
    }

    public static boolean isNotEmpty(final Object input) {
        return !ObjectUtils.isEmpty(input);
    }

    public static boolean isEmpty(final Object input) {
        return ObjectUtils.isEmpty(input);
    }

    public static String generateUUIDv4() {
        return UUID.randomUUID().toString();
    }

    public static String generateUUIDv7(){
       return UuidCreator.getTimeOrderedEpoch().toString();
    }

    public static String getCurrentTenantId() {
        return NotificationContextHolder.getContext().tenantId();
    }

    public static String getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal()
                instanceof AuthenticatedUserDetails authenticatedUserDetails) {
            return authenticatedUserDetails.userId();
        }
        return null;
    }

    public static String getCurrentTraceId() {
        return MDC.get(X_REQUEST_ID);
    }

    public static String generateSHA256Hash(String arg){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = messageDigest.digest(arg.getBytes());
            return HexFormat.of().formatHex(hashed);
        }catch (NoSuchAlgorithmException e){
            throw new ValidationException("SHA-256 not available");
        }
    }

    //@RelocationCandidate
    public static String interpolateMessageTemplate(String messageTemplate, Map<String, String> dynamicVariables) {
        String result = messageTemplate;

        for (Map.Entry<String, String> entry : dynamicVariables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
