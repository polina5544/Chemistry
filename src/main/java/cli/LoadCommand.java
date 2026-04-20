package cli;

/*
 * load <path> - загрузить данные из xml
 * а если файл невалиден — данные в памяти НЕ меняются (атомарность)
 */

import domain.Measurement;
import domain.Protocol;
import domain.Sample;
import service.SampleService;
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

    public LoadCommand(SampleService sampleService,
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

        // Только после успешной загрузки применяем данные в память
        sampleService.setSamples(loaded.getSamples());

        allMeasurements.clear();
        allMeasurements.addAll(loaded.getMeasurements());

        protocolStorage.clear();
        protocolStorage.addAll(loaded.getProtocols());

        // Обновляем счётчик ай ди, чтобы не было коллизий
        IDgenerator.updateAll(loaded.getSamples(), loaded.getMeasurements(), loaded.getProtocols());

        System.out.println("OK, загружено из " + path +
                ": " + loaded.getSamples().size() + " образцов, " +
                loaded.getMeasurements().size() + " измерений, " +
                loaded.getProtocols().size() + " протоколов");
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {}

    @Override
    public boolean isRequiredAdditionalInput() { return false; }
}