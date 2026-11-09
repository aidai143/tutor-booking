package kg.alatoo.tutorbooking.dao;

import kg.alatoo.tutorbooking.model.Student;
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
public class StudentDao {

    // PostgreSQL error code for "UNIQUE constraint violated"
    private static final String UNIQUE_VIOLATION = "23505";
    // PostgreSQL error code for "row is still referenced by a foreign key"
    private static final String FOREIGN_KEY_VIOLATION = "23503";

    private final DataSource dataSource;

    public StudentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Student> findAll() {
        String sql = "SELECT id, name, phone, grade FROM students ORDER BY name";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<Student> students = new ArrayList<>();
            while (rows.next()) {
                students.add(toStudent(rows));
            }
            return students;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось загрузить список учеников", e);
        }
    }

    public Optional<Student> findById(int id) {
        String sql = "SELECT id, name, phone, grade FROM students WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(toStudent(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось найти ученика", e);
        }
    }

    public Student save(Student student) {
        String sql = "INSERT INTO students (name, phone, grade) VALUES (?, ?, ?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, student.getName());
            statement.setString(2, student.getPhone());
            statement.setInt(3, student.getGrade());
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                student.setId(rows.getInt("id"));
            }
            return student;
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalArgumentException("Ученик с телефоном " + student.getPhone() + " уже есть");
            }
            throw new DatabaseException("Не удалось сохранить ученика", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try(Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1,id);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (FOREIGN_KEY_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalArgumentException("Нельзя удалить ученика: у него есть занятия");
            }
            throw new DatabaseException("Не удалось удалить ученика", e);
        }
    }

    private Student toStudent(ResultSet row) throws SQLException {
        Student student = new Student(row.getString("name"), row.getString("phone"), row.getInt("grade"));
        student.setId(row.getInt("id"));
        return student;
    }
}
