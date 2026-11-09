package kg.alatoo.tutorbooking.dao;

import kg.alatoo.tutorbooking.model.Subject;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SubjectDao {

    private static final String UNIQUE_VIOLATION = "23505";
    private static final String FOREIGN_KEY_VIOLATION = "23503";

    private final DataSource dataSource;

    public SubjectDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Subject> findAll() {
        String sql = "SELECT id, name FROM subjects ORDER BY name";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<Subject> subjects = new ArrayList<>();
            while (rows.next()) {
                subjects.add(toSubject(rows));
            }
            return subjects;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось загрузить список предметов", e);
        }
    }

    public Optional<Subject> findById(int id) {
        String sql = "SELECT id, name FROM subjects WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(toSubject(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось найти предмет", e);
        }
    }

    public Subject save(Subject subject) {
        String sql = "INSERT INTO subjects (name) VALUES (?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, subject.getName());
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                subject.setId(rows.getInt("id"));
            }
            return subject;
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalArgumentException("Предмет «" + subject.getName() + "» уже есть");
            }
            throw new DatabaseException("Не удалось сохранить предмет", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM subjects WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (FOREIGN_KEY_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalArgumentException("Нельзя удалить предмет: по нему есть занятия");
            }
            throw new DatabaseException("Не удалось удалить предмет", e);
        }
    }

    private Subject toSubject(ResultSet row) throws SQLException {
        Subject subject = new Subject(row.getString("name"));
        subject.setId(row.getInt("id"));
        return subject;
    }
}
