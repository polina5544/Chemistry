package cli;

/*
 * save <path> - сохранить данные в xml
 */

//Команда собирает все данные из памяти в один контейнер
// StorageData и передаёт его в StorageService, который уже знает как
// записать это в xml. Сама команда не знает про xml она просто говорит
// сервису сохрани вот это

import domain.Measurement;
import domain.Protocol;
import domain.Sample;
import service.SampleService;
import storage.StorageData;
import storage.StorageService;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class SaveCommand implements Command {

    private final SampleService sampleService;
    private final Set<Measurement> allMeasurements;
    private final Set<Protocol> protocolStorage;
    private final StorageService storageService;

    public SaveCommand(SampleService sampleService,
                       Set<Measurement> allMeasurements,
                       Set<Protocol> protocolStorage,
                       StorageService storageService) {
        this.sampleService = sampleService;
        this.allMeasurements = allMeasurements;
        this.protocolStorage = protocolStorage;
        this.storageService = storageService;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Используйте: save <path>");
        }
    }

    @Override
    public String getHelp() {
        return "save <path> - сохранить данные в XML-файл";
    }

    @Override
    public void execute(String[] args) {
        String path = args[0];
        Set<Sample> samples = new HashSet<>(sampleService.getAll()); //конвертация типа так как StorageData ожидает Set а гет алл возвращает лист

        StorageData data = new StorageData(samples, allMeasurements, protocolStorage);
        storageService.save(path, data);
        //делегируем работу сервису
        // StorageService внутри вызывает xmlStorage.save,
        // который уже строит xml документ и пишет его в файл.
        System.out.println("OK, данные сохранены в " + path);
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {}

    @Override
    public boolean isRequiredAdditionalInput() { return false; }
}