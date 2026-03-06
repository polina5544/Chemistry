package validation;

import domain.Protocol;

public class ProtocolValidation {

    public static void validate(Protocol p) {

        if (p.getName().isEmpty())
            throw new IllegalArgumentException("Ошибка: значение должно быть больше 0");

        if (p.getName().length() > 128)
            throw new IllegalArgumentException("Ошибка: значение не должно привышать 128 символов");

        if (p.getRequiredParams() == null)
            throw new IllegalArgumentException("Ошибка: список пуст");

        if (p.getOwnerUsername() == null)
            throw new IllegalArgumentException("Ошибка: имя пользователя не должно быть пустым");
    }
}