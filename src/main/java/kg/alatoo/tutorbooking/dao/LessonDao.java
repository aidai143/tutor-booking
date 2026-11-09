package kg.alatoo.tutorbooking.dao;

import kg.alatoo.tutorbooking.model.Lesson;
import kg.alatoo.tutorbooking.model.LessonFactory;
import kg.alatoo.tutorbooking.model.LessonStatus;
import kg.alatoo.tutorbooking.model.Student;
import kg.alatoo.tutorbooking.model.Subject;
import kg.alatoo.tutorbooking.model.Tutor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LessonDao {

    // One query joins all four tables, so each row already has everything needed to build a Lesson
    private static final String SELECT_WITH_JOINS = """
            SELECT l.id, l.starts_at, l.type, l.location, l.status,
                   s.id AS student_id, s.name AS student_name, s.phone AS student_phone, s.grade,
                   t.id AS tutor_id, t.name AS tutor_name, t.phone AS tutor_phone, t.hourly_rate_som,
                   sub.id AS subject_id, sub.name AS subject_name
            FROM lessons l
            JOIN students s   ON s.id = l.student_id
            JOIN tutors t     ON t.id = l.tutor_id
            JOIN subjects sub ON sub.id = l.subject_id
            """;

    private final DataSource dataSource;

    public LessonDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** All lessons; if studentName is not blank, only lessons of students whose name contains it. */
    public List<Lesson> findAll(String studentName) {
        boolean search = studentName != null && !studentName.isBlank();
        String sql = SELECT_WITH_JOINS
                + (search ? " WHERE s.name ILIKE ?" : "")
                + " ORDER BY l.starts_at";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (search) {
                statement.setString(1, "%" + studentName.trim() + "%");
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<Lesson> lessons = new ArrayList<>();
                while (rows.next()) {
                    lessons.add(toLesson(rows));
                }
                return lessons;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось загрузить занятия", e);
        }
    }

    public Lesson save(Lesson lesson) {
        String sql = """
                INSERT INTO lessons (student_id, tutor_id, subject_id, starts_at, type, location, status, price_som)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, lesson.getStudent().getId());
            statement.setInt(2, lesson.getTutor().getId());
            statement.setInt(3, lesson.getSubject().getId());
            statement.setTimestamp(4, Timestamp.valueOf(lesson.getStartsAt()));
            statement.setString(5, lesson.getType());
            statement.setString(6, lesson.getLocation());
            statement.setString(7, lesson.getStatus().name());
            statement.setInt(8, lesson.getPriceSom());
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                lesson.setId(rows.getInt("id"));
            }
            return lesson;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось сохранить занятие", e);
        }
    }

    /** Changes the status of a BOOKED lesson. Returns false if there is no such BOOKED lesson. */
    public boolean updateBookedStatus(int lessonId, LessonStatus status) {
        String sql = "UPDATE lessons SET status = ? WHERE id = ? AND status = 'BOOKED'";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, lessonId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось изменить статус занятия", e);
        }
    }

    public boolean tutorIsBusy(int tutorId, LocalDateTime startsAt) {
        return hasOverlap("SELECT COUNT(*) FROM lessons WHERE tutor_id = ?"
                + " AND status <> 'CANCELLED' AND starts_at > ? AND starts_at < ?", tutorId, startsAt);
    }

    public boolean studentIsBusy(int studentId, LocalDateTime startsAt) {
        return hasOverlap("SELECT COUNT(*) FROM lessons WHERE student_id = ?"
                + " AND status <> 'CANCELLED' AND starts_at > ? AND starts_at < ?", studentId, startsAt);
    }

    /** Report: for every tutor (even with no lessons) — how many lessons are done and how much they earned. */
    public List<TutorReportRow> tutorReport() {
        String sql = """
                SELECT t.name,
                       COUNT(l.id)                   AS done_lessons,
                       COALESCE(SUM(l.price_som), 0) AS earned_som
                FROM tutors t
                LEFT JOIN lessons l ON l.tutor_id = t.id AND l.status = 'DONE'
                GROUP BY t.id, t.name
                ORDER BY earned_som DESC, t.name
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<TutorReportRow> report = new ArrayList<>();
            while (rows.next()) {
                report.add(new TutorReportRow(
                        rows.getString("name"), rows.getInt("done_lessons"), rows.getInt("earned_som")));
            }
            return report;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось построить отчёт", e);
        }
    }

    /**
     * One query, three aggregates. "Today" means from 00:00 of the given day to 00:00 of the next one;
     * cancelled lessons are not counted for today, revenue counts only lessons that took place.
     */
    public LessonSummary summary(LocalDate day) {
        String sql = """
                SELECT COUNT(CASE WHEN starts_at >= ? AND starts_at < ? AND status <> 'CANCELLED' THEN 1 END) AS today_lessons,
                       COUNT(CASE WHEN status = 'DONE' THEN 1 END)                                          AS done_lessons,
                       COALESCE(SUM(CASE WHEN status = 'DONE' THEN price_som END), 0)                       AS revenue_som
                FROM lessons
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(day.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(day.plusDays(1).atStartOfDay()));
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return new LessonSummary(rows.getInt("today_lessons"), rows.getInt("done_lessons"), rows.getInt("revenue_som"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось посчитать сводку", e);
        }
    }

    // Two lessons of 60 minutes overlap if their starts are less than 60 minutes apart
    private boolean hasOverlap(String sql, int personId, LocalDateTime startsAt) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, personId);
            statement.setTimestamp(2, Timestamp.valueOf(startsAt.minusMinutes(Lesson.DURATION_MINUTES)));
            statement.setTimestamp(3, Timestamp.valueOf(startsAt.plusMinutes(Lesson.DURATION_MINUTES)));
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось проверить расписание", e);
        }
    }

    private Lesson toLesson(ResultSet row) throws SQLException {
        Student student = new Student(row.getString("student_name"), row.getString("student_phone"), row.getInt("grade"));
        student.setId(row.getInt("student_id"));
        Tutor tutor = new Tutor(row.getString("tutor_name"), row.getString("tutor_phone"), row.getInt("hourly_rate_som"));
        tutor.setId(row.getInt("tutor_id"));
        Subject subject = new Subject(row.getString("subject_name"));
        subject.setId(row.getInt("subject_id"));

        Lesson lesson = LessonFactory.create(row.getString("type"), student, tutor, subject,
                row.getTimestamp("starts_at").toLocalDateTime(), row.getString("location"));
        lesson.setId(row.getInt("id"));
        lesson.setStatus(LessonStatus.valueOf(row.getString("status")));
        return lesson;
    }
}
