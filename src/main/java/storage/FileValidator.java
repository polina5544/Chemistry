package storage;

import org.w3c.dom.*;

import java.util.HashSet;
import java.util.Set;

public class FileValidator {

    public void validate(Document doc) {

        if (doc == null) {
            throw new IllegalArgumentException("Ошибка: файл не читается");
        }

        doc.getDocumentElement().normalize();

        validateSamples(doc);
        validateMeasurements(doc);
    }

    private void validateSamples(Document doc) {

        NodeList nodes = doc.getElementsByTagName("sample");
        Set<Long> sampleIdsSeen = new HashSet<>();

        for (int i = 0; i < nodes.getLength(); i++) { // прошли по всем xml элементам sample
            Element el = (Element) nodes.item(i); //кастинг
            long SampleId = parseLong(get(el, "id"), "sample.id");

            String name = get(el, "name");
            String status = get(el, "status");

            // обязательные поля
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: поле name пустое у объекта id=" + SampleId
                );
            }

            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: поле status пустое у объекта id=" + SampleId
                );
            }

            try {
                Enum.valueOf(domain.SampleStatus.class, status);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: неверный status у sample id=" + SampleId
                );
            }

            long sampleId = parseLong(get(el, "id"), "sample.id");

            if (sampleId <= 0) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: id должен быть > 0 у sample id=" + sampleId
                );
            }

            if (!sampleIdsSeen.add(SampleId)) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: id не уникален у sample id=" + SampleId
                );
            }
        }
    }

    private void validateMeasurements(Document doc) {

        NodeList nodes = doc.getElementsByTagName("measurement");

        Set<Long> measIdsSeen = new HashSet<>();// храняться все id из sample для meas но это только для локальной проверки
        Set<Long> existingSampleId = getSampleIds(doc); // собрали все id из sample

        for (int i = 0; i < nodes.getLength(); i++) {

            Element el = (Element) nodes.item(i);
            long idMeas = parseLong(get(el, "id"), "measurement.id"); //Id измерения
            long sampleIdFromMeas = parseLong(get(el, "sampleId"), "measurement.sampleId");//Id образца на которое ссылается id данного измерения

            String param = get(el, "param");
            String valueStr = get(el, "value");

            // обязательные поля
            if (param == null || param.isBlank()) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: поле param пустое у объекта id=" + idMeas
                );
            }

            if (valueStr == null || valueStr.isBlank()) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: поле value пустое у объекта id=" + idMeas
                );
            }

            double value = parseDouble(valueStr, "measurement.value");

            try {
                Enum.valueOf(domain.MeasurementParam.class, param);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: неверный param у measurement id=" + idMeas
                );
            }

            if (value < 0) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: значение value < 0 у measurement id=" + idMeas
                );
            }

            if (!measIdsSeen.add(idMeas)) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: id не уникален у measurement id=" + idMeas
                );
            }

            if (!existingSampleId.contains(sampleIdFromMeas)) {
                throw new IllegalArgumentException(
                        "Ошибка загрузки: measurement.sampleId=" + sampleIdFromMeas +
                                " ссылается на несуществующий sample"
                );
            }
        }
    }

    // сбор всех sample id - это первый этап тк все измерения обязаны ссылаться на образцы
    private Set<Long> getSampleIds(Document doc) {

        NodeList nodes = doc.getElementsByTagName("sample");
        Set<Long> ids = new HashSet<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            ids.add(Long.parseLong(get(el, "id")));
        }

        return ids;
    }

    private String get(Element el, String tag) {
        NodeList list = el.getElementsByTagName(tag);

        if (list == null || list.getLength() == 0 || list.item(0) == null) {
            throw new IllegalArgumentException(
                    "Ошибка XML: у sample нет тега <" + tag + ">"
            );
        }

        String value = list.item(0).getTextContent();

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Ошибка XML: тег <" + tag + "> пустой"
            );
        }

        return value;
    }

    private long parseLong(String value, String field) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Ошибка загрузки: " + field + " не число"
            );
        }
    }

    private double parseDouble(String value, String field) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Ошибка загрузки: " + field + " не число"
            );
        }
    }
}