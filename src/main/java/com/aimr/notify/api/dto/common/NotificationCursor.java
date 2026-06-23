package com.aimr.notify.api.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCursor {
    /*
        createdAt of last item returned from last request
        used as the primary keyset position in the next fetch query
     */
    private Instant createdAt;

    /*
        id of last item returned
        used as tie-breaker when multiple documents have the same createdAt
     */
    private String id;
}
