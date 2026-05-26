package db;

import domain.Measurement;
import domain.MeasurementParam;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MeasurementRepository — операции с таблицей measurements.
 */
public class MeasurementRepository {

    /**
     * Сохранить новое измерение.
     * id генерируется БД автоматически (BIGSERIAL).
     */
    public Measurement save(Measurement m) {
        String sql = """
                INSERT INTO measurements
                    (sample_id, param, value, unit, method, measured_at, owner_login)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, m.getSampleId());
            stmt.setString(2, m.getParam().name()); // enum → строка: "PH", "TURBIDITY" и т.д.
            stmt.setDouble(3, m.getValue());
            stmt.setString(4, m.getUnit());
            stmt.setString(5, m.getMethod());
            // setTimestamp: конвертируем Java Instant → SQL Timestamp
            stmt.setTimestamp(6, Timestamp.from(m.getMeasuredAt()));
            stmt.setString(7, m.getOwnerUsername());

            stmt.executeUpdate();

            // Читаем сгенерированный id
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long genId = keys.getLong(1);
                    return new Measurement(
                            genId,
                            m.getSampleId(),
                            m.getParam(),
                            m.getValue(),
                            m.getUnit(),
                            m.getMethod(),
                            m.getMeasuredAt(),
                            m.getOwnerUsername(),
                            Instant.now(),
                            Instant.now()
                    );
                }
            }
            throw new RuntimeException("БД не вернула id измерения");

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения измерения: " + e.getMessage(), e);
        }
    }

    /**
     * Получить все измерения для конкретного образца.
     * Используется в MeasurementListCommand.
     */
    public List<Measurement> findBySampleId(long sampleId) {
        // ORDER BY measured_at DESC — сначала самые новые
        String sql = """
                SELECT * FROM measurements
                WHERE sample_id = ?
                ORDER BY measured_at DESC
                """;
        List<Measurement> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sampleId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки измерений: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Получить измерения образца по конкретному параметру.
     * Используется в MeasurementStatsCommand и MeasurementListCommand с --param.
     */
    public List<Measurement> findBySampleIdAndParam(long sampleId, MeasurementParam param) {
        String sql = """
                SELECT * FROM measurements
                WHERE sample_id = ? AND param = ?
                ORDER BY measured_at DESC
                """;
        List<Measurement> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sampleId);
            stmt.setString(2, param.name()); // enum → строка

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки измерений: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Получить ВСЕ измерения (нужно для совместимости с Set<Measurement> в сервисах).
     * Используется при инициализации приложения.
     */
    public List<Measurement> findAll() {
        String sql = "SELECT * FROM measurements ORDER BY id";
        List<Measurement> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки всех измерений: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * mapRow — одна строка ResultSet → объект Measurement.
     */
    private Measurement mapRow(ResultSet rs) throws SQLException {
        // valueOf(строка) → enum. Строка "PH" → MeasurementParam.PH
        MeasurementParam param = MeasurementParam.valueOf(rs.getString("param"));

        Timestamp measuredAt = rs.getTimestamp("measured_at");
        Timestamp createdAt  = rs.getTimestamp("created_at");
        Timestamp updatedAt  = rs.getTimestamp("updated_at");

        return new Measurement(
                rs.getLong("id"),
                rs.getLong("sample_id"),
                param,
                rs.getDouble("value"),
                rs.getString("unit"),
                rs.getString("method"),
                measuredAt != null ? measuredAt.toInstant() : Instant.now(),
                rs.getString("owner_login"),
                createdAt  != null ? createdAt.toInstant()  : Instant.now(),
                updatedAt  != null ? updatedAt.toInstant()  : Instant.now()
        );
    }
}