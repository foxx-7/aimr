package com.aimr.notify.models.dto.request;

import com.aimr.notify.util.CommonUtils;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

import static com.aimr.notify.constants.ErrorConstants.*;

@Data
public class CreateTemplateRequest {

    @Size(max = 10000, message = NAME_VARIABLE_ERROR)
    @NotBlank(message = "Name Field Is Required")
    private String name;

    @NotEmpty(message = "Template Variables Are Required")
    private Map<String, String> templateVariables;

    @Size(max = 10000, message = MESSAGE_VARIABLE_ERROR)
    @NotBlank(message = "Message Template Field Is Required")
    private String messageTemplate;

    @AssertTrue(message = TEMPLATE_VARIABLE_ERROR)
    public boolean validateTemplateVariable() {
        return CommonUtils.isNotEmpty(templateVariables) && templateVariables.size() <= 20;
    }

}
