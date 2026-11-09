package kg.alatoo.tutorbooking.model;

import java.time.LocalDateTime;

/**
 * A lesson is always either online or offline, so this class is abstract:
 * "just a lesson" cannot be created. Each subclass decides its own price and place.
 */
public abstract class Lesson {
    public static final int DURATION_MINUTES = 60;

    private Integer id;
    private final Student student;
    private final Tutor tutor;
    private final Subject subject;
    private final LocalDateTime startsAt;
    private final String location;
    private LessonStatus status;

    protected Lesson(Student student, Tutor tutor, Subject subject, LocalDateTime startsAt, String location) {
        if (student == null || tutor == null || subject == null) {
            throw new IllegalArgumentException("Нужно выбрать ученика, репетитора и предмет");
        }
        if (startsAt == null) {
            throw new IllegalArgumentException("Нужно указать дату и время занятия");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Поле «" + locationHint() + "» не заполнено");
        }
        if (location.length() > 200) {
            throw new IllegalArgumentException("Поле «" + locationHint() + "»: слишком длинно (макс. 200 символов)");
        }
        this.student = student;
        this.tutor = tutor;
        this.subject = subject;
        this.startsAt = startsAt;
        this.location = location.trim();
        this.status = LessonStatus.BOOKED;
    }

    /** Price in som for this lesson. Online and offline lessons calculate it differently. */
    public abstract int getPriceSom();

    /** "ONLINE" or "OFFLINE" — the value stored in the lessons.type column. */
    public abstract String getType();

    /** Human-readable name of the type, e.g. for the web page. */
    public abstract String getTypeTitle();

    /** What the location field means for this type: a link or an address. */
    public abstract String locationHint();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public Subject getSubject() {
        return subject;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public String getLocation() {
        return location;
    }

    public LessonStatus getStatus() {
        return status;
    }

    public void setStatus(LessonStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Статус не может быть пустым");
        }
        this.status = status;
    }

    @Override
    public String toString() {
        return getTypeTitle() + ": " + subject + ", " + student.getName() + " у " + tutor.getName()
                + ", " + startsAt + ", " + getPriceSom() + " сом, " + status.getTitle();
    }
}
