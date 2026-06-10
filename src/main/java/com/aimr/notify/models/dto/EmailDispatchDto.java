package com.aimr.notify.models.dto;

import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.util.CommonUtils;
import lombok.Data;

import java.util.Map;

@Data
public class EmailDispatchDto {
    //actual fields required
    private String tenantId;
    private String requestId;
    private String from;//sender email
    private String sender;
    private String to;//recipient email
    private String recipient;
    private String subject;
    private String message;

    public EmailDispatchDto(ChannelDispatchDTO dispatchDTO){

        Map<String,String> variables=dispatchDTO.getDynamicVariables();

        variables.forEach((key,value)->{
            switch (key){
                case "FROM"->setFrom(value);
                case "SENDER"-> setSender(value);
                case "TO"-> setTo(value);
                case "RECIPIENT"->setRecipient(value);
                default -> {}
            }
        });

        if(getFrom().isBlank() || getTo().isBlank() ||
                getSender().isBlank()){
            throw new ValidationException("name and email fields for sender and recipient are required");
        }

        String interpolatedMessageTemplate = CommonUtils.interpolateMessageTemplate(dispatchDTO
                .getMessage(),variables);

        setTenantId(dispatchDTO.getTenantId());
        setRequestId(dispatchDTO.getRequestId());
        setSubject(dispatchDTO.getSubject());
        setMessage(interpolatedMessageTemplate);
    }
}
