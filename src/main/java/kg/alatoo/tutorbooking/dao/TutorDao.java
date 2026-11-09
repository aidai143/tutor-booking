package kg.alatoo.tutorbooking.dao;

import kg.alatoo.tutorbooking.model.Tutor;
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
public class TutorDao {

    private static final String UNIQUE_VIOLATION = "23505";
    private static final String FOREIGN_KEY_VIOLATION = "23503";

    private final DataSource dataSource;

    public TutorDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Tutor> findAll() {
        String sql = "SELECT id, name, phone, hourly_rate_som FROM tutors ORDER BY name";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<Tutor> tutors = new ArrayList<>();
            while (rows.next()) {
                tutors.add(toTutor(rows));
            }
            return tutors;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось загрузить список репетиторов", e);
        }
    }

    public Optional<Tutor> findById(int id) {
        String sql = "SELECT id, name, phone, hourly_rate_som FROM tutors WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(toTutor(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось найти репетитора", e);
        }
    }

    public Tutor save(Tutor tutor) {
        String sql = "INSERT INTO tutors (name, phone, hourly_rate_som) VALUES (?, ?, ?) RETURNING id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tutor.getName());
            statement.setString(2, tutor.getPhone());
            statement.setInt(3, tutor.getHourlyRateSom());
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                tutor.setId(rows.getInt("id"));
            }
            return tutor;
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalArgumentException("Репетитор с телефоном " + tutor.getPhone() + " уже есть");
            }
            throw new DatabaseException("Не удалось сохранить репетитора", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM tutors WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (FOREIGN_KEY_VIOLATION.equals(e.getSQLState())) {
                throw new IllegalArgumentException("Нельзя удалить репетитора: у него есть занятия");
            }
            throw new DatabaseException("Не удалось удалить репетитора", e);
        }
    }

    private Tutor toTutor(ResultSet row) throws SQLException {
        Tutor tutor = new Tutor(row.getString("name"), row.getString("phone"), row.getInt("hourly_rate_som"));
        tutor.setId(row.getInt("id"));
        return tutor;
    }
}
