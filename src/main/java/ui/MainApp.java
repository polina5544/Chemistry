package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static String filePath;

    @Override
    public void start(Stage stage) {
        MainController controller = new MainController();
        Scene scene = new Scene(controller.getView(), 860, 520);
        stage.setTitle("Лабораторные образцы — НОЦ Инфохимии");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Путь к файлу можно передать аргументом: java -jar app.jar data.xml
        filePath = (args.length > 0) ? args[0] : "data.xml";
        launch();
    }
}