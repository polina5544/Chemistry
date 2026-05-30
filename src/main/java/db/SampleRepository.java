package db;

import domain.Sample;
import domain.SampleStatus;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//   SampleRepository - все SQL-операции с таблицей samples
//   id генерирует PostgreSQL (BIGSERIAL = автоинкремент)
//   При INSERT мы id не передаём, а читаем сгенерированный через getGeneratedKeys()

public class SampleRepository {

//     Сохранить новый образец в БД
//     Возвращает тот же образец но уже с реальным id от PostgreSQL

    public Sample save(Sample sample) {

        // id не указываем - PostgreSQL сам назначит следующий по порядку

        //Знаки вопроса превращают любые введенные пользователем символы в
        //обычный безобидный текст (строку) и не дают базе данных запустить
        //этот текст как команду

        String sql = "INSERT INTO samples (name, type, location, status, owner_login) " +
                "VALUES (?, ?, ?, ?, ?)";

        // RETURN_GENERATED_KEYS - флаг: после INSERT верни сгенерированный id

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Подставляем значения вместо ?
            stmt.setString(1, sample.getName());
            stmt.setString(2, sample.getType());
            stmt.setString(3, sample.getLocation());
            stmt.setString(4, sample.getStatus().name()); // enum → строка "ACTIVE"/"ARCHIVED"
            stmt.setString(5, sample.getOwnerUsername());

            // executeUpdate() выполняет INSERT и возвращает кол-во затронутых строк
            int affected = stmt.executeUpdate();
            stmt.executeUpdate();

            if (affected == 0) {
                throw new RuntimeException("INSERT не добавил ни одной строки");
            }

            // Читаем id который PostgreSQL присвоил новой строке
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long newId = keys.getLong(1); // первая колонка = id

                    // Возвращаем новый объект с правильным id от БД
                    return new Sample(
                            newId,
                            sample.getName(),
                            sample.getType(),
                            sample.getLocation(),
                            sample.getStatus(),
                            sample.getOwnerUsername(),
                            Instant.now(),
                            Instant.now()
                    );
                }
            }

            throw new RuntimeException("PostgreSQL не вернул сгенерированный id");

        } catch (SQLException e) {
            // Выводим полную информацию об ошибке для отладки
            System.err.println("SQL ошибка при save: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            throw new RuntimeException("Ошибка сохранения образца: " + e.getMessage(), e);
        }
    }


//     Обновить существующий образец в БД.
//     UPDATE находит строку по id и меняет поля

    public void update(Sample sample) {
        String sql = "UPDATE samples SET name=?, type=?, location=?, status=?, " +
                "updated_at=NOW() WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sample.getName());
            stmt.setString(2, sample.getType());
            stmt.setString(3, sample.getLocation());
            stmt.setString(4, sample.getStatus().name());
            stmt.setLong(5, sample.getId()); // id идёт последним — он в WHERE

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Образец с id=" + sample.getId() + " не найден в БД");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления образца: " + e.getMessage(), e);
        }
    }

    public Sample findById(long id) {
        String sql = "SELECT * FROM samples WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                // rs.next() — перейти на следующую строку результата
                // Если строка есть — возвращает true
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null; // строка не найдена
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска образца: " + e.getMessage(), e);
        }
    }

    public List<Sample> findAll() {
        String sql = "SELECT * FROM samples ORDER BY id";
        List<Sample> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // while rs.next() - обходим все строки результата
            while (rs.next()) {
                result.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки образцов: " + e.getMessage(), e);
        }

        return result;
    }

    public List<Sample> findByStatus(SampleStatus status) {
        String sql = "SELECT * FROM samples WHERE status=? ORDER BY id";
        List<Sample> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска по статусу: " + e.getMessage(), e);
        }

        return result;
    }

    public void delete(long id) {
        String sql = "DELETE FROM samples WHERE id=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Образец с id=" + id + " не найден");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления образца: " + e.getMessage(), e);
        }
    }

//     mapRow - одна строка ResultSet это объект Sample
//     Вынесен отдельно чтобы не дублировать код в findById и findAll
//     rs.getLong("id") - читает колонку id как long
//     rs.getString("name") - читает колонку name как String
//     rs.getTimestamp() - Timestamp конвертируем в Java Instant

    private Sample mapRow(ResultSet rs) throws SQLException {
        // "ACTIVE" → SampleStatus.ACTIVE
        SampleStatus status = SampleStatus.valueOf(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new Sample(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getString("location"),
                status,
                rs.getString("owner_login"), // может быть null если пользователь удалён
                createdAt != null ? createdAt.toInstant() : Instant.now(),
                updatedAt != null ? updatedAt.toInstant() : Instant.now()
        );
    }
}