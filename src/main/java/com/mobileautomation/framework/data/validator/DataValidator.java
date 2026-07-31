package com.mobileautomation.framework.data.validator;

import com.mobileautomation.framework.exceptions.DataValidationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;

/**
 * Generic, model-agnostic required-field validation, applied after a
 * {@code data.reader.DataReader} has already successfully parsed a
 * resource. Deliberately separate from parse-time validation: a malformed
 * JSON/YAML/Properties file or a type mismatch is already caught and
 * wrapped as {@code TestDataException} by the reader (Jackson's own
 * schema-shape binding IS the schema validation for this framework — see
 * docs/framework/TEST_DATA_FRAMEWORK.md). This class only checks the
 * business-agnostic rule "a field marked {@link Required} must not be null
 * or blank," driven entirely by reflection — it has no knowledge of any
 * specific model.
 */
public final class DataValidator {

    private DataValidator() {
    }

    /**
     * @throws DataValidationException if {@code data} is {@code null}, or any {@link Required}-annotated record component/field on it is null or blank
     */
    public static void validate(Object data) {
        if (data == null) {
            throw new DataValidationException("Loaded test data must not be null.");
        }
        Class<?> type = data.getClass();
        if (type.isRecord()) {
            validateRecordComponents(data, type);
        } else {
            validateFields(data, type);
        }
    }

    private static void validateRecordComponents(Object data, Class<?> type) {
        for (RecordComponent component : type.getRecordComponents()) {
            if (component.isAnnotationPresent(Required.class)) {
                requireNonBlank(invokeAccessor(component, data), component.getName(), type);
            }
        }
    }

    private static void validateFields(Object data, Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Required.class)) {
                requireNonBlank(readField(field, data), field.getName(), type);
            }
        }
    }

    private static void requireNonBlank(Object value, String fieldName, Class<?> type) {
        boolean blank = value == null || (value instanceof String stringValue && stringValue.isBlank());
        if (blank) {
            throw new DataValidationException(
                    "Required field '" + fieldName + "' is missing or blank on " + type.getSimpleName());
        }
    }

    private static Object invokeAccessor(RecordComponent component, Object data) {
        try {
            return component.getAccessor().invoke(data);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new DataValidationException(
                    "Failed to read record component '" + component.getName() + "' for validation.", e);
        }
    }

    private static Object readField(Field field, Object data) {
        try {
            field.setAccessible(true);
            return field.get(data);
        } catch (IllegalAccessException e) {
            throw new DataValidationException(
                    "Failed to read field '" + field.getName() + "' for validation.", e);
        }
    }
}
