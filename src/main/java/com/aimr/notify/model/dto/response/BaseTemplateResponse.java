package com.aimr.notify.model.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BaseTemplateResponse<I, R extends Number> {

    private List<I> data;
    private boolean hasMoreElement;
    private R totalCount;
}
