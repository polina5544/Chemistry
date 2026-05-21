package cli;

import domain.User;
import service.UserService;
import service.UserSession;

import java.util.Scanner;

public class RegisterCommand implements Command {

    private final UserService userService;
    private final UserSession session;

    private final String path;

    public RegisterCommand(

            UserService userService,

            UserSession session,


            String path){

        this.userService=userService;

        this.session=session;

        this.path=path;

    }

    @Override
    public boolean isRequiredAdditionalInput() {
        return true;
    }

    @Override
    public String getHelp(){
        return "register";
    }

    @Override
    public void execute( String[] args){

    }

    @Override
    public void validateArgs(String[] args) {

    }

    @Override
    public void startAdditionalInput(Scanner scanner){

        System.out.println("Логин:");

        String login= scanner.nextLine();

        System.out.println("Пароль:");

        String pass= scanner.nextLine();

        User user= userService.register(login, pass);

        session.login(user);

        System.out.println("Регистрация завершена");
    }
}