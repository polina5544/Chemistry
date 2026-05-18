package cli;

/*
 * meas_list <sample_id> [--param] | [--last N] - показать измерения образца
 */

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import domain.*;
import service.SampleService;

public class MeasurementListCommand implements Command {
    private final SampleService sampleService;
    private final Set<Measurement> allMeasurements;
    private static final Set<String> VALID_PARAMS = new HashSet<>(Arrays.asList( //быстрый способ превратить перечисление элементов в список. в HashSet нам нужны только ключи (уникальные элементы). Чтобы не тратить память на создание разных объектов для значений, Java использует одну-единственную статическую константу-заглушку.
            "PH", "CONDUCTIVITY", "TURBIDITY", "NITRATE"
    ));
    public MeasurementListCommand(SampleService sampleService,
                                  Set<Measurement> allMeasurements) {
        this.sampleService = sampleService;
        this.allMeasurements = allMeasurements;
    }

    @Override
    public boolean isRequiredAdditionalInput() {
        return true;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Используйте: meas_list <id> [--param] | [--last N]");
        }

        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: id образца должен быть числом");
        }

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--param")) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Ошибка: после --param укажите нужный параметр");
                }
                String param = args[i + 1].toUpperCase();
                if (!VALID_PARAMS.contains(param)) {
                    throw new IllegalArgumentException("Ошибка: неизвестный параметр '" + args[i + 1] + "'");
                }
                i++;
            }
            else if (arg.equals("--last")) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Ошибка: после --last нужно указать число");
                }
                try {
                    int n = Integer.parseInt(args[i + 1]);
                    if (n <= 0) {
                        throw new IllegalArgumentException("Ошибка: число после --last должно быть положительным");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Ошибка: после --last должно быть число");
                }
                i++;
            }
            else if (arg.startsWith("--")) {
                throw new IllegalArgumentException("Ошибка: неизвестный ввод '" + arg + "'");
            }
        }
    }

    @Override
    public String getHelp() {
        return "meas_list <sample_id> [--param] | [--last N] - показать измерения образца";
    }

    @Override
    public void execute(String[] args) {
        long sampleId = Long.parseLong(args[0]);

        try {
            sampleService.getById(sampleId);
        } catch (NoSuchElementException e) {
            System.out.println("Ошибка: образец с id = " + sampleId + " не найден");
            return;
        }

        // Парсим опции
        MeasurementParam paramFilter = null;
        Integer lastCount = null;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--param")) {
                paramFilter = MeasurementParam.valueOf(args[i + 1].toUpperCase());
                i++;
            } else if (args[i].equals("--last")) {
                lastCount = Integer.parseInt(args[i + 1]);
                i++;
            }
        }

        List<Measurement> filtered = new ArrayList<>();
        for (Measurement m : allMeasurements) {
            if (m.getSampleId() == sampleId) {
                if (paramFilter == null || m.getParam() == paramFilter) {
                    filtered.add(m);
                }
            }
        }

        Collections.sort(filtered, new Comparator<Measurement>() {
            @Override
            public int compare(Measurement m1, Measurement m2) {
                return m2.getMeasuredAt().compareTo(m1.getMeasuredAt());
            }
        });

        if (lastCount != null && lastCount < filtered.size()) {
            filtered = filtered.subList(0, lastCount);
        }
        if (filtered.isEmpty()) {
            System.out.println("Нет измерений для образца " + sampleId +
                    (paramFilter != null ? " с параметром " + paramFilter : ""));
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        System.out.println("Измерения для образца id = " + sampleId + " :");

        for (int i = 0; i < filtered.size(); i++) {
            Measurement m = filtered.get(i);
            System.out.println((i + 1) + ". " +
                    "id = " + m.getId() + ", " +
                    "param = " + m.getParam() + ", " +
                    "value = " + m.getValue() + " " + m.getUnit() + ", " +
                    "method = " + m.getMethod() + ", " +
                    "time = " + m.getMeasuredAt().atZone(java.time.ZoneId.systemDefault()).format(formatter));
        }
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {
        throw new UnsupportedOperationException("meas_list не поддерживает дополнительный ввод");
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o; //это приведение типа (casting) это обьект типа command
        return Objects.equals(getHelp(), command.getHelp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHelp());
    }
}
