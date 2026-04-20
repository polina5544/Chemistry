package cli;

/*
 * meas_stats <id> <param> - привести статистику по выбранному параметру: count/min/max/avg
 */

import java.io.*;
import java.util.*;
import domain.*;
import service.SampleService;

public class MeasurementStatsCommand implements Command {
    private final SampleService sampleService;
    private final Set<Measurement> allMeasurements;

    public MeasurementStatsCommand(SampleService sampleService,
                                  Set<Measurement> allMeasurements) {
        this.sampleService = sampleService;
        this.allMeasurements = allMeasurements;
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
    public void startAdditionalInput(Scanner scanner) {

    }

    @Override
    public boolean isRequiredAdditionalInput() {
        return false;
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
        List<Double> values = allMeasurements.stream()//превращение в поток те обработка элементов
                .filter(m -> m.getSampleId() == sampleId && m.getParam() == param) //для каждого m проверяем принадлежность условий
                .map(Measurement::getValue) // получение значения с помощью map превращаем одно в другое а именно в double
                .toList(); //собрала элементы в лист

        if (values.isEmpty()) {
            System.out.println("Ошибка: нет измерений " + param + " для sample = " + sampleId);
            return;
        }

        double min = values.stream().min(Double::compare).get();
        double max = values.stream().max(Double::compare).get();
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        double avg = sum / values.size();

        System.out.println("count: " + values.size());
        System.out.printf("min: %.2f%n", min); // %.2f - спецификатор для числа с плавающей точкой
        System.out.printf("max: %.2f%n", max);
        System.out.printf("avg: %.2f%n", avg);
    }
}