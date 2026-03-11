package cli;

/**
 * meas_add <id> - добавить измерение к образцу
 */

import java.io.*;
import java.time.Instant;
import java.util.*;
import domain.*;
import service.SampleService;
import validation.MeasurmentValidation;

public class MeasurementAddCommand extends Command {
    private final SampleService sampleService;
    private final List<Measurement> allMeasurements;
    private long nextMeasurementId = 1;
    private long currentSampleId;
    private long sampleId;

    public MeasurementAddCommand(SampleService sampleService, List<Measurement> allMeasurements) {
        this.sampleService = sampleService;
        this.allMeasurements = allMeasurements;
        this.requiredAdditionalInput = true;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Ошибка: укажите ID образца");
        }
        if (args.length > 1) {
            throw new IllegalArgumentException("Ошибка: meas_add принимает только один аргумент - ID образца");
        }
        try {
            this.sampleId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: ID должен быть числом");
        }
    }

    @Override
    public String getHelp() {
        return "meas_add <sample_id> - добавить измерение к образцу";
    }

    @Override
    public void execute(String[] args) {
    }

    @Override
    public void startAdditionalInput(InputStream inputStream) {

        long currentSampleId = this.sampleId;
        this.sampleId = 0;

        Scanner scanner = new Scanner(inputStream);

        try {
            Sample sample;
            try {
                sample = sampleService.getById(currentSampleId);
            } catch (NoSuchElementException e) {
                System.out.println("Ошибка: образец с id = " + currentSampleId + " не найден");
                return;
            }
            finally {
                this.sampleId = 0;
            }

            if (sample.getStatus() == SampleStatus.ARCHIVED) {
                System.out.println("Ошибка: нельзя добавлять измерения к заархивированному образцу");
                return;
            }

            MeasurementParam param = promptForParameter(scanner);
            double value = promptForValue(scanner);
            String unit = promptForNonEmpty(scanner, "Единицы");
            String method = promptForNonEmpty(scanner, "Метод");

            Measurement measurement = new Measurement(
                    nextMeasurementId++,
                    currentSampleId,
                    param,
                    value,
                    unit,
                    method,
                    Instant.now(),
                    sample.getOwnerUsername(),
                    Instant.now(),
                    Instant.now()
            );

            MeasurmentValidation.validate(measurement);
            allMeasurements.add(measurement);

            System.out.println("OK, измерение добавлено. measurement_id = " + measurement.getId());

        } catch (NoSuchElementException e) {
            System.out.println("Ошибка: ввод прерван");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private MeasurementParam promptForParameter(Scanner scanner) {
        while (true) {
            System.out.println("Параметр (PH/CONDUCTIVITY/TURBIDITY/NITRATE):");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return MeasurementParam.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: неизвестный параметр. Допустимые значения: PH, CONDUCTIVITY, TURBIDITY, NITRATE");
            }
        }
    }

    private double promptForValue(Scanner scanner) {
        while (true) {
            System.out.println("Значение:");
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value <= 0) {
                    System.out.println("Ошибка: значение должно быть больше 0");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: значение должно быть числом");
            }
        }
    }

    private String promptForNonEmpty(Scanner scanner, String fieldName) {
        while (true) {
            System.out.println(fieldName + ":");
            String input = scanner.nextLine().trim();
            if (input == null || input.isBlank()) {
                System.out.println("Ошибка: " + fieldName.toLowerCase() + " не могут быть пустыми");
                continue;
            }
            return input;
        }
    }
}