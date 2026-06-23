package com.aimr.notify.api.dto.request;

import com.aimr.notify.api.dto.common.BaseSearchDTO;
import com.aimr.notify.domain.enums.NotificationChannel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class TemplateSearchDTO extends BaseSearchDTO {

    private static final Set<String> ALLOWED_SORT_KEYS = Set.of(
        "createdAt", "updatedAt", "name", "channel"
    );

    private String name;        // case-insensitive partial match
    private NotificationChannel channel;
    private Boolean isActive;

    // No buildCursorCriteria() override needed here.
    // Templates are low-volume and don't suffer from timestamp collisions,
    // so the default single-field anchorTime cursor from BaseSearchDTO is sufficient.

    @Override
    protected Set<String> allowedSortKeys() {
        return ALLOWED_SORT_KEYS;
    }

    @Override
    protected List<Criteria> buildSubCriteria() {
        List<Criteria> criteria = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            criteria.add(Criteria.where("name").regex(name, "i"));
        }

        if (channel != null) {
            criteria.add(Criteria.where("channel").is(channel));
        }

        if (isActive != null) {
            criteria.add(Criteria.where("isActive").is(isActive));
        }

        return criteria;
    }
}
