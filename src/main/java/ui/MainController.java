package ui;

import domain.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import service.SampleService;
import service.UserSession;


//MainController - главное окно приложения
//Источник данных - PostgreSQL через SampleService потом SampleRepository

public class MainController {

    // SampleService - единственный путь к данным
    // Контроллер не работает с базой напрямую, а вызывает методы сервиса
    private final SampleService sampleService = new SampleService();

    // UserSession хранит информацию о текущем пользователе
    // Она нужна, чтобы понимать, кто создаёт образец и имеет ли пользователь право удалить его
    private final UserSession userSession;

    // ObservableList — список, который автоматически обновляет таблицу при изменении данных.
    private final ObservableList<Sample> data = FXCollections.observableArrayList();

    // Основная таблица со всеми лабораторными образцами.
    private final TableView<Sample> table = new TableView<>();

    // Label с деталями теперь находится не просто в правой панели,
    // а прямо внутри визуальной колбы.
    private final Label details = new Label("Выберите образец из списка");

    private final Button refreshBtn = new Button("Refresh");
    private final Button addBtn = new Button("Add");
    private final Button deleteBtn = new Button("Delete");
    private final Button editBtn = new Button("Edit");

    private final BorderPane root = new BorderPane();

    //  Визуализация образца
    // Это путь для рисования векторной графики (SVG Path), из которого JavaFX рисует форму колбы
    // Координаты выглядят как набор точек:
    // M - перейти в точку (move to), L - провести линию(line to), Q - плавная кривая, Z - замкнуть фигуру

    private static final String FLASK_PATH =
            "M 80 16 " +
                    "L 140 16 " +
                    "L 140 23 " +
                    "L 126 40 " +
                    "L 126 96 " +
                    "L 198 238 " +
                    "Q 214 272 178 286 " +
                    "L 42 286 " +
                    "Q 6 272 22 238 " +
                    "L 94 96 " +
                    "L 94 40 " +
                    "L 80 23 Z";

    // ИЕРАРХИЯ пошла

    // Корневой контейнер справа: колба + короткая подсказка под ней
    private final VBox visualBox = new VBox(8);

    // flaskPane - внешний контейнер, распологает элементы слоями
    // На него вешается интерактивность: наведение, клик, увеличение
    // то есть он перехватывает действие пользователя, там будут слушатели
    private final StackPane flaskPane = new StackPane();

    // flaskBody - внутренняя часть колбы
    // Она плавно меняет масштаб, что бы внешняя зона клика
    // оставалась стабильной и предсказуемой для пользователя
    private final StackPane flaskBody = new StackPane();

    // Слой с пузырьками
    // Он лежит поверх колбы и не перехватывает мышь, чтобы наведение работало по flaskPane
    private final Pane bubbleLayer = new Pane();

    //createFlaskShape() - трижды создает одну и ту же геометрию колбы,
    //но каждый слой красится по разному,
    // flaskGlow - самый нижний слой (красный/зеленый)
    private final SVGPath flaskGlow = createFlaskShape();

    // Цветная жидкость внутри колбы - средний слой (полупрозрачный, типо вода)
    private final SVGPath flaskLiquid = createFlaskShape();

    // Стеклянная внешняя обводка колбы
    private final SVGPath flaskOutline = createFlaskShape();

    private final Label visualHint = new Label();

    // Карточка с деталями выбранного образца
    private final VBox detailsCard = new VBox(6);

    // Анимация дыхания колбы
    // Timeline - встроенные в JavaFX часы для создания покадровой анимации
    // breathingAnimation: изменяет свойства scaleX и scaleY у контейнера flaskBody
    private Timeline breathingAnimation;

    // Этот таймлайн работает как генератор частиц
    // Каждые несколько миллисекунд он создает маленькие белые кружки и запускает их анимацию вверх
    private Timeline hoverBubbleGenerator;

    public MainController(UserSession userSession) {
        this.userSession = userSession;

        // При старте запускаем кэш / подготовку сервиса
        sampleService.init();

        setupTable();
        setupButtons();

        // Настраиваем визуализацию до layout,
        // чтобы потом можно было добавить готовый visualBox в правую панель
        setupVisualization();

        setupLayout();

        // При старте загружаем все образцы из БД.
        refreshFromDb();
    }


