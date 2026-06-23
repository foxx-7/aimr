package com.aimr.notify.api.dto.common;

import com.aimr.notify.api.dto.request.SortRequest;
import com.aimr.notify.domain.enums.SortType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.aimr.notify.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.aimr.notify.constant.ApplicationConstants.MAX_PAGE_SIZE;

@Getter
@Setter
public abstract class BaseSearchDTO {

    private int page = 0;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private SortRequest sortRequest;

    /**
     * Returns the set of field names the caller is permitted to common by.
     * Any sortKey not in this set causes an IllegalArgumentException.
     */
    protected abstract Set<String> allowedSortKeys();

    /**
     * Returns domain-specific Criteria contributed by each subclass.
     * Return an empty list if there are no additional filters.
     * Null entries in the list are safely ignored.
     */
    protected abstract List<Criteria> buildSubCriteria();

    /**
     * Builds the cursor Criteria used to position the query at the right
     * point in the collection for the next page.
     * Default behaviour: single-field anchorTime boundary.
     * Subclasses with timestamp-collision risk (e.g. NotificationSearchDTO)
     * override this with a composite { createdAt, _id } cursor.
     * Returns null when no cursor is present (first page request).
     */
    protected Criteria buildCursorCriteria() {
        if (sortRequest == null || sortRequest.getAnchorTime() == null) return null;

        return SortType.ASC.equals(sortRequest.getSortType())
            ? Criteria.where("createdAt").gt(sortRequest.getAnchorTime())
            : Criteria.where("createdAt").lt(sortRequest.getAnchorTime());
    }

    /**
     * Assembles the final MongoDB Query:
     *   1. Tenant scope
     *   2. Cursor boundary (default or composite via override)
     *   3. Subclass field-level filters
     *   4. andOperator merge
     *   5. Sort
     *   6. limit(pageSize + 1) for hasMore detection
     */
    public Query toQuery(String tenantId) {
        List<Criteria> all = new ArrayList<>();

        //Tenant scope — always first, never skippable
        all.add(Criteria.where("tenantId").is(tenantId));

        // Cursor — delegated to default or subclass override
        Criteria cursorCriteria = buildCursorCriteria();
        if (cursorCriteria != null) {
            all.add(cursorCriteria);
        }

        //Subclass field-level filters
        List<Criteria> sub = buildSubCriteria();
        if (sub != null) {
            sub.stream()
               .filter(Objects::nonNull)
               .forEach(all::add);
        }

        // Combine — every condition must match
        Criteria combined = new Criteria().andOperator(all.toArray(new Criteria[0]));
        Query query = new Query(combined);

        // Sort
        query.with(resolveSort());

        // Fetch pageSize + 1 so the service can detect hasMore without
        //    a separate count query. Caller trims the extra document before returning.
        int safeSize = resolvedPageSize();
        query.limit(safeSize + 1);

        return query;
    }

    /**
     * Returns the clamped page size.
     * Service layer uses this when trimming the +1 result and building
     * the MongoSearchResult.
     */
    public int resolvedPageSize() {
        return Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);
    }

    private Sort resolveSort() {
        if (sortRequest == null || sortRequest.getSortKey() == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String key = sortRequest.getSortKey();
        if (!allowedSortKeys().contains(key)) {
            throw new IllegalArgumentException(
                "Invalid common key '" + key + "'. Allowed: " + allowedSortKeys()
            );
        }

        Sort.Direction direction = SortType.ASC.equals(sortRequest.getSortType())
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        return Sort.by(direction, key);
    }
}
