package kg.alatoo.tutorbooking.service;

/** Thrown when the tutor or the student already has a lesson at the requested time. */
public class TimeSlotTakenException extends RuntimeException {
    public TimeSlotTakenException(String message) {
        super(message);
    }
}
