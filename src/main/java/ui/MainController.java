package ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import domain.Sample;
import domain.SampleStatus;
import domain.Measurement;
import domain.Protocol;
import service.SampleService;
import storage.StorageService;
import storage.StorageData;
import utilits.IDgenerator;

import java.util.HashSet;
import java.util.Set;

public class MainController {

    private final StorageService storageService = new StorageService();
    private final SampleService sampleService = new SampleService();

    // Хранилища в памяти — нужны для сохранения
    private final Set<Measurement> allMeasurements = new HashSet<>();
    private final Set<Protocol> allProtocols = new HashSet<>();

    private final ObservableList<Sample> data = FXCollections.observableArrayList();

    private final TableView<Sample> table = new TableView<>();
    private final Label details = new Label("Выберите образец из списка");

    private final Button refreshBtn = new Button("Refresh");
    private final Button saveBtn    = new Button("Save");
    private final Button addBtn     = new Button("Add");
    private final Button deleteBtn  = new Button("Delete");

    private final String filePath = MainApp.filePath;
    private final BorderPane root  = new BorderPane();

    public MainController() {
        setupTable();
        setupButtons();
        setupLayout();
        loadOnStart();
    }

    // Авто-загрузка при старте, если файл существует
    private void loadOnStart() {
        try {
            StorageData loaded = storageService.load(filePath);
            applyLoadedData(loaded);
        } catch (Exception e) {
            // Файл может не существовать при первом запуске и это норм
        }
    }

    private void setupTable() {
        TableColumn<Sample, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getId())));
        idCol.setPrefWidth(50);

        TableColumn<Sample, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Sample, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getType()));
        typeCol.setPrefWidth(120);

        TableColumn<Sample, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().name()));
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, nameCol, typeCol, statusCol);
        table.setItems(data);

        // Master-detail
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                long measCount = allMeasurements.stream()
                        .filter(m -> m.getSampleId() == selected.getId())
                        .count();

                details.setText(
                        "ID: "       + selected.getId()             + "\n" +
                                "Name: "     + selected.getName()           + "\n" +
                                "Type: "     + selected.getType()           + "\n" +
                                "Location: " + selected.getLocation()       + "\n" +
                                "Status: "   + selected.getStatus()         + "\n" +
                                "Owner: "    + selected.getOwnerUsername()  + "\n" +
                                "Измерений: "+ measCount
                );
            }
        });
    }

    private void setupButtons() {

        // Refresh
        refreshBtn.setOnAction(e -> {
            try {
                StorageData loaded = storageService.load(filePath);
                applyLoadedData(loaded);
                showInfo("Данные обновлены из " + filePath);
            } catch (Exception ex) {
                showError("Ошибка загрузки: " + ex.getMessage());
            }
        });

        // Save
        saveBtn.setOnAction(e -> {
            try {
                Set<Sample> samples = new HashSet<>(sampleService.getAll());
                StorageData data = new StorageData(samples, allMeasurements, allProtocols);
                storageService.save(filePath, data);
                showInfo("Сохранено в " + filePath);
            } catch (Exception ex) {
                showError("Ошибка сохранения: " + ex.getMessage());
            }
        });

        // Add
        addBtn.setOnAction(e -> {
            Dialog<Sample> dialog = new Dialog<>();
            dialog.setTitle("Добавить образец");

            ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

            TextField nameField     = new TextField();
            TextField typeField     = new TextField();
            TextField locationField = new TextField();
            TextField ownerField    = new TextField();

            ComboBox<SampleStatus> statusBox = new ComboBox<>();
            statusBox.getItems().addAll(SampleStatus.values());
            statusBox.setValue(SampleStatus.ACTIVE);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10));

            grid.add(new Label("Name:"),     0, 0); grid.add(nameField,     1, 0);
            grid.add(new Label("Type:"),     0, 1); grid.add(typeField,     1, 1);
            grid.add(new Label("Location:"), 0, 2); grid.add(locationField, 1, 2);
            grid.add(new Label("Owner:"),    0, 3); grid.add(ownerField,    1, 3);
            grid.add(new Label("Status:"),   0, 4); grid.add(statusBox,     1, 4);

            dialog.getDialogPane().setContent(grid);

            // Активируем кнопку OK только если все поля заполнены
            javafx.scene.Node okNode = dialog.getDialogPane().lookupButton(okBtn);
            okNode.setDisable(true);
            Runnable checkFields = () -> okNode.setDisable(
                    nameField.getText().isBlank() ||
                            typeField.getText().isBlank() ||
                            locationField.getText().isBlank() ||
                            ownerField.getText().isBlank()
            );
            nameField.textProperty().addListener((o, a, b) -> checkFields.run());
            typeField.textProperty().addListener((o, a, b) -> checkFields.run());
            locationField.textProperty().addListener((o, a, b) -> checkFields.run());
            ownerField.textProperty().addListener((o, a, b) -> checkFields.run());

            dialog.setResultConverter(btn -> {
                if (btn == okBtn) {
                    return new Sample(
                            IDgenerator.nextId(),
                            nameField.getText().trim(),
                            typeField.getText().trim(),
                            locationField.getText().trim(),
                            statusBox.getValue(),
                            ownerField.getText().trim(),
                            java.time.Instant.now(),
                            java.time.Instant.now()
                    );
                }
                return null;
            });

            dialog.showAndWait().ifPresent(sample -> {
                try {
                    sampleService.addSample(sample);
                    data.add(sample);           // обновляем таблицу сразу
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        });

        // Delete
        deleteBtn.setOnAction(e -> {
            Sample selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Выберите образец в таблице");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Удалить образец «" + selected.getName() + "»?",
                    ButtonType.OK, ButtonType.CANCEL);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        sampleService.deleteSample(selected.getId());
                        data.remove(selected);
                        details.setText("Выберите образец из списка");
                    } catch (Exception ex) {
                        showError(ex.getMessage());
                    }
                }
            });
        });
    }

    // Применяем загруженные данные в память и обновляем таблицу
    private void applyLoadedData(StorageData loaded) {
        sampleService.setSamples(loaded.getSamples());

        allMeasurements.clear();
        allMeasurements.addAll(loaded.getMeasurements());

        allProtocols.clear();
        allProtocols.addAll(loaded.getProtocols());

        IDgenerator.updateAll(loaded.getSamples(), loaded.getMeasurements(), loaded.getProtocols());

        data.setAll(sampleService.getAll());
        details.setText("Выберите образец из списка");
    }

    private void setupLayout() {
        // Детали справа
        VBox right = new VBox(10, new Label("─── Детали ───"), details);
        right.setPadding(new Insets(10));
        right.setPrefWidth(220);

        // Кнопки снизу
        HBox buttons = new HBox(10, refreshBtn, saveBtn, addBtn, deleteBtn);
        buttons.setPadding(new Insets(10));

        root.setCenter(table);
        root.setRight(right);
        root.setBottom(buttons);
    }

    private void showError(String msg) {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, msg).showAndWait());
    }

    private void showInfo(String msg) {
        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, msg).showAndWait());
    }

    public Pane getView() {
        return root;
    }
}