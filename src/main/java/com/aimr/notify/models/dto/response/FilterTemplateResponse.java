package com.aimr.notify.models.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FilterTemplateResponse extends BaseTemplateResponse<TemplateResponseDTO, Long> {

    public FilterTemplateResponse(final List<TemplateResponseDTO> list,
                                  final boolean hasMoreElement,
                                  final Long totalCount) {
        setData(list);
        setTotalCount(totalCount);
        setHasMoreElement(hasMoreElement);
    }
}
