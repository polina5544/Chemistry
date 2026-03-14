package cli;

/**
 * exit: выйти без сохранения
 */

import java.io.*;
import java.util.Objects;

public class ExitCommand extends Command {
    private final Runnable onExit;

    public ExitCommand(Runnable onExit) {
        this.onExit = onExit;
        this.requiredAdditionalInput = false;
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
    public void execute(String[] args) {
        validateArgs(args);
        System.out.println("Завершение работы...");
        onExit.run();
    }

    @Override
    public void startAdditionalInput(InputStream inputStream) {
        throw new UnsupportedOperationException
                ("exit не поддерживает дополнительный ввод");
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
