package kg.alatoo.tutorbooking.model;

public enum LessonStatus {
    BOOKED("Запланировано"),
    DONE("Проведено"),
    CANCELLED("Отменено");

    private final String title;

    LessonStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
