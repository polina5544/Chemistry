package cli;

/*
 * prot_apply <protocol_id> <sample_id> - проверить, выполнен ли протокол для образца
 */

import java.io.*;
import java.util.*;
import domain.*;
import service.SampleService;

public class ProtocolApplyCommand extends Command {

    private final SampleService sampleService;
    private final Set<Protocol> protocolStorage;
    private final Set<Measurement> allMeasurements;

    public ProtocolApplyCommand(
            SampleService sampleService,
            Set<Protocol> protocolStorage,
            Set<Measurement> allMeasurements
    ) {
        this.sampleService = sampleService;
        this.protocolStorage = protocolStorage;
        this.allMeasurements = allMeasurements;
        this.requiredAdditionalInput = false;
    }

    @Override
    public void validateArgs(String[] args) {

        if (args == null || args.length != 2) {
            throw new IllegalArgumentException(
                    "Использование: prot_apply <protocol_id> <sample_id>");
        }

        try {
            Long.parseLong(args[0]);
            Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: id должен быть числом");
        }
    }

    @Override
    public String getHelp() {
        return "prot_apply <protocol_id> <sample_id> - проверить, выполнен ли протокол";
    }

    @Override
    public void execute(String[] args) {

        long protocolId = Long.parseLong(args[0]);
        long sampleId = Long.parseLong(args[1]);

        Protocol foundProtocol = null;
        for (Protocol p : protocolStorage) {
            if (p.getId() == protocolId) {
                foundProtocol = p;
                break;
            }
        }
        if (foundProtocol == null) {
            System.out.println("Ошибка: протокол с id " + protocolId + " не найден");
            return;
        }

        Sample sample;

        try {
            sample = sampleService.getById(sampleId);
        } catch (NoSuchElementException e) {
            System.out.println("Ошибка: образец не найден");
            return;
        }

        Set<MeasurementParam> requiredParams = foundProtocol.getRequiredParams(); // какие обязаны быть
        Set<MeasurementParam> sampleParams = new HashSet<>(); //какие параметры реально измерены у образца

        for (Measurement m : allMeasurements) {
            if (m.getSampleId() == sampleId) {
                sampleParams.add(m.getParam());
            }
        }

        Set<MeasurementParam> missing = new HashSet<>(requiredParams); //копия обязательных параметров
        missing.removeAll(sampleParams); // удаляяются из обязательных те параметры которве уже есть у образца

        if (missing.isEmpty()) {
            System.out.println("OK protocol is complete");
        } else {

            System.out.print("Missing params: ");

            boolean first = true;

            for (MeasurementParam p : missing) {

                if (!first) {
                    System.out.print(", ");
                }
                System.out.print(p);
                first = false;
            }

            System.out.println();
        }
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {
        throw new UnsupportedOperationException(
                "prot_apply не поддерживает дополнительный ввод");
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