    // Читает все образцы из PostgreSQL и обновляет таблицу
    // Вызывается при старте приложения и при нажатии Refresh

    private void refreshFromDb() {
        try {
            var all = sampleService.getAll();

            System.out.println("FROM DB = " + all.size());
            for (var s : all) {
                System.out.println(s.getId() + " " + s.getName());
            }

            data.setAll(all);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }


    // Настраивает таблицу: создаёт столбцы и подключает список data
    // обрабатывает выбор строки и задаёт оформление строк

    private void setupTable() {
        table.getColumns().add(makeCol("ID", 60, s -> String.valueOf(s.getId())));
        table.getColumns().add(makeCol("Name", 200, Sample::getName));
        table.getColumns().add(makeCol("Type", 120, Sample::getType));
        table.getColumns().add(makeCol("Location", 140, Sample::getLocation));
        table.getColumns().add(makeCol("Status", 100, s -> s.getStatus().name()));
        table.getColumns().add(makeCol("Owner", 200, s -> s.getOwnerUsername() != null
                ? s.getOwnerUsername()
                : "—"));

        // Подключаем список данных к таблице.
        table.setItems(data);
        table.setPlaceholder(new Label("Нет образцов. Нажми Add!"));

        // При клике на строку:
        //   1. показываем подробности внутри колбы
        //   2. обновляем цвет и подсказку визуализации
        //   3. проверяем, можно ли текущему пользователю удалить образец

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    showDetails(selected);

                    showDetails(selected);

                    if (selected != null) {
                        for (int i = 0; i < 12; i++) {
                            createFlyingBubble();
                        }
                    }

                    // Кнопка Delete активна только если выбран образец
                    // и текущий пользователь является его владельцем
                    if (selected == null) {
                        deleteBtn.setDisable(true);
                    } else {
                        boolean isOwner = selected.getOwnerUsername() != null &&
                                selected.getOwnerUsername().equals(userSession.getCurrentLogin());
                        deleteBtn.setDisable(!isOwner);
                    }
                });

        // Чередование розовых строк

