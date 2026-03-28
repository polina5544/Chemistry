package cli;

/*
 * prot_create - создать новый протокол
 */

import java.time.Instant;
import java.util.*;

import domain.*;
import utilits.IDgenerator;
import validation.ProtocolValidation;

public class ProtocolCreateCommand implements Command {

    private final Set<Protocol> protocolStorage;
    long nextProtocolId = IDgenerator.nextId();


    public ProtocolCreateCommand(Set<Protocol> protocolStorage) {
        this.protocolStorage = protocolStorage;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args != null && args.length > 0) {
            throw new IllegalArgumentException("prot_create не принимает аргументы");
        }
    }

    @Override
    public String getHelp() {
        return "prot_create - создать новый протокол";
    }

    @Override
    public void execute(String[] args) {
        throw new UnsupportedOperationException("Используйте интерактивный режим");
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {

        try {

            System.out.println("Название протокола:");
            String name = scanner.nextLine().trim();

            if (name.isEmpty()) {
                System.out.println("Ошибка: имя протокола не может быть пустым");
                return;
            }

            System.out.println("Обязательные параметры (через запятую):");
            String paramsLine = scanner.nextLine().trim();

            if (paramsLine.isEmpty()) {
                System.out.println("Ошибка: нужно указать хотя бы один параметр");
                return;
            }

            String[] parts = paramsLine.split(",");
            Set<MeasurementParam> params = new HashSet<>();

            for (String part : parts) {
                try {
                    MeasurementParam p =
                            MeasurementParam.valueOf(part.trim().toUpperCase());
                    params.add(p);
                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка: неизвестный параметр " + part.trim());
                    return;
                }
            }

            Protocol protocol = new Protocol(
                    nextProtocolId,
                    name,
                    params,
                    "system",
                    Instant.now(),
                    Instant.now()
            );

            ProtocolValidation.validate(protocol);
            protocolStorage.add(protocol);

            System.out.println("OK protocol_id = " + protocol.getId());

        } catch (NoSuchElementException e) { // ошибка источника ввода а именно данные закончились например чтпение из файла
            System.out.println("Ошибка: ввод прерван");
        }
    }

}