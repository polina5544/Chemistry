package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.UserService;
import service.UserSession;
import storage.StorageData;
import storage.StorageService;

public class MainApp extends Application {

    public static String filePath;
    private final UserService userService    = new UserService();
    private final UserSession    userSession    = new UserSession();
    private final StorageService storageService = new StorageService();


    @Override
    public void start(Stage stage) {

        StorageData loaded = storageService.load(filePath);
        userService.setUsers(loaded.users());

        //окно авторизации
        LoginDialog loginDialog = new LoginDialog(userService, userSession);
        boolean loggedIn = loginDialog.show();

        if (!loggedIn) {
            // Закрыли без входа - выходим
            //TODO РАЗОБРАТЬ ПЛАТФОРМ
            Platform.exit();
            return;
        }
        MainController controller = new MainController();
        Scene scene = new Scene(controller.getView(), 860, 520);
        stage.setTitle("");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Путь к файлу можно передать аргументом: java -jar app.jar data.xml
        filePath = (args.length > 0) ? args[0] : "data.xml";
        launch();
    }
}