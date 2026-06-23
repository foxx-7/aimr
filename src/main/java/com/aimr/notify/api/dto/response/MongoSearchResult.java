package com.aimr.notify.api.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class MongoSearchResult<T> {

    private final List<T> content;
    private final long total;
    private final int page;
    private final int pageSize;
    private final boolean hasMore;
    private final String nextCursor; // null when no further pages exist

    public MongoSearchResult(List<T> content, long total, int page,
                              int pageSize, String nextCursor) {
        this.content    = content;
        this.total      = total;
        this.page       = page;
        this.pageSize   = pageSize;
        this.nextCursor = nextCursor;
        this.hasMore    = nextCursor != null;
    }

    public static <T> MongoSearchResult<T> of(List<T> content, long total,
                                               int page, int pageSize,
                                               String nextCursor) {
        return new MongoSearchResult<>(content, total, page, pageSize, nextCursor);
    }
}
