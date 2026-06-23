package com.aimr.notify.api.controller;

import com.aimr.notify.api.dto.request.CreateTemplateRequest;
import com.aimr.notify.api.dto.request.UpdateTemplateRequest;
import com.aimr.notify.api.dto.response.ApiResponse;
import com.aimr.notify.api.dto.response.TemplateResponse;
import com.aimr.notify.api.dto.response.MongoSearchResult;
import com.aimr.notify.api.dto.request.TemplateSearchDTO;
import com.aimr.notify.service.TemplateService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.aimr.notify.constant.ApplicationConstants.*;

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
    public ApiResponse<@NonNull MongoSearchResult<TemplateResponse>> filterTemplate(@RequestBody TemplateSearchDTO request) {
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
