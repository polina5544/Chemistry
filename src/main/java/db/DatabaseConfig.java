package db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConfig — единственное место подключения к PostgreSQL.
 *
 * Читает параметры из файла src/main/resources/db.properties
 * Формат файла:
 *   DB_URL=jdbc:postgresql://localhost:5432/chemistry_lab
 *   DB_USER=postgres
 *   DB_PASSWORD=твой_пароль
 *
 * getResourceAsStream — загружает файл из папки resources как поток байт.
 * Это стандартный способ читать конфиг-файлы в Java-проектах.
 */
public class DatabaseConfig {

    // Properties — стандартный Java-класс для чтения файлов формата ключ=значение
    private static final Properties props = new Properties();

    // static { } — статический блок инициализации.
    // Выполняется ОДИН РАЗ при первом обращении к классу.
    // Используем чтобы загрузить настройки до любого вызова getConnection().
    static {
        try (InputStream in = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (in == null) {
                throw new RuntimeException(
                        "Файл db.properties не найден в src/main/resources/. " +
                                "Создай его с полями DB_URL, DB_USER, DB_PASSWORD");
            }

            props.load(in); // читаем все пары ключ=значение из файла

            // Проверяем что обязательные поля заполнены
            if (props.getProperty("DB_URL") == null || props.getProperty("DB_URL").isBlank()) {
                throw new RuntimeException("В db.properties не задан DB_URL");
            }

        } catch (RuntimeException e) {
            throw e; // пробрасываем дальше
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения db.properties: " + e.getMessage(), e);
        }
    }

    /**
     * Создать новое подключение к PostgreSQL.
     *
     * ВАЖНО: каждый вызов открывает НОВОЕ соединение.
     * Закрывай его через try-with-resources: try (Connection c = getConnection()) { }
     * Java закроет соединение автоматически после блока — даже при ошибке.
     *
     * DriverManager.getConnection() — стандартный JDBC-метод.
     * Драйвер PostgreSQL (из pom.xml) регистрируется автоматически.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("DB_URL"),
                props.getProperty("DB_USER"),
                props.getProperty("DB_PASSWORD")
        );
    }
}