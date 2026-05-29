package service;

import db.UserRepository;
import domain.User;

import java.util.Collection;


// UserService - бизнес-логика пользователей
// Всё хранится в PostgreSQL через UserRepository

public class UserService {

    private final UserRepository repo = new UserRepository();

//     Зарегистрировать нового пользователя
//     Проверяет уникальность через SELECT COUNT в БД
//     Пароль хешируется в конструкторе User (SHA-256)

    public User register(String login, String rawPassword) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Ошибка: логин не может быть пустым");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Ошибка: пароль не может быть пустым");
        }
        if (rawPassword.length() < 4) {
            throw new IllegalArgumentException("Ошибка: пароль должен быть не короче 4 символов");
        }

        // Проверяем уникальность через запрос к БД
        if (repo.existsByLogin(login.toLowerCase())) {
            throw new IllegalArgumentException("Ошибка: логин «" + login + "» уже занят");
        }

        // Конструктор User(login, rawPassword) сам хеширует пароль через SHA-256
        User user = new User(login.toLowerCase(), rawPassword);

        // INSERT в таблицу users
        repo.save(user);

        return user;
    }

//     Войти в систему: проверить логин и пароль
//     Читаем хеш из БД и сравниваем с хешем введённого пароля

    public User authenticate(String login, String rawPassword) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Ошибка: введите логин");
        }

        // SELECT из БД по логину
        User user = repo.findByLogin(login.toLowerCase());

        if (user == null || !user.checkPassword(rawPassword)) {
            throw new IllegalArgumentException("Ошибка: неверный логин или пароль");
        }

        return user;
    }


    public Collection<User> getAll() {
        return repo.findAll();
    }

}