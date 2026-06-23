package com.aimr.notify.api.controller;

import com.aimr.notify.api.dto.response.NotificationSummary;
import com.aimr.notify.api.dto.response.MongoSearchResult;
import com.aimr.notify.api.dto.response.NotificationResponse;
import com.aimr.notify.domain.enums.NotificationChannel;
import com.aimr.notify.domain.enums.NotificationStatus;
import com.aimr.notify.domain.enums.SummaryWindow;
import com.aimr.notify.api.dto.request.SendNotificationRequest;
import com.aimr.notify.api.dto.response.ApiResponse;
import com.aimr.notify.service.NotificationService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import static com.aimr.notify.constant.ApplicationConstants.OBJECT_CREATED_SUCCESS_MESSAGE;
import static com.aimr.notify.constant.ApplicationConstants.OBJECT_RETRIEVAL_SUCCESS_MESSAGE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<@NonNull String> sendSingleNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        //template id should be passed as request param
        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                OBJECT_CREATED_SUCCESS_MESSAGE,
                notificationService.sendNotification(request)
        );
    }

    @PostMapping("/broadcast")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<?> sendBroadcastNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        //template id should be passed as request param
        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                OBJECT_CREATED_SUCCESS_MESSAGE,
                notificationService.broadcastNotification(request)
        );
    }


    @GetMapping("/browse")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<@NonNull MongoSearchResult<NotificationResponse>> browseTenantNotification(
            @RequestParam Instant anchorTime,
            @RequestParam NotificationChannel channel,
            @RequestParam NotificationStatus status,
            @RequestParam String cursor){

        return ApiResponse.success(HttpStatus.OK.value(),
                OBJECT_RETRIEVAL_SUCCESS_MESSAGE,
                notificationService.browseNotification(anchorTime, channel, status, cursor)
        );
    }

    @GetMapping("/summarize")
    public ApiResponse<@NonNull NotificationSummary> summarizeTenantNotifications(
            @RequestParam SummaryWindow window){
        return ApiResponse.success(HttpStatus.OK.value(),
                OBJECT_RETRIEVAL_SUCCESS_MESSAGE,
                notificationService.getNotificationSummary(window));
    }
}
