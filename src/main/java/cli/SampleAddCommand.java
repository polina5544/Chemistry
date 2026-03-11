package cli;

/**
 * sample_add - создание нового образца
 * происходит крайне интерактивно
 */

import java.io.*;
import java.time.Instant;
import java.util.*;
import domain.Sample;
import domain.SampleStatus;
import service.SampleService;
import validation.SampleValidation;

public class SampleAddCommand extends Command {
    private final SampleService sampleService;

    public SampleAddCommand(SampleService sampleService) {
        this.sampleService = sampleService;
        this.requiredAdditionalInput = true;
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
    public void startAdditionalInput(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream);

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
                    fieldName.equals("name") ? input : (validatedName != null ? validatedName : "temp_name"),
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
                    isCurrentFieldError = true;
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
                    throw new RuntimeException(errorMessage);
                }
            }
        }
    }

    private Sample createTempSample(String name, String type, String location, String owner) {
        return new Sample(
                0L,
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