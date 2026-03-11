package cli;

/**
 * sample_list [--status ACTIVE|ARCHIVED] [--mine] - вывести список образцов
 */

import java.io.*;
import domain.Sample;
import domain.SampleStatus;
import service.SampleService;

public class SampleListCommand extends Command {
    private final SampleService sampleService;

    public SampleListCommand(SampleService sampleService) {
        this.sampleService = sampleService;
        this.requiredAdditionalInput = false;
    }

    @Override
    public void validateArgs(String[] args) {}

    @Override
    public String getHelp() {
        return "sample_list [--status ACTIVE|ARCHIVED] [--mine] - вывести список образцов";
    }

    @Override
    public void execute(String[] args) {
        SampleStatus statusFilter = null;
        boolean mine = false;

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if ("--status".equals(args[i])) {
                    if (i + 1 >= args.length) {
                        System.out.println("Нужно указать статус:");
                        return;
                    }
                    try {
                        statusFilter = SampleStatus.valueOf(args[i + 1].toUpperCase());
                        i++;
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка: в качестве статуса используйте ACTIVE или ARCHIVED");
                        return;
                    }
                } else if ("--mine".equals(args[i])) {
                    mine = true;
                } else {
                    System.out.println("Ошибка: " + args[i]);
                    return;
                }
            }
        }

        System.out.println("ID Name Type Location Status");

        boolean found = false;
        for (Sample s : sampleService.getAll()) {
            if (statusFilter != null && s.getStatus() != statusFilter) continue;
            if (mine) {
            }

            System.out.printf("%-5d %-20s %-10s %-13s %-8s%n",
                    s.getId(),
                    s.getName().length() > 20 ? s.getName().substring(0, 17) + "..." : s.getName(),
                    s.getType().length() > 10 ? s.getType().substring(0, 7) + "..." : s.getType(),
                    s.getLocation().length() > 13 ? s.getLocation().substring(0, 10) + "..." : s.getLocation(),
                    s.getStatus()
            );
            found = true;
        }

        if (!found) {
            System.out.println("В системе нет образцов, соответствующих критериям");
        }
    }

    @Override
    public void startAdditionalInput(InputStream inputStream) {
        throw new UnsupportedOperationException("sample_list не поддерживает дополнительный ввод");
    }
}