package kg.alatoo.tutorbooking.model;

import java.time.LocalDateTime;

public class OnlineLesson extends Lesson {

    public OnlineLesson(Student student, Tutor tutor, Subject subject, LocalDateTime startsAt, String meetingLink) {
        super(student, tutor, subject, startsAt, meetingLink);
    }

    @Override
    public int getPriceSom() {
        return getTutor().getHourlyRateSom();
    }

    @Override
    public String getType() {
        return "ONLINE";
    }

    @Override
    public String getTypeTitle() {
        return "Онлайн";
    }

    @Override
    public String locationHint() {
        return "Ссылка на звонок";
    }
}
