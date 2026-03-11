package cli;

import java.io.*;

public abstract class Command {
    public abstract void execute(String[] args); // главный метод, реализующий логику команды
    protected boolean requiredAdditionalInput = false;
    public final boolean isRequiredAdditionalInput() {
        return this.requiredAdditionalInput;
    } // геттер для дополнительного ввода
    public abstract void startAdditionalInput(InputStream inputStream); // метод, обрабатывающий дополнительный ввод
    public abstract void validateArgs(String[] args); // метод, обрабатывающий введённые аргументы
    public abstract String getHelp(); // метод для получения справки по команде
}
