package dev.oumaimaa.util;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for formatting timestamps into human-readable strings.
 */
public final class TimeFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("MMM dd, yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private TimeFormatter() {
    }

    /**
     * Formats a timestamp into a relative or absolute time string.
     *
     * @param timestamp the timestamp in milliseconds
     * @return formatted time string
     */
    public static @NotNull String format(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        Instant now = Instant.now();

        long seconds = ChronoUnit.SECONDS.between(instant, now);

        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            return FORMATTER.format(instant);
        }
    }

    /**
     * Formats a timestamp into an absolute date/time string.
     *
     * @param timestamp the timestamp in milliseconds
     * @return formatted date/time string
     */
    public static @NotNull String formatAbsolute(long timestamp) {
        return FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }
}