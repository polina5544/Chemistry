package cli;

/*
 * meas_add <id> - добавить измерение к образцу
 */


import java.time.Instant;
import java.util.*;
import domain.*;

import cli.Command;
import service.SampleService;
import validation.MeasurmentValidation;
import utilits.IDgenerator;

public class MeasurementAddCommand implements Command {

    private final SampleService sampleService;
    private final Set<Measurement> allMeasurements;
    private long sampleId;

    public MeasurementAddCommand(SampleService sampleService,
                                Set<Measurement> allMeasurements) { // тип - контракт метода
        this.sampleService = sampleService;
        this.allMeasurements = allMeasurements;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Ошибка: укажите id образца");
        }
        if (args.length > 1) {
            throw new IllegalArgumentException("Ошибка: meas_add принимает только id образца");
        }
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: id должен быть числом");
        }
    }

    @Override
    public String getHelp() {
        return "meas_add <id> - добавить измерение к образцу";
    }

    @Override
    public void execute(String[] args) {
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {

        long currentSampleId = this.sampleId;
        this.sampleId = 0;

        try {
            Sample sample;
            try {
                sample = sampleService.getById(currentSampleId);
            } catch (NoSuchElementException e) {
                System.out.println("Ошибка: образец с id = " + currentSampleId + " не найден");
                return;
            }

            if (sample.getStatus() == SampleStatus.ARCHIVED) {
                System.out.println("Ошибка: нельзя добавлять измерения к заархивированному образцу");
                return;
            }

            MeasurementParam param = promptForParameter(scanner);
            double value = promptForValue(scanner);
            String unit = promptForNonEmpty(scanner, "Единицы");
            String method = promptForNonEmpty(scanner, "Метод");
            long measurementId = IDgenerator.nextId();

            Measurement measurement = new Measurement(
                    measurementId,
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

            if (input.isBlank()) {
                System.out.println("Ошибка: параметр не может быть пустым");
                continue;
            }

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

            if (input.isBlank()) {
                System.out.println("Ошибка: значение не может быть пустым");
                continue;
            }

            try {
                double value = Double.parseDouble(input);

                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    System.out.println("Ошибка: значение должно быть обычным числом");
                    continue;
                }

                if (value <= 0) {
                    System.out.println("Ошибка: значение должно быть больше 0");
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число");
            }
        }
    }

    private String promptForNonEmpty(Scanner scanner, String fieldName) {
        while (true) {
            System.out.println(fieldName + ":");
            String input = scanner.nextLine();

            if (input.isBlank()) {
                System.out.println("Ошибка: " + fieldName.toLowerCase() + " не может быть пустым");
                continue;
            }

            return input.trim();
        }
    }
}