package cli;
import java.util.Scanner;

public interface Command {
    void execute(String[]args);
    void validateArgs(String[]args);
    String getHelp();
    boolean equals(Object o);
    int hashCode();
    void startAdditionalInput(Scanner scanner);
    boolean isRequiredAdditionalInput();
}
