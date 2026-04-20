import java.util.*;
import cli.*;
import domain.*;
import service.*;
import storage.StorageService;

public class Start {

    public static void main(String[] args) {
        SampleService sampleService = new SampleService();
        Set<Measurement> measurements = new HashSet<>();
        Set<Protocol> protocols = new HashSet<>();
        StorageService storageService = new StorageService();

        // Если путь к файлу передан аргументом командной строки — загружаем сразу
        String filePath = args.length > 0 ? args[0] : null;

        Start app = new Start(sampleService, measurements, protocols, storageService, filePath);
        app.run();
    }

    private final Set<Command> commands = new HashSet<>();
    private final SampleService sampleService;
    private final Set<Measurement> allMeasurements;
    private final Set<Protocol> protocolStorage;
    private final StorageService storageService;
    private boolean running = true;
    private final Scanner scanner = new Scanner(System.in);

    public Start(SampleService sampleService,
                 Set<Measurement> allMeasurements,
                 Set<Protocol> protocolStorage,
                 StorageService storageService,
                 String initialFilePath) {
        this.sampleService = sampleService;
        this.allMeasurements = allMeasurements;
        this.protocolStorage = protocolStorage;
        this.storageService = storageService;
        registerCommands();

        // Авто-загрузка при старте
        if (initialFilePath != null) {
            try {
                LoadCommand loader = new LoadCommand(sampleService, allMeasurements, protocolStorage, storageService);
                loader.execute(new String[]{initialFilePath});
            } catch (Exception e) {
                System.out.println("Не удалось загрузить файл при старте: " + e.getMessage());
            }
        }
    }

    private void registerCommands() {
        commands.add(new HelpCommand(commands));
        commands.add(new ExitCommand(() -> running = false));

        commands.add(new SaveCommand(sampleService, allMeasurements, protocolStorage, storageService));
        commands.add(new LoadCommand(sampleService, allMeasurements, protocolStorage, storageService));

        commands.add(new ProtocolApplyCommand(sampleService, protocolStorage, allMeasurements));
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
            String[] cmdArgs = Arrays.copyOfRange(parts, 1, parts.length);

            Command command = findCommandByName(commandName);

            if (command == null) {
                System.out.println("Неизвестная команда: " + commandName);
                System.out.println("Введите help для списка команд");
                continue;
            }

            try {
                command.validateArgs(cmdArgs);
                if (command.isRequiredAdditionalInput()) {
                    command.startAdditionalInput(scanner);
                } else {
                    command.execute(cmdArgs);
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
        scanner.close();
    }
}