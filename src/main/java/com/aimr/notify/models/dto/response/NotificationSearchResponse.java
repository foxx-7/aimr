package com.aimr.notify.models.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationSearchResponse {

    //current batch size is 50
    private List<NotificationResponse> notifications;

    /*
        opaque Base64 string source parses back as cursor on the next request
        null when hasNext is false(no further pages exist)
     */
    private String nextCursor;

    //true if more results exist beyond this batch within the 24 hour window
    private boolean hasMore;
}
