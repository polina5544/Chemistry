package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.UserService;
import service.UserSession;


// MainApp - точка входа JavaFX
// Порядок:
// 1. Показываем LoginDialog (вход или регистрация)
// 2. Если вошли - открываем MainController
// 3. Если закрыли без входа - выходим

public class MainApp extends Application {

    public static String filePath = "data.xml"; // оставлен для совместимости с CLI

    private final UserService userService = new UserService();
    private final UserSession userSession = new UserSession();

    @Override
    public void start(Stage stage) {

        // 1: показываем окно входа/регистрации
        // UserService при register/authenticate работает с PostgreSQL напрямую
        LoginDialog loginDialog = new LoginDialog(userService, userSession);
        boolean loggedIn = loginDialog.show();

        if (!loggedIn) {
            // Пользователь закрыл окно без входа
            Platform.exit();
            return;
        }

        // 2: открываем главное окно
        // MainController получает userSession чтобы знать кто вошёл (для owner)
        MainController controller = new MainController(userSession);
        Scene scene = new Scene(controller.getView(), 900, 540);

        stage.setTitle("Лабораторные образцы - " + userSession.getCurrentLogin());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        if (args.length > 0) filePath = args[0];
        launch();
    }
}