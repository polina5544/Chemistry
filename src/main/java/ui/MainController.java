package ui;

import domain.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import service.SampleService;
import service.UserSession;



//Кнопки:
//Refresh — перечитать образцы из БД
//Add     — диалог создания нового образца (owner = текущий пользователь)
//Delete  — удалить выбранный образец из БД

public class MainController {

    // SampleService — единственный путь к данным
    private final SampleService sampleService = new SampleService();
    private final UserSession userSession;

    //  особый список JavaFX. Когда в него добавляешь или удаляешь элементы,
    //  таблица автоматически перерисовывается
    // FXCollections.observableArrayList()- фабричный метод, создаёт пустой такой список
    private final ObservableList<Sample> data = FXCollections.observableArrayList();
    private final TableView<Sample> table = new TableView<>();
    private final Label details = new Label("Выберите образец из списка");

    private final Button refreshBtn = new Button("Refresh");
    private final Button addBtn     = new Button("Add");
    private final Button deleteBtn  = new Button("Delete");

    private final BorderPane root = new BorderPane();

    public MainController(UserSession userSession) {
        this.userSession = userSession;
        setupTable();
        setupButtons();
        setupLayout();

        // При старте загружаем все образцы из БД
        refreshFromDb();
    }
    // Загрузка из БД
    // Читает все образцы из PostgreSQL и обновляет таблицу
    // Вызывается при старте и при нажатии Refresh
    private void refreshFromDb() {
        try {
            // sampleService.getAll() потом repo.findAll() и в бд  SELECT * FROM samples
                data.setAll(sampleService.getAll());
            } catch (Exception e) {
                showError("Ошибка загрузки из БД: " + e.getMessage());
        }
    }

    // Таблица
    private void setupTable() {
        // Создаём столбцы таблицы
        table.getColumns().addAll(
                makeCol("ID",       60,  s -> String.valueOf(s.getId())),
                makeCol("Name",     200, Sample::getName),
                makeCol("Type",     120, Sample::getType),
                makeCol("Location", 140, Sample::getLocation),

                // метод name возвращает название константы как тест
                makeCol("Status",   100, s -> s.getStatus().name()),
                makeCol("Owner",    120, s -> s.getOwnerUsername() != null
                        ? s.getOwnerUsername() : "—")
        );

        // Подключаем список данных к таблице
        table.setItems(data);
        //устанавливает заглушку - визуальный элемент, который будет
        //виден на экране, если таблица окажется пустой
        table.setPlaceholder(new Label("Нет образцов. Нажми Add!"));

        // При клике на строку - показываем детали справа
        // .getSelectionModel() - запрашивает у таблицы специальную модель,
        // которая управляет выделением строк (знает, какая строка сейчас активна)
        // selectedItemProperty() - обращается к конкретному свойству (наблюдаемому объекту),
        // которое хранит текущий выделенный элемент
        // При клике на строку модель заглядывает в  список данных
        // data под соответствующим индексом.
        // Она берет оттуда полноценный Java-объект со всеми его скрытыми полями
        //  и помещает его в свойство selectedItemProperty

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> showDetails(selected));

