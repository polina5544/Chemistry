package cli;

/*
 * sample_list [--status ACTIVE|ARCHIVED] [--mine] - вывести список образцов
 */

import java.util.Objects;
import java.util.Scanner;

import domain.Sample;
import domain.SampleStatus;
import service.SampleService;

public class SampleListCommand implements Command {
    private final SampleService sampleService;

    public SampleListCommand(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @Override
    public void validateArgs(String[] args) {

        if (args.length < 1) {
            throw new IllegalArgumentException("Нужно указать id");
        }

        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id должен быть числом");
        }

    }

    @Override
    public boolean isRequiredAdditionalInput() {
        return false;
    }

    @Override
    public String getHelp() {
        return "sample_list [--status ACTIVE|ARCHIVED] [--mine] - вывести список образцов";
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {

    }

    @Override
    public void execute(String[] args) {
        SampleStatus statusFilter = null;

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if ("--status".equals(args[i])) {
                    if (i + 1 >= args.length) {
                        System.out.println("Нужно указать статус:");
                        return;
                    }
                    try {
                        statusFilter = SampleStatus.valueOf(args[i + 1].toUpperCase()); // конвертируем сдедующий полученный аргумент(статус) в обьект енума
                        i++;
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка: в качестве статуса используйте ACTIVE или ARCHIVED");
                        return;
                    }

                } else {
                    System.out.println("Ошибка: " + args[i]);
                    return;
                }
            }
        }

        System.out.println("ID Name Type Location Status");

        boolean found = false; // флаг что б понять нашли ли мы подходящий образец
        for (Sample s : sampleService.getAll()) {
            if (statusFilter != null && s.getStatus() != statusFilter) continue; // если фильтр задан и полученный образец не соответствует ему то пропустить


            System.out.printf("%-5d %-20s %-10s %-13s %-8s%n",
                    s.getId(),
                    s.getName().length() > 20 ? s.getName().substring(0, 17) + "..." : s.getName(), //обрезаем строку если она длинная, но днлаем троеточние что б было ровно 20
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
}