        table.setRowFactory(tv -> {
            TableRow<Sample> row = new TableRow<>();

            row.itemProperty().addListener((obs, oldItem, item) -> {
                if (item == null) {
                    row.setStyle("");
                    return;
                }

                row.setStyle(row.getIndex() % 2 == 0
                        ? "-fx-background-color:#FFF0F5;"
                        : "-fx-background-color:#FFE4EC;");
            });

            row.selectedProperty().addListener((obs, wasSelected, selected) -> {
                if (selected) {
                    row.setStyle("-fx-background-color:#FFB6C1;");
                }
            });

            return row;
        });
    }

    //Создать столбец таблицы
    // extractor - функция, которая достаёт нужное поле из объекта Sample
    // Например: Sample::getName берёт имя
    // s -> String.valueOf(s.getId()) берёт id и превращает его в строку

    private TableColumn<Sample, String> makeCol(
            String title,
            double width,
            java.util.function.Function<Sample, String> extractor
    ) {
        TableColumn<Sample, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cd -> new SimpleStringProperty(extractor.apply(cd.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    // Показывает детали выбранного образца в колбе прямо
    private void showDetails(Sample s) {
        if (s == null) {
            details.setText("Выберите образец из списка");
            updateVisualization(null);
            return;
        }

        details.setText(
                "ID: " + s.getId() + "\n\n" +
                        "Name: " + s.getName() + "\n\n" +
                        "Type: " + s.getType() + "\n\n" +
                        "Location: " + s.getLocation() + "\n\n" +
                        "Status: " + s.getStatus() + "\n\n" +
                        "Owner: " + (s.getOwnerUsername() != null ? s.getOwnerUsername() : "—")
        );

        updateVisualization(s);
    }

    private void setupButtons() {
        // Refresh — перечитать данные из БД.
        refreshBtn.setOnAction(e -> {
            refreshFromDb();
            showInfo("Данные обновлены из БД");
        });

        // Add - диалог создания нового образца.
        addBtn.setOnAction(e -> {
            Dialog<Sample> dialog = new Dialog<>();
            dialog.setTitle("Новый образец");
            dialog.setHeaderText("Создаёт: " + userSession.getCurrentLogin());

            ButtonType okType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

            TextField nameField = styledField("Например: Речная вода");
            TextField typeField = styledField("Например: water");
            TextField locationField = styledField("Например: лаба в аквариуме");

            ComboBox<SampleStatus> statusBox = new ComboBox<>();
            statusBox.getItems().addAll(SampleStatus.values());
            statusBox.setValue(SampleStatus.ACTIVE);

            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(10);
            grid.setPadding(new Insets(16));

            grid.add(pinkLabel("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(pinkLabel("Type:"), 0, 1);
            grid.add(typeField, 1, 1);
            grid.add(pinkLabel("Location:"), 0, 2);
            grid.add(locationField, 1, 2);
            grid.add(pinkLabel("Status:"), 0, 3);
            grid.add(statusBox, 1, 3);

            // Owner не вводим вручную
            // Он подставляется из текущей сессии автоматически
            grid.add(pinkLabel("Owner:"), 0, 4);
            grid.add(new Label(userSession.getCurrentLogin()), 1, 4);

            dialog.getDialogPane().setContent(grid);

            // Кнопка OK активна только если все поля заполнены
            javafx.scene.Node okNode = dialog.getDialogPane().lookupButton(okType);
            okNode.setDisable(true);

            Runnable check = () -> okNode.setDisable(
                    nameField.getText().isBlank() ||
                            typeField.getText().isBlank() ||
                            locationField.getText().isBlank()
            );

            nameField.textProperty().addListener((o, a, b) -> check.run());
            typeField.textProperty().addListener((o, a, b) -> check.run());
            locationField.textProperty().addListener((o, a, b) -> check.run());

            // Собираем объект Sample из полей диалога
            // id = 0 - временная заглушка.
            // Настоящий id придёт из PostgreSQL после сохранения

            dialog.setResultConverter(btn -> btn == okType
                    ? new Sample(
                    0L,
                    nameField.getText().trim(),
                    typeField.getText().trim(),
                    locationField.getText().trim(),
                    statusBox.getValue(),
                    userSession.getCurrentLogin(),
                    java.time.Instant.now(),
                    java.time.Instant.now()
            )
                    : null);

            dialog.showAndWait().ifPresent(sample -> {
                try {
                    sampleService.addSample(sample);

                    // После добавления перечитываем реальные данные из PostgreSQL
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

            if (selected.getOwnerUsername() == null ||
                    !selected.getOwnerUsername().equals(userSession.getCurrentLogin())) {
                showError("У Вас нет прав владельца, необходимых для удаления этого образца");
                return;
            }

            new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Удалить «" + selected.getName() + "»?",
                    ButtonType.OK,
                    ButtonType.CANCEL
            ).showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        // DELETE FROM samples WHERE id = ?
                        sampleService.deleteSample(selected.getId());

                        refreshFromDb();
                        details.setText("       Выберите образец из списка");
                        updateVisualization(null);
                    } catch (Exception ex) {
                        showError("Ошибка удаления: " + ex.getMessage());
                    }
                }

                // Изначально кнопка Delete неактивна
                // После удаления тоже выключаем её, потому что выбранного объекта уже нет
                deleteBtn.setDisable(true);
            });
        });

        deleteBtn.setDisable(true);

        editBtn.setOnAction(e -> {
            Sample selected = table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showError("Выберите образец");
                return;
            }
            showEditDialog(selected);
        });

        editBtn.setDisable(true);

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    showDetails(selected);

                    if (selected != null) {
                        for (int i = 0; i < 12; i++) {
                            createFlyingBubble();
                        }
                    }

                    if (selected == null) {
                        deleteBtn.setDisable(true);
                        editBtn.setDisable(true);
                    } else {
                        boolean isOwner = selected.getOwnerUsername() != null &&
                                selected.getOwnerUsername().equals(userSession.getCurrentLogin());
                        deleteBtn.setDisable(!isOwner);
                        editBtn.setDisable(false);
                    }
                });
    }

    private void showEditDialog(Sample sample) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Редактирование образца: " + sample.getName());
        dialog.setHeaderText("Выберите действие");

        ButtonType editSampleBtn = new ButtonType("Изменить образец");
        ButtonType addMeasurementBtn = new ButtonType("Добавить измерение");
        ButtonType cancelBtn = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(editSampleBtn, addMeasurementBtn, cancelBtn);

        // красота
        dialog.getDialogPane().lookupButton(editSampleBtn).setStyle(
                "-fx-background-color: #d63384; -fx-text-fill: white; -fx-font-weight: bold;"
        );
        dialog.getDialogPane().lookupButton(addMeasurementBtn).setStyle(
                "-fx-background-color: #f48fb1; -fx-text-fill: #880e4f; -fx-font-weight: bold;"
        );

        dialog.setResultConverter(btn -> {
            if (btn == editSampleBtn) {
                showEditSampleDialog(sample);
            } else if (btn == addMeasurementBtn) {
                showAddMeasurementDialog(sample);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showEditSampleDialog(Sample sample) {
        // чекаем права на изменение образца
        if (sample.getOwnerUsername() == null ||
                !sample.getOwnerUsername().equals(userSession.getCurrentLogin())) {
            showError("Только владелец может редактировать образец");
            return;
        }

        Dialog<Sample> dialog = new Dialog<>();
        dialog.setTitle("Редактирование образца");
        dialog.setHeaderText("Редактирует: " + userSession.getCurrentLogin());

        ButtonType saveType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nameField = styledField(sample.getName());
        TextField typeField = styledField(sample.getType());
        TextField locationField = styledField(sample.getLocation());
        ComboBox<SampleStatus> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(SampleStatus.values());
        statusBox.setValue(sample.getStatus());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(pinkLabel("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(pinkLabel("Type:"), 0, 1);
        grid.add(typeField, 1, 1);
        grid.add(pinkLabel("Location:"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(pinkLabel("Status:"), 0, 3);
        grid.add(statusBox, 1, 3);
        grid.add(pinkLabel("Owner:"), 0, 4);
        grid.add(new Label(userSession.getCurrentLogin()), 1, 4);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveNode = dialog.getDialogPane().lookupButton(saveType);
        saveNode.setDisable(true);

        Runnable check = () -> saveNode.setDisable(
                nameField.getText().isBlank() ||
                        typeField.getText().isBlank() ||
                        locationField.getText().isBlank()
        );

        nameField.textProperty().addListener((o, a, b) -> check.run());
        typeField.textProperty().addListener((o, a, b) -> check.run());
        locationField.textProperty().addListener((o, a, b) -> check.run());

        dialog.setResultConverter(btn -> btn == saveType ? new Sample(
                sample.getId(),
                nameField.getText().trim(),
                typeField.getText().trim(),
                locationField.getText().trim(),
                statusBox.getValue(),
                userSession.getCurrentLogin(),
                sample.getCreatedAt(),
                java.time.Instant.now()
        ) : null);

        dialog.showAndWait().ifPresent(updatedSample -> {
            try {
                sampleService.updateSample(updatedSample);
                refreshFromDb();
                showInfo("Образец обновлён");
            } catch (Exception ex) {
                showError("Ошибка обновления: " + ex.getMessage());
            }
        });
    }
    private void showAddMeasurementDialog(Sample sample) {
        Dialog<Measurement> dialog = new Dialog<>();
        dialog.setTitle("Добавление измерения");
        dialog.setHeaderText("Образец: " + sample.getName());

        ButtonType addType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        ComboBox<MeasurementParam> paramBox = new ComboBox<>();
        paramBox.getItems().addAll(MeasurementParam.values());
        paramBox.setPromptText("Выберите параметр");

        TextField valueField = styledField("Значение (число)");

        TextField unitField = styledField("Единицы измерения (например, °C)");

        TextField methodField = styledField("Метод измерения");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        grid.add(pinkLabel("Параметр:"), 0, 0);
        grid.add(paramBox, 1, 0);
        grid.add(pinkLabel("Значение:"), 0, 1);
        grid.add(valueField, 1, 1);
        grid.add(pinkLabel("Единицы:"), 0, 2);
        grid.add(unitField, 1, 2);
        grid.add(pinkLabel("Метод:"), 0, 3);
        grid.add(methodField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node addNode = dialog.getDialogPane().lookupButton(addType);
        addNode.setDisable(true);

        Runnable check = () -> addNode.setDisable(
                paramBox.getValue() == null ||
                        valueField.getText().isBlank() ||
                        unitField.getText().isBlank() ||
                        methodField.getText().isBlank()
        );

        paramBox.valueProperty().addListener((o, a, b) -> check.run());
        valueField.textProperty().addListener((o, a, b) -> check.run());
        unitField.textProperty().addListener((o, a, b) -> check.run());
        methodField.textProperty().addListener((o, a, b) -> check.run());

        dialog.setResultConverter(btn -> {
            if (btn == addType) {
                try {
                    double value = Double.parseDouble(valueField.getText().trim());
                    Measurement measurement = new Measurement(
                            sample.getId(), // sampleId
                            paramBox.getValue(),
                            value,
                            unitField.getText().trim(),
                            methodField.getText().trim(),
                            java.time.Instant.now(),
                            userSession.getCurrentLogin()
                    );
                    return measurement;
                } catch (NumberFormatException e) {
                    showError("Значение должно быть числом");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(measurement -> {
            try {
                sampleService.addMeasurement(measurement);
                showInfo("Измерение добавлено");
            } catch (Exception ex) {
                showError("Ошибка добавления измерения: " + ex.getMessage());
            }
        });
    }

    // Настраивает визуальное представление образца
    // Здесь создаётся колба:
    // В JavaFX элементы, добавленные позже, рисуются выше остальных!

    private void setupVisualization() {
        // flaskGlow - мягкое цветное свечение вокруг сосуда
        setupFlaskGlow();
        // flaskLiquid  - цветная жидкость, зависящая от статуса
        setupFlaskLiquid();
        // flaskOutline - стеклянная обводка
        setupFlaskOutline();
        // detailsCard  - карточка с деталями выбранного образца поверх колбы
        setupDetailsCard();

        bubbleLayer.setPrefSize(260, 330);
        bubbleLayer.setMaxSize(260, 330);


        flaskBody.setPrefSize(360, 330);
        flaskBody.setMaxSize(460, 330);
        //getChildren() - это метод, который возвращает специальный список типа
        // ObservableList<Node>. Этот список хранит в себе всех детей, которые
        // этот контейнер должен отобразить на экране
        flaskBody.getChildren().addAll(
                flaskGlow,
                flaskLiquid,
                flaskOutline,
                detailsCard,
                bubbleLayer
        );

        // Детали как будто лучше смотрятся в широкой нижней части колбы,
        // поэтому карточку немного опущу вниз, если что поправишь
        StackPane.setAlignment(detailsCard, Pos.CENTER);
        detailsCard.setTranslateY(48);

        // Внешний контейнер чуть больше, чтобы пузырькам было куда вылетать вверх)
        flaskPane.setPrefSize(280, 350);
        flaskPane.setMaxSize(280, 350);
        flaskPane.getChildren().add(flaskBody);

        visualBox.setAlignment(Pos.CENTER);
        visualBox.setPadding(new Insets(8));
        visualBox.getChildren().addAll(flaskPane, visualHint);


        setupBreathingAnimation();
        setupFlaskMouseInteraction();

        // Пока образец не выбран, показываем нейтральное состояние
        updateVisualization(null);
    }


    // Создаёт SVGPath с формой колбы

    private static SVGPath createFlaskShape() {
        SVGPath path = new SVGPath();
        path.setContent(FLASK_PATH);
        return path;
    }

    // Используется цветовая модель RGB (Red, Green, Blue)
    // с добавлением Альфа-канала (прозрачности)
    // Потом цвет свечения будет обновляться в updateVisualization()

    private void setupFlaskGlow() {
        flaskGlow.setFill(Color.rgb(180, 180, 180, 0.12));
        // контур фигуры прозрачный
        flaskGlow.setStroke(Color.TRANSPARENT);
        // Добавляет графический эффект падающей тени, который в данном случае
        // превращается в эффект неонового свечения
        // 34 - радиус размытия
        flaskGlow.setEffect(new DropShadow(45, Color.rgb(180, 180, 180, 0.55)));
    }

    /**
     * Настраивает жидкость внутри колбы
     *
     * Я использую LinearGradient, потому что однотонная заливка выглядит слишком плоско
     * Градиент даёт лёгкий переход цвета и создаёт ощущение стекла/жидкости
     */
    private void setupFlaskLiquid() {
        flaskLiquid.setStroke(Color.TRANSPARENT);
        flaskLiquid.setOpacity(0.88);
    }

    /**
     * Настраивает внешнюю стеклянную обводку.
     *
     * Бело-розовая полупрозрачная обводка делает колбу похожей на стеклянный объект
     * Обводка специально толстая, чтобы силуэт был хорошо виден на розовом фоне приложения
     */
    private void setupFlaskOutline() {
        flaskOutline.setFill(Color.TRANSPARENT);
        flaskOutline.setStroke(Color.rgb(255, 238, 246, 0.95));
        flaskOutline.setStrokeWidth(5);
    }

    /**
     * Настраивает карточку с деталями.
     *
     * Карточка лежит поверх колбы, как отдельное белое окошко.
     * Белые декоративные полосы больше не рисуются поверх неё,
     * поэтому текст не перекрывается и читается нормально.
     */
    private void setupDetailsCard() {
        Label cardTitle = pinkLabel("Детали образца");
        cardTitle.setStyle("-fx-text-fill:#880e4f; -fx-font-weight:bold; -fx-font-size:13px;");

        details.setStyle("-fx-font-size:12px; -fx-text-fill:#5f2740; -fx-font-family:Georgia;");
        details.setWrapText(true);
        details.setMaxWidth(280);

        detailsCard.setAlignment(Pos.CENTER);
        detailsCard.setMaxWidth(290);
        detailsCard.setMaxHeight(220);
        detailsCard.setPadding(new Insets(12));
        detailsCard.setMouseTransparent(true);
        detailsCard.setStyle(
                "-fx-background-color:rgba(255,255,255,0.90); " +
                        "-fx-background-radius:14; " +
                        "-fx-border-color:rgba(255,182,193,0.85); " +
                        "-fx-border-radius:14; " +
                        "-fx-border-width:1;"
        );
        detailsCard.getChildren().addAll(cardTitle, details);
    }

    //Timeline это главный управляющий класс для создания анимаций и таймеров
    // Если KeyFrame - это отдельная точка на временной шкале, то Timeline - это вся шкала целиком

    private void setupBreathingAnimation() {
        breathingAnimation = new Timeline(
                //KeyFrame (ключевой кадр)  это объект, который определяет состояние
                // свойств элемента в конкретный момент времени внутри анимации
                // KeyValue - список изменяемых свойств (изменить масштаб до чего то к этой секунде)
                new KeyFrame(Duration.ZERO,
                        new KeyValue(flaskBody.scaleXProperty(), 1.0),
                        new KeyValue(flaskBody.scaleYProperty(), 1.0)
                ),
                new KeyFrame(Duration.seconds(1.45),
                        new KeyValue(flaskBody.scaleXProperty(), 1.025),
                        new KeyValue(flaskBody.scaleYProperty(), 1.025)
                ),
                new KeyFrame(Duration.seconds(2.9),
                        new KeyValue(flaskBody.scaleXProperty(), 1.0),
                        new KeyValue(flaskBody.scaleYProperty(), 1.0)
                )
        );

        breathingAnimation.setCycleCount(Animation.INDEFINITE);
        breathingAnimation.play();
    }

    /**
     * Настраивает взаимодействие мышью
     *
     * Наведение:
     *   - колба немного увеличивается
     *   - запускается генератор пузырьков
     *
     * Уход мыши:
     *   - колба возвращается к обычному размеру;
     *   - новые пузырьки перестают появляться.
     *
     * Клик:
     *   - ставит дыхание на паузу или запускает снова
     */
    private void setupFlaskMouseInteraction() {
        flaskPane.setOnMouseEntered(e -> {
            animateScale(1.05);
            startHoverBubbles();
        });

        flaskPane.setOnMouseExited(e -> {
            animateScale(1.0);
            stopHoverBubbles();
        });
    }

    /**
     * Запускает генератор пузырьков.
     *
     * Сам генератор - это Timeline, который каждые 160 миллисекунд
     * создаёт новый пузырёк. Пока мышь находится над колбой,
     * пузырьки продолжают появляться
     */
    private void startHoverBubbles() {
        stopHoverBubbles();
        createFlyingBubble();

        hoverBubbleGenerator = new Timeline(
                new KeyFrame(Duration.millis(160), e -> createFlyingBubble())
        );
        hoverBubbleGenerator.setCycleCount(Animation.INDEFINITE);
        hoverBubbleGenerator.play();
    }

    /**
     * Останавливает создание новых пузырьков
     *
     * Уже созданные пузырьки не исчезают резко:
     * они спокойно долетают по своей анимации и удаляются сами
     */
    private void stopHoverBubbles() {
        if (hoverBubbleGenerator != null) {
            hoverBubbleGenerator.stop();
            hoverBubbleGenerator = null;
        }
    }

    private void createFlyingBubble() {
        double startX = 130 + randomBetween(-18, 18);
        double startY = 42 + randomBetween(-4, 8);
        double endX = startX + randomBetween(-34, 34);
        double endY = -28 + randomBetween(-18, 10);
        double radius = randomBetween(3.5, 7.0);
        double duration = randomBetween(0.75, 1.25);

        Circle flyingBubble = new Circle(radius);
        flyingBubble.setCenterX(startX);
        flyingBubble.setCenterY(startY);
        flyingBubble.setFill(Color.rgb(255, 255, 255, 0.70));
        flyingBubble.setStroke(Color.rgb(255, 255, 255, 0.95));
        flyingBubble.setStrokeWidth(1);

        bubbleLayer.getChildren().add(flyingBubble);

        Timeline bubbleFlight = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(flyingBubble.centerXProperty(), startX),
                        new KeyValue(flyingBubble.centerYProperty(), startY),
                        new KeyValue(flyingBubble.opacityProperty(), 0.0),
                        new KeyValue(flyingBubble.radiusProperty(), radius * 0.65)
                ),
                new KeyFrame(Duration.seconds(duration * 0.25),
                        new KeyValue(flyingBubble.opacityProperty(), 1.0),
                        new KeyValue(flyingBubble.radiusProperty(), radius)
                ),
                new KeyFrame(Duration.seconds(duration),
                        new KeyValue(flyingBubble.centerXProperty(), endX),
                        new KeyValue(flyingBubble.centerYProperty(), endY),
                        new KeyValue(flyingBubble.opacityProperty(), 0.0),
                        new KeyValue(flyingBubble.radiusProperty(), radius * 0.35)
                )
        );

        //Команда setOnFinished ждёт, пока таймлайн доиграет до конца,
        // и затем начисто удаляет этот конкретный кружок из списка getChildren()

        bubbleFlight.setOnFinished(e -> bubbleLayer.getChildren().remove(flyingBubble));
        bubbleFlight.play();
    }

    /**
     * Обновляет визуализацию при выборе образца
     *
     * Если sample = null то
     *   колба становится нейтральной серой
     *   внутри написано, что нужно выбрать образец
     * Если sample выбран:
     *    ACTIVE окрашивает жидкость и свечение в зелёный
     *   любой другой статус окрашивает жидкость и свечение в красный
     *    tooltip получает данные выбранного образца
     *    details уже содержит текст, который виден внутри колбы
     */
    private void updateVisualization(Sample sample) {
        if (sample == null) {
            applyFlaskColor(Color.rgb(185, 185, 185), true);
            visualHint.setText("Выберите образец для визуализации");
            return;
        }

        Color statusColor = getStatusColor(sample.getStatus());
        applyFlaskColor(statusColor, false);


    }

    private void applyFlaskColor(Color baseColor, boolean neutral) {
        double alpha = neutral ? 0.42 : 0.74;

        // градиент идет по диагонали из верхнего левого угла в нижный правый
        // true - координаты 0 и 1 это проценты от рамера колбы
        // Если колба растянетсяю/сожмётся градиент автоматически подстроится
        flaskLiquid.setFill(new LinearGradient(
                0, 0,
                1, 1,
                true,
                // просто формальность для компилятора из за конструктора градиента
                CycleMethod.NO_CYCLE,
                // белый прозрачный цвет для блика
                new Stop(0.00, Color.rgb(255, 255, 255, 0.70)),
                new Stop(0.32, colorWithOpacity(baseColor, alpha)),
                new Stop(1.00, colorWithOpacity(baseColor.darker(), alpha))
        ));

        flaskGlow.setFill(colorWithOpacity(baseColor, neutral ? 0.10 : 0.16));
        flaskGlow.setEffect(new DropShadow(38, colorWithOpacity(baseColor, neutral ? 0.35 : 0.72)));
    }

    // зеленый цвет возвразщает иначе красный
    private Color getStatusColor(SampleStatus status) {
        if (status != null && "ACTIVE".equals(status.name())) {
            return Color.rgb(58, 205, 112);
        }

        return Color.rgb(230, 82, 91);
    }

    /**
     * Создаёт копию цвета с другой прозрачностью
     *
     * В JavaFX Color неизменяемый, поэтому мы не меняем старый объект,
     * а создаём новый с теми же RGB-компонентами и новым opacity
     */
    private Color colorWithOpacity(Color color, double opacity) {
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                opacity
        );
    }

    /**
     * Простая функция для случайных чисел.
     *
     * Она нужна пузырькам, чтобы они летели не строго по одной линии,
     * а каждый раз чуть по-разному.
     */
    private double randomBetween(double min, double max) {
        return min + Math.random() * (max - min);
    }

    /**
     * Плавно меняет размер визуализации при наведении мыши.
     *
     * ScaleTransition используется отдельно от дыхания:
     * дыхание работает на flaskBody, а наведение - на flaskPane
     * Поэтому эффекты не мешают друг другу
     */
    private void animateScale(double value) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(160), flaskPane);
        scale.setToX(value);
        scale.setToY(value);
        scale.play();
    }

    /**
     * Настройка расположения элементов на главном экране.
     */
    private void setupLayout() {
        // Правая панель теперь посвящена визуализации.
        // Детали образца находятся не отдельным блоком, а прямо внутри колбы.
        VBox right = new VBox(
                10,
                pinkLabel("  Визуализация  "),
                visualBox
        );

        right.setAlignment(Pos.TOP_CENTER);
        right.setPadding(new Insets(12));
        right.setPrefWidth(300);
        right.setStyle("-fx-background-color:#FFF0F5; " +
                "-fx-border-color:#FFB6C1; -fx-border-width:0 0 0 1;");

        // Кнопки снизу
        // Save убрана, потому что данные сохраняются сразу в БД
        HBox buttons = new HBox(10, refreshBtn, addBtn, editBtn, deleteBtn);
        buttons.setPadding(new Insets(10));
        buttons.setStyle("-fx-background-color:#FFE4EC; " +
                "-fx-border-color:#FFB6C1; -fx-border-width:1 0 0 0;");

        String btnStyle = "-fx-background-color:#FFB6C1; -fx-text-fill:white; " +
                "-fx-font-size:13px; -fx-font-family:Georgia; " +
                "-fx-font-weight:bold; -fx-background-radius:14; -fx-padding:8 18;";

        refreshBtn.setStyle(btnStyle);
        addBtn.setStyle(btnStyle);
        deleteBtn.setStyle(btnStyle);
        editBtn.setStyle(btnStyle);

        root.setStyle("-fx-background-color:#FFF0F5;");
        root.setCenter(table);
        root.setRight(right);
        root.setBottom(buttons);
    }

    /**
     * Создаёт красиво оформленное текстовое поле
     */
    private static TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(210);
        tf.setStyle("-fx-border-color:#f48fb1; -fx-border-radius:6; " +
                "-fx-background-radius:6; -fx-padding:6 10;");
        return tf;
    }

    /**
     * Создаёт розовую подпись
     * Используется для заголовков и подписей в форме
     */
    private static Label pinkLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#880e4f; -fx-font-weight:bold; -fx-font-family:Georgia;");
        return l;
    }

    /**
     * Показывает окно ошибки
     * Platform.runLater нужен, чтобы Alert точно открывался в JavaFX-потоке
     */
    private void showError(String msg) {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, msg).showAndWait());
    }

    /**
     * Показывает информационное окно
     */
    private void showInfo(String msg) {
        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, msg).showAndWait());
    }

    /**
     * Возвращает корневую панель
     * MainApp использует этот метод, чтобы поставить интерфейс в Scene
     */
    public Pane getView() {
        return root;
    }
}
