package ui;

import domain.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import service.UserService;
import service.UserSession;

import java.util.Optional;

//LoginDialog - окно авторизации/регистрации.
// Показывается ДО главного окна (в MainApp.start()).
// Пользователь выбирает - войти или зарегистрироваться
// Если закрыть окно без входа - приложение завершается

public class LoginDialog {

    private final UserService userService;
    private final UserSession session;

    // Флаг: переключаемся между режимами Вход и Регистрация
    private boolean isRegisterMode = false;

    public LoginDialog(UserService userService, UserSession session) {
        this.userService = userService;
        this.session     = session;
    }

    public boolean show() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Лабораторная система");

        // Запрещаем закрытие крестиком без входа
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        // Скрываем стандартные кнопки - сделаем свои
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);

        // Заголовок
        Label titleLabel = new Label("Добро пожаловать!");
        titleLabel.setFont(Font.font("Georgia", 20));
        titleLabel.setTextFill(Color.web("#d63384"));

        Label subtitleLabel = new Label("НОЦ «Инфохимии» · 2026");
        subtitleLabel.setFont(Font.font("Georgia", 12));
        subtitleLabel.setTextFill(Color.web("#f48fb1"));

        VBox header = new VBox(4, titleLabel, subtitleLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 16, 0));

        // Поля ввода
        TextField loginField    = styledField("Ваш логин");
        PasswordField passField = styledPassword("Пароль");
        PasswordField passRepeatField = styledPassword("Повторите пароль");

        Label passRepeatLabel = new Label("Повторите пароль:");
        passRepeatLabel.setStyle("-fx-text-fill: #880e4f; -fx-font-weight: bold;");
        passRepeatField.setVisible(false);
        passRepeatLabel.setVisible(false);
        passRepeatField.setManaged(false); // не занимает место когда скрыт
        passRepeatLabel.setManaged(false);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #d63384; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        // Кнопки
        Button actionBtn = new Button("Войти");
        actionBtn.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #e91e8c, #d63384);
                -fx-text-fill: white;
                -fx-font-family: Georgia;
                -fx-font-weight: bold;
                -fx-font-size: 14px;
                -fx-background-radius: 20;
                -fx-padding: 10 30;
                -fx-cursor: hand;
                """);

        // Кнопка переключения режима
        Button toggleBtn = new Button("Нет аккаунта? Зарегистрироваться");
        toggleBtn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #d63384;
                -fx-font-size: 12px;
                -fx-cursor: hand;
                -fx-underline: true;
                """);

        // Заголовок режима
        Label modeLabel = new Label("Вход в систему");
        modeLabel.setFont(Font.font("Georgia", 15));
        modeLabel.setTextFill(Color.web("#880e4f"));

        //Переключение режима
        toggleBtn.setOnAction(e -> {
            isRegisterMode = !isRegisterMode;
            if (isRegisterMode) {
                modeLabel.setText("Регистрация");
                actionBtn.setText("Зарегистрироваться");
                toggleBtn.setText("Уже есть аккаунт? Войти");
                passRepeatField.setVisible(true);
                passRepeatLabel.setVisible(true);
                passRepeatField.setManaged(true);
                passRepeatLabel.setManaged(true);
            } else {
                modeLabel.setText("Вход в систему");
                actionBtn.setText("Войти");
                toggleBtn.setText("Нет аккаунта? Зарегистрироваться");
                passRepeatField.setVisible(false);
                passRepeatLabel.setVisible(false);
                passRepeatField.setManaged(false);
                passRepeatLabel.setManaged(false);
            }
            errorLabel.setText("");
            dialog.getDialogPane().getScene().getWindow().sizeToScene();
        });

        // Логика кнопки Войти/Зарегистрироваться
        actionBtn.setOnAction(e -> {
            String login = loginField.getText().trim();
            String pass  = passField.getText().trim();
            errorLabel.setText("");

            try {
                if (isRegisterMode) {
                    // Режим регистрации
                    String passRepeat = passRepeatField.getText().trim();
                    if (!pass.equals(passRepeat)) {
                        errorLabel.setText("Пароли не совпадают");
                        return;
                    }
                    // UserService.register() проверит уникальность и сложность пароля
                    // User-конструктор внутри захеширует пароль через SHA-256
                    User user = userService.register(login, pass);
                    session.login(user); // автовход после регистрации
                } else {
                    // Режим входа
                    // authenticate хеширует введённый пароль и сравнивает с хранимым хешем
                    User user = userService.authenticate(login, pass);
                    session.login(user);
                }

                dialog.setResult(true);
                dialog.close();

            } catch (IllegalArgumentException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(8, 0, 8, 0));

        grid.add(pinkLabel("Логин:"), 0, 0);    grid.add(loginField,       1, 0);
        grid.add(pinkLabel("Пароль:"), 0, 1);   grid.add(passField,        1, 1);
        grid.add(passRepeatLabel, 0, 2);         grid.add(passRepeatField,  1, 2);

        VBox content = new VBox(12,
                header,
                modeLabel,
                grid,
                errorLabel,
                actionBtn,
                toggleBtn
        );
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24, 32, 16, 32));
        content.setStyle("""
                -fx-background-color: #fff0f6;
                """);
        content.setPrefWidth(380);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle("""
                -fx-background-color: #fff0f6;
                -fx-border-color: #f8bbd0;
                -fx-border-width: 2;
                """);

        // Enter в поле пароля это нажать кнопку
        passField.setOnAction(e -> actionBtn.fire());
        loginField.setOnAction(e -> passField.requestFocus());

        Optional<Boolean> result = dialog.showAndWait();
        return result.isPresent() && result.get();
    }

    // Вспомогательные методы
    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(200);
        tf.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #f48fb1;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-border-width: 1.5;
                -fx-padding: 7 12;
                -fx-font-family: Georgia;
                """);
        return tf;
    }

    private PasswordField styledPassword(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setPrefWidth(200);
        pf.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #f48fb1;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-border-width: 1.5;
                -fx-padding: 7 12;
                -fx-font-family: Georgia;
                """);
        return pf;
    }

    private Label pinkLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #880e4f; -fx-font-weight: bold; -fx-font-family: Georgia;");
        return l;
    }
}