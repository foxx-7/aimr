package com.aimr.notify.api.util;

import com.aimr.notify.domain.dao.SingleIdUpdatableDao;
import com.aimr.notify.domain.dao.UpdatableDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class EntityUpdater {

    public static <T, D> T updateForTenant(
            UpdatableDao<T> dao,
            String tenantId,
            String id,
            D dto
    ) {
        T entity = dao.fetchEntity(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found for tenantId: " + tenantId + ", id: " + id));

        applyDto(dto, entity);

        return dao.saveEntity(entity);
    }

    public static <T,D> T updateForEntity(
            SingleIdUpdatableDao<T> dao,
            String id,
            D dto
    ){
        T entity = dao.fetchEntity(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Resource not found for identifier "+id));
        applyDto(dto, entity);

        return dao.saveEntity(entity);
    }

    private static <T, D> void applyDto(D dto, T entity) {
        Class<?> dtoClass = dto.getClass();
        Class<?> entityClass = entity.getClass();

        for (Field dtoField : dtoClass.getDeclaredFields()) {
            dtoField.setAccessible(true);

            Object value;
            try {
                value = dtoField.get(dto);
            } catch (IllegalAccessException e) {
                continue;
            }

            // null means not included in this update — skip
            if (value == null) continue;

            // find matching field on entity by name
            Field entityField = findField(entityClass, dtoField.getName());
            if (entityField == null) continue; // no match — silently ignore

            entityField.setAccessible(true);
            try {
                entityField.set(entity, value);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                // type mismatch between dto field and entity field — ignore
                continue;
            }
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}