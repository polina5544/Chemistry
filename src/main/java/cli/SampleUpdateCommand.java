package cli;

/*
 * sample_update <id> field=value ... - изменить поля образца (name, type, location, status)
 */

import java.io.*;
import java.time.Instant;
import java.util.*;
import domain.Sample;
import domain.SampleStatus;
import service.SampleService;
import validation.SampleValidation;

public class SampleUpdateCommand implements Command {
    private final SampleService sampleService;
    private static final Set<String> ALLOWED_FIELDS = new HashSet<>(Arrays.asList(
            "name", "type", "location", "status"
    ));

    public SampleUpdateCommand(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException
                    ("Используйте такой формат ввода: sample_update <id> поле=значение ... " +
                            "Введите изменяемое значение в кавычках");
        }
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: ID должен быть числом");
        }
    }

    @Override
    public String getHelp() {
        return "sample_update <id> field=value - изменить поля образца (name, type, location, status)";
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {

    }

    @Override
    public void execute(String[] args) {
        
        long id = Long.parseLong(args[0]);

        try {
            Sample sample = sampleService.getById(id);

            Map<String, String> updates = parseArgumentsWithQuotes(args); //метод разбирает аргументы вида: name="River sample" создаёт Map - name : River sample

            Set<String> seenFields = new HashSet<>();
            String oldName = sample.getName();
            String oldType = sample.getType();
            String oldLocation = sample.getLocation();
            SampleStatus oldStatus = sample.getStatus();

            for (String field : updates.keySet()) { // проходится по каждой веденной пользователем паре и возвращает множество (Set<K>), содержащее все уникальные ключи, присутствующие в карте
                if (!ALLOWED_FIELDS.contains(field)) { // contains - проверяет есть ли элемент(ключ) в списке/строке
                    System.out.println("Ошибка: нельзя менять поле '" + field + "'");
                    return;
                }
                if (seenFields.contains(field)) {
                    System.out.println("Ошибка: поле '" + field + "' указано несколько раз");
                    return;
                }
                seenFields.add(field);
            }

            boolean changed = false;

            // уже обход обновленных
            for (Map.Entry<String, String> entry : updates.entrySet()) { // Проходится по кажой паре ключ значение - Entry - одна пара «ключ-значение» внутри словаря Map. entrySet() — это метод у любой Map, который вытряхивает из неё все пары «ключ-значение» и складывает их в один набор Set
                String field = entry.getKey(); // здесь Map - временная структура. аргументы разбираются в Map, где ключ — это имя поля,а значение — введённое пользователем значение. Далее через entrySet происходит перебор всех пар ключ-значение.
                String value = entry.getValue();

                if (value == null || value.trim().isEmpty()) {
                    System.out.println("Ошибка: " + field + " не может быть пустым");
                    return;
                }

                switch (field) {
                    case "name":
                        sample.setName(value);
                        changed = true;
                        break;
                    case "type":
                        sample.setType(value);
                        changed = true;
                        break;
                    case "location":
                        sample.setLocation(value);
                        changed = true;
                        break;
                    case "status":
                        String statusUpper = value.toUpperCase();
                        if (!statusUpper.equals("ACTIVE") && !statusUpper.equals("ARCHIVED")) {
                            System.out.println("Ошибка: статус только ACTIVE или ARCHIVED");
                            return;
                        }
                        SampleStatus newStatus = SampleStatus.valueOf(statusUpper);
                        if (oldStatus == SampleStatus.ARCHIVED && newStatus != SampleStatus.ARCHIVED) {
                            System.out.println("Ошибка: если образец заархивирован, то вносить изменения уже нельзя");
                            return;
                        }
                        sample.setStatus(newStatus);
                        changed = true;
                        break;
                }
            }

            if (!changed) {
                System.out.println("Ничего не изменено");
                return;
            }

            sample.setUpdatedAt(Instant.now());

            try {
                SampleValidation.validate(sample);
            } catch (IllegalArgumentException e) {
                sample.setName(oldName);
                sample.setType(oldType);
                sample.setLocation(oldLocation);
                sample.setStatus(oldStatus);
                System.out.println("Ошибка: " + e.getMessage());
                return;
            }

            System.out.println("OK, образец с id = " + id + " изменён.");

        } catch (NoSuchElementException e) {
            System.out.println("Ошибка: образец с id = " + id + " не найден");
        }
    }

    private Map<String, String> parseArgumentsWithQuotes(String[] args) {
        Map<String, String> updates = new HashMap<>();

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];

            int equalsIndex = arg.indexOf('='); //ищет позицию = и проверяет если оно есть

            if (equalsIndex == -1) { // возвращает -1 если элекмент не найден
                throw new IllegalArgumentException("Ошибка: аргумент '" + arg + "' должен быть в формате поле=значение");
            }

            String field = arg.substring(0, equalsIndex).toLowerCase(); //берет все до = и возвращает имя поля, извлекает часть строки
            String valuePart = arg.substring(equalsIndex + 1);

            if (valuePart.startsWith("\"")) { //проверяет, начинается ли строка с указанного префикса, возвращая true или false. Он чувствителен к регистру
                StringBuilder fullValue = new StringBuilder(valuePart);

                if (!valuePart.endsWith("\"")) {
                    i++;

                    while (i < args.length) {
                        fullValue.append(" ").append(args[i]); //добавить к старой строке пробел и еще один аргумент
                        if (args[i].endsWith("\"")) {
                            break;
                        }
                        i++;
                    }
                }

                String quotedValue = fullValue.toString();
                int firstQuote = quotedValue.indexOf('"'); //ищет индексы первой и последней кавычки
                int lastQuote = quotedValue.lastIndexOf('"');
                if (firstQuote != -1 && lastQuote != -1 && firstQuote < lastQuote) {
                    valuePart = quotedValue.substring(firstQuote + 1, lastQuote); //substring берёт часть строки между индексами
                } else {
                    valuePart = quotedValue;
                }
            }

            updates.put(field, valuePart.trim());
        }
        return updates;
    }
}
