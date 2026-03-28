package cli;

/*
 * sample_add - создание нового образца
 * происходит краааайне интерактивно (жеееееесть варя жесть, я чуть не умерла)
 */


import java.time.Instant;
import java.util.*;
import domain.Sample;
import domain.SampleStatus;
import service.SampleService;
import validation.SampleValidation;

public class SampleAddCommand implements Command {
    private final SampleService sampleService;

    public SampleAddCommand(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args != null && args.length > 0) {
            throw new IllegalArgumentException("sample_add не принимает аргументы");
        }
    }

    @Override
    public String getHelp() {
        return "sample_add - создать новый образец";
    }

    @Override
    public void execute(String[] args) {}

    @Override
    public void startAdditionalInput(Scanner scanner) {

        String name = promptForField(scanner, "name", null, null, null, null);
        String type = promptForField(scanner, "type", name, null, null, null);
        String location = promptForField(scanner, "location", name, type, null, null);
        String owner = promptForField(scanner, "owner", name, type, location, null);

        try {
            Sample sample = sampleService.add(name, type, location, owner);
            System.out.println("OK, образец добавлен. sample_id = " + sample.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка при создании: " + e.getMessage());
        }
    }

    private String promptForField(Scanner scanner, String fieldName,
                                  String validatedName, String validatedType,
                                  String validatedLocation, String validatedOwner) {
        while (true) {
            System.out.println(fieldName + ":");
            String input = scanner.nextLine().trim();

            Sample tempSample = createTempSample(
                    fieldName.equals("name") ? input : (validatedName), // 4х этапная валидация. при первом вводе name в остальные подставляется заглушка тк validateSample проверяет 4 аргумента сразу. Далее после name оно уходит на второй этап type и следующая валидация проходит с удже существующим name
                    fieldName.equals("type") ? input : (validatedType != null ? validatedType : "temp_type"),
                    fieldName.equals("location") ? input : (validatedLocation != null ? validatedLocation : "temp_location"),
                    fieldName.equals("owner") ? input : (validatedOwner != null ? validatedOwner : "temp_owner")
            );

            try {
                SampleValidation.validate(tempSample);
                return input;
            } catch (IllegalArgumentException e) {
                String errorMessage = e.getMessage();
                boolean isCurrentFieldError = false;
                if (fieldName.equals("name") && errorMessage.contains("name")) {
                    isCurrentFieldError = true; // проверить относится ли ошибка к текущему полю и если относится то попросить повторить ввод после этого значения
                } else if (fieldName.equals("type") && errorMessage.contains("type")) {
                    isCurrentFieldError = true;
                } else if (fieldName.equals("location") && errorMessage.contains("location")) {
                    isCurrentFieldError = true;
                } else if (fieldName.equals("owner") && (errorMessage.contains("owner") || errorMessage.contains("создатель"))) {
                    isCurrentFieldError = true;
                }
                if (isCurrentFieldError) {
                    System.out.println(errorMessage);
                    System.out.println("Повторите ввод:");
                } else {
                    throw new RuntimeException(errorMessage); //это тип ошибки, которую компилятор не заставляет обязательно обрабатывать в блоке try-catch. Она просто «роняет» текущий поток выполнения, если её никто не перехватит.
                }
            }
        }
    }

    private Sample createTempSample(String name, String type, String location, String owner) {
        return new Sample(
                0L, //заглушка  используется потому что это временный объект для валидации, а реальные id генерируются в сервисе при создании настоящего Sample
                name,
                type,
                location,
                SampleStatus.ACTIVE,
                owner,
                Instant.now(),
                Instant.now()
        );
    }
}