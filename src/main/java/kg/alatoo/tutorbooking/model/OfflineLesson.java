package kg.alatoo.tutorbooking.model;

import java.time.LocalDateTime;

public class OfflineLesson extends Lesson {
    // Demo business rule: the tutor travels to the student, so offline costs more
    public static final int TRAVEL_FEE_SOM = 200;

    public OfflineLesson(Student student, Tutor tutor, Subject subject, LocalDateTime startsAt, String address) {
        super(student, tutor, subject, startsAt, address);
    }

    @Override
    public int getPriceSom() {
        return getTutor().getHourlyRateSom() + TRAVEL_FEE_SOM;
    }

    @Override
    public String getType() {
        return "OFFLINE";
    }

    @Override
    public String getTypeTitle() {
        return "Офлайн";
    }

    @Override
    public String locationHint() {
        return "Адрес";
    }
}
