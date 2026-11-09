package kg.alatoo.tutorbooking.web;

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
}
