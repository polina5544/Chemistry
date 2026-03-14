package cli;

/**
 * sample_update <id> field=value ... - изменить поля образца (name, type, location, status)
 */

import java.io.*;
import java.time.Instant;
import java.util.*;
import domain.Sample;
import domain.SampleStatus;
import service.SampleService;
import validation.SampleValidation;

public class SampleUpdateCommand extends Command {
    private final SampleService sampleService;
    private static final Set<String> ALLOWED_FIELDS = new HashSet<>(Arrays.asList(
            "name", "type", "location", "status"
    ));

    public SampleUpdateCommand(SampleService sampleService) {
        this.sampleService = sampleService;
        this.requiredAdditionalInput = false;
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
    public void execute(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("Используйте такой формат ввода: sample_update <id> поле=значение ... " +
                    "Введите изменяемое значение в кавычках");
            return;
        }

        long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
            return;
        }

        try {
            Sample sample = sampleService.getById(id);

            String oldName = sample.getName();
            String oldType = sample.getType();
            String oldLocation = sample.getLocation();
            SampleStatus oldStatus = sample.getStatus();

            Map<String, String> updates = parseArgumentsWithQuotes(args); //метод разбирает аргументы вида: name="River sample" создаёт Map: name -> River sample


            Set<String> seenFields = new HashSet<>();
            for (String field : updates.keySet()) { //возвращает множество (Set<K>), содержащее все уникальные ключи, присутствующие в карте
                if (!ALLOWED_FIELDS.contains(field)) { // contains - проверяет есть ли элемент в списке/строке
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

            for (Map.Entry<String, String> entry : updates.entrySet()) { // Entry - одна пара «ключ-значение» внутри словаря Map. entrySet() — это метод у любой Map, который вытряхивает из неё все пары «ключ-значение» и складывает их в один набор Set
                String field = entry.getKey();
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

            sampleService.update(id, sample.getName(), sample.getType(), sample.getLocation());
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

            if (equalsIndex == -1) {
                throw new IllegalArgumentException("Ошибка: аргумент '" + arg + "' должен быть в формате поле=значение");
            }

            String field = arg.substring(0, equalsIndex).toLowerCase(); //берет все до = и возвращает имя поля
            String valuePart = arg.substring(equalsIndex + 1);

            if (valuePart.startsWith("\"")) {
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

    @Override
    public void startAdditionalInput(InputStream inputStream) {
        throw new UnsupportedOperationException("sample_update не поддерживает дополнительный ввод");
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
