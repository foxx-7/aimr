package com.aimr.notify.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailDispatchDto {
    //actual fields required
    private String tenantId;
    private String requestId;
    private String senderAddress;//sender email
    private String senderName;
    private String recipientAddress;//recipient email
    private String subject;
    private String message;
}
