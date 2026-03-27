package cli;

/*
 * sample_archive <id> - перевести образец в ARCHIVED
 * если образец заархивирован, то добавлять новые измерения уже нельзя
 */

import java.io.*;
import java.util.*;
import java.time.Instant;
import domain.Sample;
import domain.SampleStatus;
import service.SampleService;

public class SampleArchiveCommand extends Command {
    private final SampleService sampleService;

    public SampleArchiveCommand(SampleService sampleService) {
        this.sampleService = sampleService;
        this.requiredAdditionalInput = false;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Введите id");
        }
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id должен быть числом");
        }
    }

    @Override
    public String getHelp() {
        return "sample_archive <id> - перевести образец в ARCHIVED";
    }

    @Override
    public void execute(String[] args) {
        long id = Long.parseLong(args[0]);

        try {
            Sample sample = sampleService.getById(id);

            if (sample.getStatus() == SampleStatus.ARCHIVED) {
                System.out.println("Статус образца уже ARCHIVED");
                return;
            }

            sample.setStatus(SampleStatus.ARCHIVED);
            sample.setUpdatedAt(Instant.now());

            System.out.println("OK sample " + id + " заархивирован");

        } catch (NoSuchElementException e) {
            System.out.println("Ошибка: образец с id " + id + " не найден");

        }
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {
        throw new UnsupportedOperationException("sample_archive не поддерживает дополнительный ввод");
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
