package db;

import domain.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//UserRepository - класс для работы с таблицей users в PostgreSQL
// Репозиторий -  класс который знает как сохранять и читать один тип объектов из БД
//UserRepository знает про таблицу users, SampleRepository - про samples, и т.д.
// Остальной код (сервисы, команды) не знают про SQL - они вызывают методы репозитория

public class UserRepository {

//      Сохранить нового пользователя в БД
//      INSERT INTO users (login, password_hash) VALUES (?, ?) -
//      Java посылает в базу только чистый скелет запроса
//      База заранее изучает его, компилирует и жестко фиксирует
//      структуру - здесь будет только вставка, и ничего больше
//      Знаки вопроса превращают любые введенные пользователем символы в
//      обычный текст (строку) и не дают базе данных запустить
//      этот текст как команду

    public void save(User user) {
        String sql = """
                INSERT INTO users (login, password_hash)
                VALUES (?, ?)
                ON CONFLICT (login) DO NOTHING
                """;
        // открываем канал связи с БД
        try (Connection conn = DatabaseConfig.getConnection();

             //берем SQL-запрос (инструкцию) и заряжаем его в этот канал связи
             // Получается готовый инструмент - sql команда (stmt), в который осталось
             // только подставить логин и пароль
             //Как только код внутри { } заканчивается (или если там происходит авария и ошибка),
             // Java сама автоматически закрывает сетевой канал, поток памяти освобождается.

             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Подставляем значения: первый ? = login, второй ? = hash
            // Индексы начинаются с 1, не с 0!

            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPasswordHash());

            // executeUpdate() выполняет INSERT/UPDATE/DELETE
            // Возвращает количество затронутых строк
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения пользователя: " + e.getMessage(), e);
        }
    }

    public User findByLogin(String login) {
        String sql = "SELECT login, password_hash FROM users WHERE login = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login.toLowerCase()); // логин всегда в нижнем регистре

            // executeQuery - специальный метод для выполнения запросов чтения (SELECT)
            // Он дает базе команду найти строку
            try (ResultSet rs = stmt.executeQuery()) {

                // Когда база возвращает табличку ResultSet, указатель (курсор)
                // программы стоит перед первой строкой. Данные оттуда еще нельзя
                // прочитать.Метод rs.next() двигает этот указатель на первую строчку с
                // данными.Если база нашла пользователя, rs.next() возвращает true

                if (rs.next()) {

                    String foundLogin = rs.getString("login");
                    String hash       = rs.getString("password_hash");

                    // Конструктор (login, hash, true): true - это уже хеш, не хешировать снова
                    return new User(foundLogin, hash, true);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска пользователя: " + e.getMessage(), e);
        }
    }

    public List<User> findAll() {
        String sql = "SELECT login, password_hash FROM users ORDER BY login";
        List<User> result = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();

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


//     Проверить, существует ли пользователь с таким логином.
//     Используем COUNT(*) вместо SELECT * - быстрее, не тащим данные

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