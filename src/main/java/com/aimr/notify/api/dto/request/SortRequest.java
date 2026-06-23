package com.aimr.notify.api.dto.request;

import com.aimr.notify.domain.enums.SortType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class SortRequest {
    private String sortKey;
    private Instant anchorTime;
    private SortType sortType = SortType.DESC;
    private String cursor;      // opaque Base64 cursor — used by subclasses that need it
}
