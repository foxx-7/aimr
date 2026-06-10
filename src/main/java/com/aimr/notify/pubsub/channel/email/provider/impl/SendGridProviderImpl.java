package com.aimr.notify.pubsub.channel.email.provider.impl;

import com.aimr.notify.exception.NotificationDispatchException;
import com.aimr.notify.models.dto.EmailDispatchDto;
import com.aimr.notify.models.enums.NotificationStatus;
import com.aimr.notify.pubsub.channel.email.provider.interfaces.SendGridProvider;
import com.aimr.notify.service.interfaces.NotificationService;
import com.aimr.notify.util.CommonUtils;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendGridProviderImpl implements SendGridProvider {
    private final SendGrid sendGrid;
    private final NotificationService notificationService;

    @Override
    public String sendEmail(final EmailDispatchDto emailDispatchDto) {
        
        String tenantId=emailDispatchDto.getTenantId();
        String requestId=emailDispatchDto.getRequestId();

        Mail mail = getMail(emailDispatchDto, tenantId, requestId);

        Response response;
        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            response = sendGrid.api(request);

            if (response.getStatusCode() >= 300) {
                log.error("[Tenant: {}] SendGrid failed - status: {}, body: {}",
                        tenantId, response.getStatusCode(), response.getBody());
                throw new NotificationDispatchException("SendGrid rejected the request: " + response.getStatusCode());
            }


            String mailId = CommonUtils.generateSHA256Hash(tenantId.concat(requestId));//generate message id and return to caller
            notificationService.markNotificationStatus(tenantId, requestId, mailId, NotificationStatus.DELIVERED);

            log.info("[Tenant: {}] Email sent to {} | notificationId: {} | mailId: {}",
                    tenantId, emailDispatchDto.getFrom(), requestId, mailId);

            return mailId;
        } catch (IOException e) {
            log.error("error encountered while sending email for notification with id: {}", requestId);
            throw new RuntimeException("Failed to reach SendGrid", e);
        }
    }

    private static Mail getMail(final EmailDispatchDto emailDispatchDto, final String tenantId, final String requestId) {
        Email sender = new Email(emailDispatchDto.getFrom(), emailDispatchDto.getSender());
        Email recipient = new Email(emailDispatchDto.getTo(), emailDispatchDto.getRecipient());
        Content content = new Content("text/html", emailDispatchDto.getMessage());
        Mail mail = new Mail(sender, emailDispatchDto.getSubject(), recipient, content);

        // for tracking purposes
        mail.addCustomArg("tenant_id", tenantId);
        mail.addCustomArg("requestId", requestId);
        return mail;
    }
}
