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

    private User currentUser = null;  // хранит объект пользователя

    // Войти в систему
    public void login(User user) {
        this.currentUser = user;
        System.out.println("Текущий пользователь: " + user.getLogin());
    }

    // Выйти из системы
    public void logout() {
        this.currentUser = null;
        System.out.println("Пользователь вышел из системы");
    }

    // Проверить, авторизован ли кто-то
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Получить текущего пользователя (объект)
    public User getCurrentUser() {
        return currentUser;
    }

    // Получить ЛОГИН текущего пользователя
    public String getCurrentLogin() {
        if (currentUser == null) {
            System.out.println("пользователь не вошёл)");
            return null;
        }
        String login = currentUser.getLogin();
        System.out.println("UserSession: getCurrentLogin() = " + login);
        return login;
    }
}
