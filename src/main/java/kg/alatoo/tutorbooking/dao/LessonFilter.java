package kg.alatoo.tutorbooking.dao;

import kg.alatoo.tutorbooking.model.LessonStatus;

import java.time.LocalDate;

/**
 * What the administrator chose in the filters above the lesson list.
 * Every field may be null — then that filter is simply not applied.
 * "to" includes the whole day: lessons up to 23:59 of that date are shown.
 */
public record LessonFilter(String studentName, Integer tutorId, LessonStatus status, LocalDate from, LocalDate to) {

    public static LessonFilter none() {
        return new LessonFilter(null, null, null, null, null);
    }

    public boolean isEmpty() {
        return (studentName == null || studentName.isBlank()) && tutorId == null && status == null && from == null && to == null;
    }
}
