// Пока что главный класс для проверки работы команд. Позже переедет в Main.

import java.util.*;
import cli.*;
import domain.*;
import service.*;
import utilits.*;
import validation.*;

public class Start {
    public static void main(String[] args) {
        new Start().run();
    }

    private final Set<Command> commands = new HashSet<>();
    private final SampleService sampleService;
    private final List<Measurement> allMeasurements = new ArrayList<>();
    private boolean running = true;

    public Start() {
        this.sampleService = new SampleService();
        registerCommands();
    }

    private void registerCommands() {

        commands.add(new HelpCommand(commands));
        commands.add(new ExitCommand(new Runnable() {
            @Override
            public void run() {
                running = false;
            }
        }));

        commands.add(new SampleAddCommand(sampleService));
        commands.add(new SampleArchiveCommand(sampleService));
        commands.add(new SampleListCommand(sampleService));
        commands.add(new SampleUpdateCommand(sampleService));

        commands.add(new MeasurementAddCommand(sampleService, allMeasurements));
    }

    private Command findCommandByName(String name) {
        for (Command cmd : commands) {
            String helpLine = cmd.getHelp();
            String cmdName = helpLine.split("\\s+")[0];
            if (cmdName.equalsIgnoreCase(name)) {
                return cmd;
            }
        }
        return null;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Система управления лабораторными образцами и измерениями по протоколу");
        System.out.println("Чтобы просмотреть доступные команды, введите help");

        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+");
            String commandName = parts[0].toLowerCase();
            String[] args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, parts.length - 1);

            Command command = findCommandByName(commandName);
            if (command != null) {
                try {
                    command.validateArgs(args);
                    if (command.isRequiredAdditionalInput()) {
                        command.startAdditionalInput(System.in);
                    } else {
                        command.execute(args);
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            } else {
                System.out.println("Неизвестная команда: " + commandName);
                System.out.println("Чтобы просмотреть доступные команды, введите help");
            }
        }
        scanner.close();
    }
}