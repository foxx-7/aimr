package com.aimr.notify.api.dto.request;

import com.aimr.notify.api.dto.common.BaseSearchDTO;
import com.aimr.notify.api.dto.common.NotificationCursor;
import com.aimr.notify.exception.DataConversionException;
import com.aimr.notify.domain.entity.Notification;
import com.aimr.notify.domain.enums.NotificationChannel;
import com.aimr.notify.domain.enums.NotificationStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.query.Criteria;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class NotificationSearchDTO extends BaseSearchDTO {

    private static final Set<String> ALLOWED_SORT_KEYS = Set.of(
        "createdAt", "updatedAt", "status", "channel"
    );

    private NotificationStatus status;
    private NotificationChannel channel;
    private String recipientId;
    private Instant from;           // explicit date range lower bound
    private Instant to;             // explicit date range upper bound
    private Instant windowFrom;     // ±window guard lower bound (set by factory)
    private Instant windowTo;       // ±window guard upper bound (set by factory)

    // Decoded in-memory — never exposed directly in the request body.
    // Set via the static factory method.
    private NotificationCursor decodedCursor;


    @Override
    protected Set<String> allowedSortKeys() {
        return ALLOWED_SORT_KEYS;
    }

    @Override
    protected List<Criteria> buildSubCriteria() {
        List<Criteria> criteria = new ArrayList<>();

        // Time window guard (±WINDOW_HOURS around anchorTime)
        // Scopes the scan so MongoDB doesn't read unbounded history
        if (windowFrom != null && windowTo != null) {
            criteria.add(Criteria.where("createdAt").gte(windowFrom).lte(windowTo));
        }

        if (status != null) {
            criteria.add(Criteria.where("status").is(status));
        }

        if (channel != null) {
            criteria.add(Criteria.where("dispatchChannel").is(channel));
        }

        if (recipientId != null && !recipientId.isBlank()) {
            criteria.add(Criteria.where("recipientId").is(recipientId));
        }

        // Explicit date range — independent of the cursor anchor
        if (from != null && to != null) {
            criteria.add(Criteria.where("createdAt").gte(from).lte(to));
        } else if (from != null) {
            criteria.add(Criteria.where("createdAt").gte(from));
        } else if (to != null) {
            criteria.add(Criteria.where("createdAt").lte(to));
        }

        return criteria;
    }

    /**
     * Composite { createdAt, _id } cursor.
     *
     * Stable across timestamp collisions — when multiple notifications share
     * the same createdAt (common in bulk dispatch), the _id tie-breaker
     * ensures no document is ever skipped or returned twice across pages.
     *
     * Equivalent MongoDB query:
     *   { $or: [
     *       { createdAt: { $lt: cursor.createdAt } },
     *       { createdAt: cursor.createdAt, _id: { $lt: cursor.id } }
     *   ]}
     */
    @Override
    protected Criteria buildCursorCriteria() {
        if (decodedCursor == null) return null;

        return new Criteria().orOperator(
            Criteria.where("createdAt").lt(decodedCursor.getCreatedAt()),
            Criteria.where("createdAt").is(decodedCursor.getCreatedAt())
                    .and("_id").lt(decodedCursor.getId())
        );
    }

    /**
     * Builds a NotificationSearchDTO from an opaque cursor string and an
     * anchorTime, decoding the cursor and computing the time window guard.
     *
     * @param encodedCursor  Base64 cursor from the previous response (null on first page)
     * @param anchorTime     The reference timestamp for the ±window guard
     * @param windowHours    Half-width of the time window (from ApplicationConstants)
     * @param jsonMapper     Jackson mapper for cursor deserialization
     */
    public static NotificationSearchDTO from(
            String encodedCursor,
            Instant anchorTime,
            long windowHours,
            JsonMapper jsonMapper) {

        NotificationSearchDTO dto = new NotificationSearchDTO();

        // Decode composite cursor if this is not the first page
        if (encodedCursor != null) {
            try {
                String json = new String(
                    Base64.getDecoder().decode(encodedCursor), StandardCharsets.UTF_8);
                dto.decodedCursor = jsonMapper.readValue(json, NotificationCursor.class);
            } catch (Exception e) {
                throw new DataConversionException("error while decoding cursor");
            }
        }

        // Apply time window guard around anchorTime
        if (anchorTime != null) {
            dto.windowFrom = anchorTime.minus(windowHours, ChronoUnit.HOURS);
            dto.windowTo   = anchorTime.plus(windowHours, ChronoUnit.HOURS);
        }

        return dto;
    }

    /**
     * Encodes the last document on the current page into an opaque Base64
     * cursor string for the client to pass back on the next request.
     *
     * @param last       The last Notification in the trimmed page
     * @param jsonMapper Jackson mapper for cursor serialization
     * @return           Opaque Base64 cursor string
     */
    public String encodeNextCursor(Notification last, JsonMapper jsonMapper) {
        try {
            NotificationCursor next = new NotificationCursor(
                last.getCreatedAt(), last.getId());
            String json = jsonMapper.writeValueAsString(next);
            return Base64.getEncoder()
                         .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new DataConversionException("error while encoding cursor");
        }
    }
}
