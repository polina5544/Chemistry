import java.util.*;
import cli.*;
import domain.*;
import service.*;

public class Start {

    public static void main(String[] args) {
        // создали зависимости по принципу dependency injection - патерн

        SampleService sampleService = new SampleService();
        Set<Measurement> measurements = new HashSet<>();
        Set<Protocol> protocols = new HashSet<>();

        // в start передали зависимости
        Start app = new Start(sampleService, measurements, protocols);
        app.run();
    }

    private final Set<Command> commands = new HashSet<>();
    private final SampleService sampleService;
    private final Set<Measurement> allMeasurements;
    private final Set<Protocol> protocolStorage;
    private boolean running = true;
    private final Scanner scanner = new Scanner(System.in);

    // передали зaвисимости через конструктор
    public Start(SampleService sampleService,
                 Set<Measurement> allMeasurements,
                 Set<Protocol> protocolStorage) {
        this.sampleService = sampleService;
        this.allMeasurements = allMeasurements;
        this.protocolStorage = protocolStorage;
        registerCommands();
    }

    private void registerCommands() {

        commands.add(new HelpCommand(commands));
        commands.add(new ExitCommand(() -> running = false)); //лямбда-выражение - это реализация метода run

        commands.add(new ProtocolApplyCommand(
                sampleService,
                protocolStorage,
                allMeasurements
        ));

        commands.add(new ProtocolCreateCommand(protocolStorage));
        commands.add(new SampleAddCommand(sampleService));
        commands.add(new SampleArchiveCommand(sampleService));
        commands.add(new SampleListCommand(sampleService));
        commands.add(new SampleUpdateCommand(sampleService));
        commands.add(new SampleShowCommand(sampleService, allMeasurements));
        commands.add(new MeasurementAddCommand(sampleService, allMeasurements));
        commands.add(new MeasurementStatsCommand(sampleService, allMeasurements));
        commands.add(new MeasurementListCommand(sampleService, allMeasurements));
    }

    private Command findCommandByName(String name) {
        for (Command cmd : commands) {
            String cmdName = cmd.getHelp().split("\\s+")[0];
            if (cmdName.equalsIgnoreCase(name)) {
                return cmd;
            }
        }
        return null;
    }

    public void run() {

        System.out.println("Система управления лабораторными образцами и измерениями по протоколу");
        System.out.println("Чтобы просмотреть доступные команды, введите help");

        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+");
            String commandName = parts[0].toLowerCase();
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);

            Command command = findCommandByName(commandName);

            if (command == null) {
                System.out.println("Неизвестная команда: " + commandName);
                System.out.println("Введите help для списка команд");
                continue;
            }

            try {
                command.validateArgs(args);
                    command.startAdditionalInput(scanner);
                    command.execute(args);
            } catch (IllegalArgumentException e) { // проверка на пользовательский ввод
                System.out.println(e.getMessage());
            } catch (Exception e) { //системная ошибка на уровне всей программы
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
        scanner.close();
    }
}