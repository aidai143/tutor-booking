package kg.alatoo.tutorbooking.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Everything from an HTML form arrives as text. These helpers turn it into numbers and dates
 * and throw a clear IllegalArgumentException instead of a technical error page.
 */
final class FormValues {

    private FormValues() {
    }

    static int parseInt(String value, String fieldTitle) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Поле «" + fieldTitle + "» не заполнено");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + fieldTitle + "» должно быть целым числом");
        }
    }

    static LocalDateTime parseDateTime(String value, String fieldTitle) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Поле «" + fieldTitle + "» не заполнено");
        }
        try {
            // <input type="datetime-local"> sends "2026-11-12T15:00"
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Поле «" + fieldTitle + "»: неверный формат даты");
        }
    }

    /** Optional filter value: empty means "not chosen" (null). */
    static Integer parseOptionalInt(String value, String fieldTitle) {
        return value == null || value.isBlank() ? null : parseInt(value, fieldTitle);
    }

    /** Optional date from <input type="date"> ("2026-11-12"); empty means "not chosen" (null). */
    static LocalDate parseOptionalDate(String value, String fieldTitle) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Поле «" + fieldTitle + "»: неверный формат даты");
        }
    }
}
