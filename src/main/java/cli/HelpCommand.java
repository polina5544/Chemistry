package cli;

/**
 * help: вывести справку по доступным командам
 */

import java.io.*;
import java.util.Objects;
import java.util.Set;

public class HelpCommand extends Command {
    private final Set<Command> commands;

    public HelpCommand(Set<Command> commands) {
        this.commands = commands;
        this.requiredAdditionalInput = false;
    }

    @Override
    public void validateArgs(String[] args) {
        if (args != null && args.length > 0) {
            throw new IllegalArgumentException("help не принимает аргументы");
        }
    }

    @Override
    public String getHelp() {
        return "help - вывести справку по доступным командам";
    }

    @Override
    public void execute(String[] args) {
        validateArgs(args);
        System.out.println("Доступные команды:");
        for (Command cmd : commands) {
            System.out.println(" " + cmd.getHelp());
        }
    }

    @Override
    public void startAdditionalInput(InputStream inputStream) {
        throw new UnsupportedOperationException
                ("help не поддерживает дополнительный ввод");
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
