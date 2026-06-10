package com.aimr.notify.controller;

import com.aimr.notify.models.dto.NotificationSummary;
import com.aimr.notify.models.enums.NotificationChannel;
import com.aimr.notify.models.enums.NotificationStatus;
import com.aimr.notify.models.enums.SummaryWindow;
import com.aimr.notify.models.dto.request.SendNotificationRequest;
import com.aimr.notify.models.dto.response.ApiResponse;
import com.aimr.notify.models.dto.response.NotificationSearchResponse;
import com.aimr.notify.service.interfaces.NotificationService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import static com.aimr.notify.constants.ApplicationConstants.OBJECT_CREATED_SUCCESS_MESSAGE;
import static com.aimr.notify.constants.ApplicationConstants.OBJECT_RETRIEVAL_SUCCESS_MESSAGE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<@NonNull String> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        //template id should be passed as request param
        String requestId = notificationService.sendNotification(request);
        return ApiResponse.success(HttpStatus.CREATED.value(), OBJECT_CREATED_SUCCESS_MESSAGE, requestId);
    }

    @GetMapping("/browse")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<@NonNull NotificationSearchResponse> browseTenantNotification(
            @RequestParam Instant anchorTime,
            @RequestParam NotificationChannel channel,
            @RequestParam NotificationStatus status,
            @RequestParam String cursor){

        return ApiResponse.success(HttpStatus.OK.value(),
                OBJECT_RETRIEVAL_SUCCESS_MESSAGE,
                notificationService.browseNotification(anchorTime, channel, status, cursor));
    }

    @GetMapping("/summarize")
    public ApiResponse<@NonNull NotificationSummary> summarizeTenantNotifications(
            @RequestParam SummaryWindow window){
        return ApiResponse.success(HttpStatus.OK.value(),
                OBJECT_RETRIEVAL_SUCCESS_MESSAGE,
                notificationService.getNotificationSummary(window));
    }
}
