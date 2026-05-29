package domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

// Новая сущность - пользователь, имя будем ему давать все таки
//  login - уникальный логин
//  password - хеш пароля в виде HEX-строки (SHA-256)
//  Пароль НИКОГДА не хранится в открытом виде
//  При создании пользователя сырой пароль сразу хешируется методом hashPassword()
//  При проверке входа мы хешируем введённый пароль и сравниваем два хеша


public class User {
    private final String login;
    private final String passwordHash; // SHA-256 в HEX


//     Основной конструктор - принимает сырой пароль и сразу же хеширует его
//     @param login       уникальный логин пользователя
//     @param rawPassword пароль в открытом виде (будет захеширован и забыт)

    public User(String login, String rawPassword) {
        this.login = login;
        this.passwordHash = hashPassword(rawPassword); // хешируем сразу сырой пароль нигде не сохраняется
    }

//     Конструктор для загрузки из файла - принимает УЖЕ готовый хеш!!
//     Если использовать 1 конструктор при чтении файла то будет повторное
//     хеширование и пользователь НИКОГДА  не сможет зайти
//     Используется в xmlUserStorage при чтении данных чтобы не хешировать повторно
//     login - логин
//     passwordHash - готовый SHA-256 хеш (hex-строка)
//     alreadyHashed - флаг-заглушка чтобы отличить этот конструктор от первого!!
//
    public User(String login, String passwordHash, boolean alreadyHashed) {
        this.login = login;
        this.passwordHash = passwordHash; // берём как есть - уже захешировано
    }

    public String getLogin() {
        return login;
    }

    // Геттер хеша нужен при сохранении в файл
    public String getPasswordHash() {
        return passwordHash;
    }

//     Проверяет, совпадает ли введённый пароль с сохранённым хешем
//     Хешируем входной пароль и сравниваем два хеша - сырой пароль нигде не всплывает
//     @param rawPassword - пароль введённый пользователем при входе
//     @return true если пароль верный
//
    public boolean checkPassword(String rawPassword) {
        return passwordHash.equals(hashPassword(rawPassword));
    }

//     SHA-256 хеширование строки
//     MessageDigest - стандартный Java класс для криптографических хешей
//     Результат - массив байт который мы переводим в HEX-строку через HexFormat
//     @param raw исходная строка
//     @return SHA-256 хеш в виде hex-строки нижнего регистра (64 символа)

    public static String hashPassword(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // getBytes переводит строку в массив байт по кодировке ютф8
            byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            // HexFormat.of() - утилита Java 17+ она переводит хешированные байты в hex-строку
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 есть в любой JVM - это исключение но практически невозможно
            throw new RuntimeException("SHA-256 недоступен в этой JVM", e);
        }
    }

    // equals и hashCode по логину тк логин уникальный идентификатор
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "User{login='" + login + "'}";
    }
}
