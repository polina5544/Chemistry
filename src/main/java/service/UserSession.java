package service;

import domain.User;


// UserSession - объект сессии приложения
// Хранит ссылку на текущего авторизованного пользователя.
// Если пользователь не вошёл - currentUser = null.
// Передаётся через dependency injection во все команды
// которым нужно знать кто сейчас работает с программой
// Паттерн: один объект сессии создаётся в Start.main()
// и передаётся во все нужные команды - так все команды
// видят одно и то же состояние входа ну или выхода

public class UserSession {

    private User currentUser = null; // null = никто не вошёл

    // Войти в систему - запомнить пользователя как текущего

    public void login(User user) {
        this.currentUser = user;
    }

    // Выйти из системы значит забыть текущего пользователя

    public void logout() {
        this.currentUser = null;
    }

     // Проверить авторизован ли кто-то сейчас

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Получить текущего пользователя

    public User getCurrentUser() {
        return currentUser;
    }

    // Получить логин текущего пользователя для записи в owner
    // Если никто не вошёл - возвращает unknown

    public String getCurrentLogin() {
        return currentUser != null ? currentUser.getLogin() : "unknown";
    }
}
