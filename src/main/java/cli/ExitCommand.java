package cli;

/*
 * exit: выйти без сохранения
 */

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class ExitCommand implements Command {
    private final Runnable onExit; //runnable - интегрфейс, контейнер для кода, который можно выполнить позже

    public ExitCommand(Runnable onExit) {
        this.onExit = onExit;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args != null && args.length > 0) {
            throw new IllegalArgumentException("exit не принимает аргументы");
        }
    }

    @Override
    public String getHelp() {
        return "exit - выйти без сохранения";
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {

    }

    @Override
    public boolean isRequiredAdditionalInput() {
        return false;
    }

    @Override
    public void execute(String[] args) {
        validateArgs(args);
        System.out.println("Завершение работы...");
        onExit.run();
    }
}
