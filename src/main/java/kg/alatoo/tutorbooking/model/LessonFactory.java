package kg.alatoo.tutorbooking.model;

import java.time.LocalDateTime;

/**
 * Simple Factory: turns the type string from a form or a DB row into the right subclass,
 * so the rest of the code never needs its own "if online ... else ..." switch.
 */
public class LessonFactory {

    private LessonFactory() {
    }

    public static Lesson create(String type, Student student, Tutor tutor, Subject subject,
                                LocalDateTime startsAt, String location) {
        if (type == null) {
            throw new IllegalArgumentException("Нужно выбрать формат занятия");
        }
        return switch (type) {
            case "ONLINE" -> new OnlineLesson(student, tutor, subject, startsAt, location);
            case "OFFLINE" -> new OfflineLesson(student, tutor, subject, startsAt, location);
            default -> throw new IllegalArgumentException("Неизвестный формат занятия: " + type);
        };
    }
}
