package cli;

import service.UserSession;

import java.util.Scanner;

public class LogoutCommand implements Command {

    private final UserSession session;

    public LogoutCommand(
            UserSession session
    ){

        this.session=session;
    }

    @Override
    public void execute(
            String[] args
    ){

        session.logout();

        System.out.println(
                "Вы вышли"
        );
    }

    @Override
    public String getHelp(){

        return "logout - ";

    }

    @Override
    public void validateArgs(String[] args) {
    }

    @Override
    public void startAdditionalInput(Scanner scanner) {

    }

    @Override
    public boolean isRequiredAdditionalInput() {
        return false;
    }

}