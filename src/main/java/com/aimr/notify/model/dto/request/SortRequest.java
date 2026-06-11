package com.aimr.notify.model.dto.request;

import com.aimr.notify.model.enums.SortType;
import lombok.Data;

@Data
public class SortRequest {

    private String sortKey;
    private SortType sortType;

}
