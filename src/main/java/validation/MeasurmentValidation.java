package validation;

import domain.Measurement;

public class MeasurmentValidation {

    public static void validate(Measurement m) {

        double value = m.getValue();

        if (m.getValue() <= 0)
            throw new IllegalArgumentException("Ошибка: значение должно быть больше 0");

        if (m.getUnit() == null || m.getUnit().isBlank())
            throw new IllegalArgumentException("Ошибка: единицы не могут быть пустыми");

        if (m.getMethod() == null || m.getMethod().isBlank())
            throw new IllegalArgumentException("Ошибка: метод не может быть пустым");

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Ошибка: значение измерения должно быть обычным числом");
        }
    }
}