        // Чередование розовых строк
        table.setRowFactory(tv -> {
            TableRow<Sample> row = new TableRow<>();
            row.itemProperty().addListener((obs, o, item) -> {
                if (item == null) { row.setStyle(""); return; }
                row.setStyle(row.getIndex() % 2 == 0
                        ? "-fx-background-color:#FFF0F5;"
                        : "-fx-background-color:#FFE4EC;");
            });
            row.selectedProperty().addListener((obs, b, sel) -> {
                if (sel) row.setStyle("-fx-background-color:#FFB6C1;");
            });
            return row;
        });
    }

    //java.util.function.Function - встроенный интерфейс Java
    // 1. даю целый объект 2. вытаскиваю из него определенную строку
    // 3. отдаю таблице и оно станет ячейкой

    //setCellValueFactory - метод, который определяет, откуда
    // ячейки этой колонки будут брать текст

    private TableColumn<Sample, String> makeCol(String title, double width,
            java.util.function.Function<Sample, String> extractor) {

        TableColumn<Sample, String> col = new TableColumn<>(title);

        // cd - информация по текущей строке(CellDataFeatures)
        // SimpleStringProperty - специальная JavaFX-обертка для строк,
        // которая нужна таблице для отслеживания изменений

        col.setCellValueFactory(cd -> new SimpleStringProperty(extractor.apply(cd.getValue())));
        col.setPrefWidth(width);
        return col;
    }


    // Показать детали выбранного образца в правой панели
    private void showDetails(Sample s) {
        if (s == null) {
            details.setText("Выберите образец из списка");
            return;
        }
        details.setText(
                "ID: "       + s.getId()            + "\n\n" +
                        "Name: "     + s.getName()          + "\n\n" +
                        "Type: "     + s.getType()          + "\n\n" +
                        "Location: " + s.getLocation()      + "\n\n" +
                        "Status: "   + s.getStatus()        + "\n\n" +
                        "Owner: "    + (s.getOwnerUsername() != null ? s.getOwnerUsername() : "—")
        );
    }

    //Кнопки
    private void setupButtons() {

        // Refresh - перечитать из БД
        refreshBtn.setOnAction(e -> {
            refreshFromDb();
            showInfo("Данные обновлены из БД");
        });

        // Add — диалог создания нового образца
        addBtn.setOnAction(e -> {
            Dialog<Sample> dialog = new Dialog<>();
            dialog.setTitle("Новый образец");
            dialog.setHeaderText("Создаёт: " + userSession.getCurrentLogin());

            //ButtonBar.ButtonData.OK_DONE - это специальный маркер (роль кнопки)
            // Он объясняет операционной системе, что эта кнопка является
            // главной (подтверждающей)

            //ButtonType.CANCEL
            // Это готовая кнопка JavaFX, на которой автоматически будет
            // написано «Отмена». Она также автоматически закрывает окно при нажатии на Esc

            ButtonType okType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

            TextField nameField     = styledField("Например: Речная вода");
            TextField typeField     = styledField("Например: water");
            TextField locationField = styledField("Например: Лаб. 204");

            // Настройка выпадающего списка для Active/Archived
            // ComboBox - это встроенный элемент интерфейса (выпадающее меню)
            // getItems() - запрашивает у списка его внутренний перечень элементов
            // addAll() - добавляет элементы в этот перечень

            ComboBox<SampleStatus> statusBox = new ComboBox<>();
            statusBox.getItems().addAll(SampleStatus.values());
            statusBox.setValue(SampleStatus.ACTIVE);

            GridPane grid = new GridPane();
            grid.setHgap(12); grid.setVgap(10);
            grid.setPadding(new Insets(16));
            grid.add(pinkLabel("Name:"),     0, 0); grid.add(nameField,     1, 0);
            grid.add(pinkLabel("Type:"),     0, 1); grid.add(typeField,     1, 1);
            grid.add(pinkLabel("Location:"), 0, 2); grid.add(locationField, 1, 2);
            grid.add(pinkLabel("Status:"),   0, 3); grid.add(statusBox,     1, 3);
            // Owner не вводим — подставляется из сессии автоматически
            grid.add(pinkLabel("Owner:"),    0, 4);
            grid.add(new Label(userSession.getCurrentLogin()), 1, 4);

            dialog.getDialogPane().setContent(grid);

            // Кнопка OK активна только если все поля заполнены
            // lookupButton(okType) - находит физическую кнопку на панели окна
            // по её описанию okType (которую мы создали ранее).
            // Она возвращается как базовый элемент интерфейса - Node
            // setDisable(true) — сразу же блокирует эту кнопку

            javafx.scene.Node okNode = dialog.getDialogPane().lookupButton(okType);
            okNode.setDisable(true);
            Runnable check = () -> okNode.setDisable(
                    nameField.getText().isBlank() ||
                            typeField.getText().isBlank()  ||
                            locationField.getText().isBlank());
            nameField.textProperty().addListener((o, a, b) -> check.run());
            typeField.textProperty().addListener((o, a, b) -> check.run());
            locationField.textProperty().addListener((o, a, b) -> check.run());

            // Собираем объект Sample из полей диалога
            // id = 0 — заглушка, настоящий id придёт из PostgreSQL
            //setResultConverter - этот метод переводит нажатие кнопки в полноценный объект данных
            // Если нажата кнопка okType то создается и возвращается новый объект new Sample()
            dialog.setResultConverter(btn -> btn == okType
                    ? new Sample(0L,
                    nameField.getText().trim(),
                    typeField.getText().trim(),
                    locationField.getText().trim(),
                    statusBox.getValue(),
                    userSession.getCurrentLogin(), // owner из сессии!
                    java.time.Instant.now(),
                    java.time.Instant.now())
                    : null);

            // ifPresent - проверяет появился ли у нас готовый объект с данными

            dialog.showAndWait().ifPresent(sample -> {
                try {
                    sampleService.addSample(sample);
                    // перечитать реальные данные из PostgreSQL
                    refreshFromDb();
                } catch (Exception ex) {
                    showError("Ошибка добавления: " + ex.getMessage());
                }
            });
        });

        deleteBtn.setOnAction(e -> {
            Sample selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Выберите образец в таблице");
                return;
            }
            new Alert(Alert.AlertType.CONFIRMATION,
                    "Удалить «" + selected.getName() + "»?",
                    ButtonType.OK, ButtonType.CANCEL)
                    .showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.OK) {
                            try {
                                // DELETE FROM samples WHERE id = чему то
                                sampleService.deleteSample(selected.getId());

                                refreshFromDb();

                                // Очистка панели деталей справа
                                details.setText("Выберите образец из списка");
                            } catch (Exception ex) {
                                showError("Ошибка удаления: " + ex.getMessage());
                            }
                        }
                    });
        });
    }

    //Layout
    private void setupLayout() {
        // Панель деталей справа
        VBox right = new VBox(10, pinkLabel("─── Детали ───"), details);
        right.setPadding(new Insets(12));
        right.setPrefWidth(220);
        right.setStyle("-fx-background-color:#FFF0F5; " +
                "-fx-border-color:#FFB6C1; -fx-border-width:0 0 0 1;");
        details.setStyle("-fx-font-size:13px; -fx-text-fill:#8B4A62; -fx-font-family:Georgia;");
        details.setWrapText(true);

        // Кнопки снизу (Save убрана - данные сохраняются сразу в БД)
        HBox buttons = new HBox(10, refreshBtn, addBtn, deleteBtn);
        buttons.setPadding(new Insets(10));
        buttons.setStyle("-fx-background-color:#FFE4EC; " +
                "-fx-border-color:#FFB6C1; -fx-border-width:1 0 0 0;");

        String btnStyle = "-fx-background-color:#FFB6C1; -fx-text-fill:white; " +
                "-fx-font-size:13px; -fx-font-family:Georgia; " +
                "-fx-font-weight:bold; -fx-background-radius:14; -fx-padding:8 18;";
        refreshBtn.setStyle(btnStyle);
        addBtn.setStyle(btnStyle);
        deleteBtn.setStyle(btnStyle);

        root.setStyle("-fx-background-color:#FFF0F5;");
        root.setCenter(table);
        root.setRight(right);
        root.setBottom(buttons);
    }

    // Вспомогательные
    private static TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(210);
        tf.setStyle("-fx-border-color:#f48fb1; -fx-border-radius:6; " +
                "-fx-background-radius:6; -fx-padding:6 10;");
        return tf;
    }

    private static Label pinkLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#880e4f; -fx-font-weight:bold; -fx-font-family:Georgia;");
        return l;
    }

    private void showError(String msg) {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, msg).showAndWait());
    }

    private void showInfo(String msg) {
        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, msg).showAndWait());
    }

    public Pane getView() { return root; }
}