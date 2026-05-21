package service;

import domain.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//UserService - сервис управления пользователями
// Отвечает за регистрацию новых пользователей (проверка уникальности логина)
//        проверку пароля при входе
//        хранение всех пользователей в памяти
//        загрузку или выгрузку пользователей для сохранения в файл
//        Не знает ничего про файлы - тока про  логику

public class UserService {
    //список всех зарегестрированных пользователей
    // где логин - ключ, целый объект пользователя - значение
    private final Map<String, User> users = new HashMap<>();

//     Зарегистрировать нового пользователя
//     Проверяет уникальность логина, создаёт объект User (пароль хешируется внутри User)
//     login - желаемый логин
//     rawPassword - пароль в открытом виде
//     return созданный User
//     throws IllegalArgumentException если логин уже занят или поля пустые

    public User register(String login, String rawPassword) {
        // Валидация входных данных
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Ошибка: логин не может быть пустым");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Ошибка: пароль не может быть пустым");
        }
        if (rawPassword.length() < 4) {
            throw new IllegalArgumentException("Ошибка: пароль должен быть не короче 4 символов");
        }

        // Проверяю не занят ли логин, напоминаю, что containsKey - метод мапы
        if (users.containsKey(login.toLowerCase())) {
            throw new IllegalArgumentException("Ошибка: логин «" + login + "» уже занят");
        }

        // Создаём пользователя - конструктор User сам уже хеширует пароль
        User user = new User(login.toLowerCase(), rawPassword);
        users.put(user.getLogin(), user); //user.getLogin() -ключ а user - значение
        return user;
    }

//     Проверить логин и пароль для входа
//     login - введённый логин
//     rawPassword - введённый пароль
//     return объект User если данные верны
//     throws IllegalArgumentException если пользователь не найден или пароль неверный

    public User authenticate(String login, String rawPassword) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Ошибка: введите логин");
        }

        User user = users.get(login.toLowerCase());

        if (user == null) {
            throw new IllegalArgumentException("Ошибка: неверный логин или пароль");
        }

        // checkPassword внутри хеширует введённый пароль и сравнивает с хранимым хешем
        if (!user.checkPassword(rawPassword)) {
            throw new IllegalArgumentException("Ошибка: неверный логин или пароль");
        }

        return user;
    }

    public boolean exists(String login) {
        return users.containsKey(login.toLowerCase());
    }

    // Получить всех пользователей - нужно для сохранения в файл

    public Collection<User> getAll() {
        return users.values();
    }

//     Загрузить пользователей из файла в память программы
//     Очищает текущий список и заполняет новым

    public void setUsers(Collection<User> loadedUsers) {
        users.clear();
        for (User u : loadedUsers) {
            users.put(u.getLogin(), u); // логин уже в lowercase (сохранялся так)
        }
    }
}