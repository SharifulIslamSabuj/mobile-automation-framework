package com.mobileautomation.framework.data.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a record component or field as required — {@link DataValidator} rejects a {@code null} or blank value for it after deserialization. */
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Required {
}
