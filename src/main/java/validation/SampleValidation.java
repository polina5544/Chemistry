package validation;

import domain.Sample;

public class SampleValidation {

    public static void validate(Sample s) {

        if (s.getName() == null || s.getName().isBlank()) {
            throw new IllegalArgumentException("Ошибка: name не может быть пустым");
        }

        if (s.getName().length() > 64) {
            throw new IllegalArgumentException("Ошибка: name слишком длинное (макс. 64 символа)");
        }

        if (s.getType() == null || s.getType().isBlank())
            throw new IllegalArgumentException("Ошибка: type не может быть пустым");

        if (s.getLocation() == null || s.getLocation().isBlank())
            throw new IllegalArgumentException("Ошибка: location не может быть пустым");

        if (s.getLocation().length() > 64)
            throw new IllegalArgumentException("Ошибка: location слишком длинное (макс. 64 символа)");

        if (s.getOwnerUsername() == null || s.getOwnerUsername().isBlank())
            throw new IllegalArgumentException("Ошибка: нужен создатель, образенц не сам себя создал!");
    }
}

