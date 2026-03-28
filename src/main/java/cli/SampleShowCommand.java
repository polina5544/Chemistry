package cli;

/*
 * sample_show <id> - показать карточку образца и статистику по измерениям
 */

import java.io.*;
import java.util.*;
import domain.*;
import service.SampleService;

public class SampleShowCommand implements Command {
    private final SampleService sampleService;
    private final Set<Measurement> allMeasurements;

    public SampleShowCommand(SampleService sampleService, Set<Measurement> allMeasurements) {
        this.sampleService = sampleService;
        this.allMeasurements = allMeasurements;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Ошибка: укажите id образца");
        }
        if (args.length > 1) {
            throw new IllegalArgumentException("Ошибка: sample_show принимает только id образца");
        }
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: id должен быть числом");
        }
    }

    @Override
    public String getHelp() {
        return "sample_show <id> - показать карточку образца и статистику по измерениям";
    }

    @Override
    public void execute(String[] args) {

        long id = Long.parseLong(args[0]);

        try {
            Sample sample = sampleService.getById(id);

            List<Measurement> sampleMeasurements = new ArrayList<>(); // все параметры с дублированием
            Set<MeasurementParam> uniqueParams = new HashSet<>(); // параметры могут дублтроваться. делам эту колекция во избежание одинаковых параметров кстати тип енум!!

            for (Measurement m : allMeasurements) { // проходит по всем объектам из коллекции и ищет нужный ID
                if (m.getSampleId() == id) {
                    sampleMeasurements.add(m); // сюда все измерения
                    uniqueParams.add(m.getParam()); // сюда только параметр измерения присем если он уже есть то больше не добавится тк set
                }
            }

            System.out.println("Sample #" + sample.getId());
            System.out.println("name: " + sample.getName());
            System.out.println("type: " + sample.getType());
            System.out.println("location: " + sample.getLocation());
            System.out.println("status: " + sample.getStatus());
            System.out.println("owner: " + sample.getOwnerUsername());
            System.out.println("measurements: " + sampleMeasurements.size());

            if (!uniqueParams.isEmpty()) {
                List<String> paramNames = new ArrayList<>();
                for (MeasurementParam p : uniqueParams) { // получаем имя из enum, p.name - метод enum который возвращает название как строку
                    paramNames.add(p.name());
                }
                Collections.sort(paramNames);
                System.out.println("params: " + String.join(", ", paramNames)); // join - соединяет элементы списка в одну строку
            } else {
                System.out.println("params: нет измерений");
            }

        } catch (NoSuchElementException e) {
            System.out.println("Ошибка: образец с id = " + id + " не найден");
        }
    }

    public void startAdditionalInput(Scanner scanner) {
        throw new UnsupportedOperationException("sample_show не поддерживает дополнительный ввод");
    }
}