package com.aimr.notify.controller;

import com.aimr.notify.models.dto.request.CreateTemplateRequest;
import com.aimr.notify.models.dto.request.UpdateTemplateRequest;
import com.aimr.notify.models.dto.request.TemplateFilterRequest;
import com.aimr.notify.models.dto.response.ApiResponse;
import com.aimr.notify.models.dto.response.FilterTemplateResponse;
import com.aimr.notify.models.dto.response.TemplateResponse;
import com.aimr.notify.service.interfaces.TemplateService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.aimr.notify.constants.ApplicationConstants.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ApiResponse<@NonNull TemplateResponse> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        return ApiResponse.success(HttpStatus.CREATED.value(), OBJECT_CREATED_SUCCESS_MESSAGE, templateService.createTemplate(request));
//        return responseHandler.ok(List.of("dummy hello data"));
    }

    @GetMapping
    public ApiResponse<@NonNull FilterTemplateResponse> filterTemplate(@RequestBody TemplateFilterRequest request) {
        return ApiResponse.success(HttpStatus.OK.value(), OBJECT_RETRIEVAL_SUCCESS_MESSAGE, templateService.filterTemplate(request));
    }

    @PatchMapping("/{id}")
    public ApiResponse<@NonNull TemplateResponse> updateTemplate(@PathVariable String id, @RequestBody UpdateTemplateRequest request) {
        return ApiResponse.success(HttpStatus.OK.value(), OBJECT_UPDATED_SUCCESS_MESSAGE, templateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteTemplate(@PathVariable String id) {
        templateService.deleteTemplate(id);
        return ApiResponse.success(HttpStatus.NO_CONTENT.value(), OBJECT_DELETION_SUCCESS_MESSAGE, null);
    }


}
