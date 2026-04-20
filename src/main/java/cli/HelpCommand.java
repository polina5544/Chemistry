package cli;

/*
 * help: вывести справку по доступным командам
 */

import java.io.*;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

public class HelpCommand implements Command {
    private final Set<Command> commands;

    public HelpCommand(Set<Command> commands) {
        this.commands = commands;
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
    public boolean equals(Object o) {
        if (this == o) return true; // Java сравнивает ссылки (адреса в памяти). Если this (текущий объект) и o (пришедший на вход) указывают на одну и ту же ячейку памяти, то это буквально один и тот же экземпляр.
        if (o == null || getClass() != o.getClass()) return false; // проверка на пренадлежность классу
        Command command = (Command) o; // casting - приведение типа то есть надо обращаться с ним как с командой, а не как с абстрактным объектом Object, так как у Object нет метода getHelp()
        return Objects.equals(getHelp(), command.getHelp()); //Объекты считаются равными, если это не null, они одного класса и у них одинаковое поле help. Кстати значение обновляется а клюс сохраняется
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHelp());
    } // переопределение с equals hash(getHelp() преобразует аргумент в число, берет строку из help и вычисляет на её основе число. Если help у двух объектов одинаковый, то и число (хэш) будет одинаковым.

    @Override
    public void startAdditionalInput(Scanner scanner) {

    }

    @Override
    public boolean isRequiredAdditionalInput() {
        return false;
    }
}
