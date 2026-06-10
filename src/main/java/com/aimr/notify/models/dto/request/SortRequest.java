package com.aimr.notify.models.dto.request;

import com.aimr.notify.models.enums.SortType;
import lombok.Data;

@Data
public class SortRequest {

    private String sortKey;
    private SortType sortType;

}
