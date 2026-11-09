package kg.alatoo.tutorbooking.dao;

/** One row of the tutor report: a short immutable data holder (Java record). */
public record TutorReportRow(String tutorName, int doneLessons, int earnedSom) {
}
