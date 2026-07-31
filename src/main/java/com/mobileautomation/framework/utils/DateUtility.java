package com.mobileautomation.framework.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Reusable date/time operations built exclusively on {@code java.time}
 * (never the legacy {@code Date}/{@code Calendar} APIs). Stateless, driver-
 * and configuration-independent — usable from any layer without any
 * framework dependency.
 */
public final class DateUtility {

    /** Pattern used for filesystem-safe timestamps (e.g., screenshot file names). */
    public static final String FILENAME_TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss";

    private DateUtility() {
    }

    public static LocalDate currentDate() {
        return LocalDate.now();
    }

    public static LocalDateTime currentDateTime() {
        return LocalDateTime.now();
    }

    /** Timezone-safe current instant, expressed in UTC. */
    public static ZonedDateTime currentDateTimeUtc() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parseDateTime(String value, String pattern) {
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseDate(String value, String pattern) {
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate plusDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    public static LocalDate minusDays(LocalDate date, long days) {
        return date.minusDays(days);
    }

    /** A filesystem-safe timestamp of "now", suitable for unique file names (see {@link #FILENAME_TIMESTAMP_PATTERN}). */
    public static String timestampForFilename() {
        return format(currentDateTime(), FILENAME_TIMESTAMP_PATTERN);
    }
}
