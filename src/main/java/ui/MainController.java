package ui;

import javafx.application.Platform;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;

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
        setupTableColumns();
        setupTableData();
        setupSelectionListener();
    }

    private void setupTableColumns() {
        TableColumn<Sample, String> idColumn = createColumn("ID", 50,
                sample -> String.valueOf(sample.getId()));
        TableColumn<Sample, String> nameColumn = createColumn("Name", 200,
                Sample::getName);
        TableColumn<Sample, String> typeColumn = createColumn("Type", 120,
                Sample::getType);
        TableColumn<Sample, String> statusColumn = createColumn("Status", 100,
                sample -> sample.getStatus().name());
        table.getColumns().addAll(idColumn, nameColumn, typeColumn, statusColumn);
    }

    private TableColumn<Sample, String> createColumn(String title, double width,
                                                     java.util.function.Function<Sample, String> extractor) {
        TableColumn<Sample, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            Sample sample = cellData.getValue();
            return new SimpleStringProperty(extractor.apply(sample));
        });
        column.setPrefWidth(width);
        return column;
    }

    private void setupTableData() {table.setItems(data);}

    private void setupSelectionListener() {
        table.getSelectionModel().selectedItemProperty().addListener
                ((obs, oldVal, selected) -> {
            updateDetailsPanel(selected);
        });
    }

    private void updateDetailsPanel(Sample selected) {
        if (selected == null) {
            details.setText("Выберите образец из списка");
            return;
        }

        long measCount = countMeasurementsForSample(selected.getId());
        details.setText(buildDetailsText(selected, measCount));
    }

    private long countMeasurementsForSample(long sampleId) {
        return allMeasurements.stream()
                .filter(m -> m.getSampleId() == sampleId)
                .count();
    }

    private String buildDetailsText(Sample sample, long measurementsCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(sample.getId()).append("\n");
        sb.append("Name: ").append(sample.getName()).append("\n");
        sb.append("Type: ").append(sample.getType()).append("\n");
        sb.append("Location: ").append(sample.getLocation()).append("\n");
        sb.append("Status: ").append(sample.getStatus()).append("\n");
        sb.append("Owner: ").append(sample.getOwnerUsername()).append("\n");
        sb.append("Измерений: ").append(measurementsCount);
        return sb.toString();
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
        root.setStyle("""
    -fx-background-color: #FFF0F5;
""");

        table.setStyle("""
    -fx-background-color: white;
    -fx-border-color: #FFB6C1;
    -fx-border-radius: 8;
""");
        table.setStyle("""
    -fx-background-color: transparent;
    -fx-control-inner-background: #FFF5F8;
    -fx-table-cell-border-color: transparent;
    -fx-padding: 5;
""");

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Sample item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                }
                else if (getIndex() % 2 == 0) {

                    // светло-розовый
                    setStyle("""
                -fx-background-color:#FFF0F5;
            """);
                }
                else {

                    // чуть темнее розовый
                    setStyle("""
                -fx-background-color:#FFE4EC;
            """);
                }
            }
        });
        table.setRowFactory(tv -> {
            TableRow<Sample> row = new TableRow<>();

            row.itemProperty().addListener((obs, oldItem, item) -> {
                if (item == null) return;

                if (row.getIndex() % 2 == 0)
                    row.setStyle("-fx-background-color:#FFF0F5;");
                else
                    row.setStyle("-fx-background-color:#FFE4EC;");
            });

            row.selectedProperty().addListener((obs,b,c)->{
                if(c)
                    row.setStyle("""
                -fx-background-color:#FFB6C1;
                -fx-text-fill:white;
            """);
            });

            return row;
        });

        details.setStyle("""
    -fx-font-size: 14px;
    -fx-text-fill: #8B4A62;
""");

        String buttonStyle = """
    -fx-background-color: #FFB6C1;
    -fx-text-fill: white;
    -fx-font-size: 14px;
    -fx-background-radius: 12;
    -fx-padding: 8 16;
""";

        refreshBtn.setStyle(buttonStyle);
        saveBtn.setStyle(buttonStyle);
        addBtn.setStyle(buttonStyle);
        deleteBtn.setStyle(buttonStyle);

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