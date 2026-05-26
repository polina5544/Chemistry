package db;

import domain.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//UserRepository - класс для работы с таблицей users в PostgreSQL
// Репозиторий - то класс, который знает как сохранять и читать один тип объектов из БД
//UserRepository знает про таблицу users, SampleRepository - про samples, и т.д.
// Остальной код (сервисы, команды) не знают про SQL — они вызывают методы репозитория.
// КАК РАБОТАЕТ JDBC:
// 1. getConnection()  — открываем "трубу" к БД
// 2. prepareStatement(sql) — готовим SQL-запрос с параметрами (? — заглушки)
// 3. stmt.setString(1, value) — подставляем значения вместо ?
// 4. stmt.executeUpdate() — выполняем запрос (INSERT/UPDATE/DELETE)
// stmt.executeQuery()  — выполняем запрос и получаем строки (SELECT)
// 5. ResultSet — итератор по строкам результата SELECT
// 6. connection.close() — закрываем соединение (через try-with-resources)

// ПОЧЕМУ PreparedStatement, а не просто Statement?
// Statement уязвим к SQL-инъекциям: если пользователь введёт логин
// "alice'; DROP TABLE users; --" — это выполнится как SQL!
// PreparedStatement экранирует параметры автоматически.

public class UserRepository {

    /**
     * Сохранить нового пользователя в БД.
     * INSERT INTO users (login, password_hash) VALUES (?, ?)
     * ON CONFLICT DO NOTHING — если такой логин уже есть, просто ничего не делать.
     *
     * @param user объект пользователя (login + passwordHash уже готовы)
     */
    public void save(User user) {
        // SQL с параметрами-заглушками ?
        String sql = """
                INSERT INTO users (login, password_hash)
                VALUES (?, ?)
                ON CONFLICT (login) DO NOTHING
                """;
        // try-with-resources: connection закроется автоматически после блока
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Подставляем значения: первый ? = login, второй ? = hash
            // Индексы начинаются с 1, не с 0!
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPasswordHash()); // только хеш, не пароль

            // executeUpdate() выполняет INSERT/UPDATE/DELETE
            // Возвращает количество затронутых строк
            stmt.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения пользователя: " + e.getMessage(), e);
        }


    }

    /**
     * Найти пользователя по логину.
     * Возвращает null если не найден.
     *
     * SELECT * FROM users WHERE login = ?
     */
    public User findByLogin(String login) {
        String sql = "SELECT login, password_hash FROM users WHERE login = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login.toLowerCase()); // логин всегда в нижнем регистре

            // executeQuery() возвращает ResultSet — это курсор по строкам результата
            try (ResultSet rs = stmt.executeQuery()) {

                // rs.next() перемещает курсор на следующую строку
                // Если строка есть — возвращает true, иначе false
                if (rs.next()) {
                    // rs.getString("колонка") — читаем значение из текущей строки
                    String foundLogin = rs.getString("login");
                    String hash       = rs.getString("password_hash");

                    // Конструктор (login, hash, true): true = "это уже хеш, не хешировать снова"
                    return new User(foundLogin, hash, true);
                }
                // Пользователь не найден — возвращаем null
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска пользователя: " + e.getMessage(), e);
        }
    }

    /**
     * Получить всех пользователей.
     * Используется при загрузке данных при старте приложения.
     */
    public List<User> findAll() {
        String sql = "SELECT login, password_hash FROM users ORDER BY login";
        List<User> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             // Statement без параметров — можно использовать обычный PreparedStatement
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) { // executeQuery сразу возвращает ResultSet

            // Обходим все строки результата в цикле
            while (rs.next()) {
                result.add(new User(
                        rs.getString("login"),
                        rs.getString("password_hash"),
                        true // уже хеш
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки пользователей: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Проверить, существует ли пользователь с таким логином.
     * Используем COUNT(*) вместо SELECT * — быстрее, не тащим данные.
     */
    public boolean existsByLogin(String login) {
        String sql = "SELECT COUNT(*) FROM users WHERE login = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login.toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // getInt(1) — первая колонка результата (COUNT(*))
                    return rs.getInt(1) > 0;
                }
                return false;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки пользователя: " + e.getMessage(), e);
        }
    }
}