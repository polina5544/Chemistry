package cli;

/*
 * load <path> - загрузить данные из xml
 * а если файл невалиден — данные в памяти НЕ меняются (атомарность)
 */

import domain.Measurement;
import domain.Protocol;
import service.SampleService;
import service.UserService;
import storage.StorageData;
import storage.StorageService;
import utilits.IDgenerator;
import java.util.Scanner;
import java.util.Set;

public class LoadCommand implements Command {

    private final SampleService sampleService;
    private final Set<Measurement> allMeasurements;
    private final Set<Protocol> protocolStorage;
    private final StorageService storageService;
    private final UserService userService;

    public LoadCommand(SampleService sampleService,
                       Set<Measurement> allMeasurements,
                       Set<Protocol> protocolStorage,
                       StorageService storageService,
                       UserService userService) {
        this.sampleService = sampleService;
        this.allMeasurements = allMeasurements;
        this.protocolStorage = protocolStorage;
        this.storageService = storageService;
        this.userService =  userService;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Используйте: load <path>");
        }
    }

    @Override
    public String getHelp() {
        return "load <path> - загрузить данные из XML-файла";
    }

    @Override
    public void execute(String[] args) {
        String path = args[0];

        // Загружаем а если файл невалиден исключение вылетит до изменения памяти
        StorageData loaded = storageService.load(path);

        userService.setUsers(loaded.users());

        // Только после успешной загрузки применяем данные в память
        sampleService.setSamples(loaded.samples());

        allMeasurements.clear();
        allMeasurements.addAll(loaded.measurements());

        protocolStorage.clear();
        protocolStorage.addAll(loaded.protocols());

        // Обновляем счётчик ай ди, чтобы не было коллизий
        IDgenerator.updateAll(loaded.samples(), loaded.measurements(), loaded.protocols());

        System.out.println("OK, загружено из " + path +
                ": " + loaded.samples().size() + " образцов, " +
                loaded.measurements().size() + " измерений, " +
                loaded.protocols().size() + " протоколов" +
                loaded.users().size() + " пользователей");
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {}

    @Override
    public boolean isRequiredAdditionalInput() { return false; }
}