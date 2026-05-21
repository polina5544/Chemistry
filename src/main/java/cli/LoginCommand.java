package cli;

import domain.User;
import service.UserService;
import service.UserSession;

import java.util.Scanner;

public class LoginCommand implements Command {

    private final UserService userService;
    private final UserSession session;

    public LoginCommand(
            UserService userService,
            UserSession session){

        this.userService=userService;
        this.session=session;
    }

    @Override
    public boolean isRequiredAdditionalInput() {
        return true;
    }

    @Override
    public String getHelp() {
        return "Команда для регистрации нового пользователя";
    }

    @Override
    public void validateArgs(
            String[] args){

        if(args.length!=0)
            throw new IllegalArgumentException(
                    "login без аргументов, введите данные - логин и пароль"
            );
    }

    @Override
    public void execute(
            String[] args
    ){}

    @Override
    public void startAdditionalInput(
            Scanner scanner
    ){

        String login;
        String password;

        System.out.println("Логин:");
        login=scanner.nextLine();

        System.out.println("Пароль:");
        password=scanner.nextLine();

        User user=
                userService.authenticate(
                        login,
                        password
                );

// сохдается обьект сессии и делает этого пользователя текущим
        session.login(user);

        System.out.println(
                "Вход выполнен"
        );

    }
}