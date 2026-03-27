package cli;

/*
 * meas_stats <id> <param> - привести статистику по выбранному параметру: count/min/max/avg
 */

import java.io.*;
import java.util.*;
import domain.*;
import service.SampleService;

public class MeasurementStatsCommand extends Command {
    private final SampleService sampleService;
    private final Set<Measurement> allMeasurements;

    public MeasurementStatsCommand(SampleService sampleService,
                                  Set<Measurement> allMeasurements) {
        this.sampleService = sampleService;
        this.allMeasurements = allMeasurements;
        this.requiredAdditionalInput = false;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("Используйте: meas_stats <id> <param>");
        }
        if (args.length > 2) {
            throw new IllegalArgumentException
                    ("Ошибка: meas_stats принимает только аргументы <id> <param>");
        }
    }

    @Override
    public String getHelp() {
        return "meas_stats <id> <param> - привести статистику по выбранному параметру: count/min/max/avg";
    }

    @Override
    public void execute(String[] args) {

        long sampleId = Long.parseLong(args[0]);
        MeasurementParam param = MeasurementParam.valueOf(args[1].toUpperCase());

        try {
            sampleService.getById(sampleId);
        } catch (NoSuchElementException e) {
            System.out.println("Ошибка: образец с id = " + sampleId + " не найден");
            return;
        }
        List<Double> values = new ArrayList<>();
        for (Measurement m : allMeasurements) {
            if (m.getSampleId() == sampleId && m.getParam() == param) {
                values.add(m.getValue());
            }
        }
        if (values.isEmpty()) {
            System.out.println("Ошибка: нет измерений " + param + " для sample = " + sampleId);
            return;
        }

        double min = values.get(0);
        double max = values.get(0);
        double sum = 0;

        for (double v : values) {
            if (v < min) min = v;
            if (v > max) max = v;
            sum += v;
        }
        double avg = sum / values.size();

        System.out.println("count: " + values.size());
        System.out.printf("min: %.2f%n", min); // %.2f - спецификатор для числа с плавающей точкой
        System.out.printf("max: %.2f%n", max);
        System.out.printf("avg: %.2f%n", avg);
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {
        throw new UnsupportedOperationException("meas_stats не поддерживает дополнительный ввод");
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(getHelp(), command.getHelp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHelp());
    }
}