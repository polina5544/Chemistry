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
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // 1: показываем окно входа/регистрации
        if (!showLoginDialog()) {
            Platform.exit();
            return;
        }

        // 2: открываем главное окно
        showMainWindow();
    }

    private boolean showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(userService, userSession);
        return loginDialog.show();
    }

    private void showMainWindow() {
        MainController controller = new MainController(userSession);
        Scene scene = new Scene(controller.getView(), 1100, 600);

        primaryStage.setTitle("Лабораторные образцы - " + userSession.getCurrentLogin());
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.close();
            userSession.logout();
            if (showLoginDialog()) {
                showMainWindow();
            } else {
                Platform.exit();
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length > 0) filePath = args[0];
        launch();
    }
}