package storage;
import domain.Measurement;
import domain.Sample;
import domain.Protocol;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

//отвечает только за работу с файлом - save - записывает данные в xml а load - читает xml и возвращает объекты

public class xmlStorage {
    //сохранение
    public void save(String path,
                         Set<Sample> samples,
                         Set<Measurement> measurements,
                         Set<Protocol> protocols) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder(); // умеет читать xml и создавать его
            Document doc = builder.newDocument(); // создается пустой xml-документ

            // корень
            Element root = doc.createElement("data"); //создали корневой элемент data
            doc.appendChild(root);

            // построение куска DOM с образцами
            Element samplesEl = doc.createElement("samples");
            root.appendChild(samplesEl); //пришпандорили к корну data ( samples будут внутри)

            for (Sample s : samples) {
                Element sampleEl = doc.createElement("sample");

                sampleEl.appendChild(createElement(doc, "id", String.valueOf(s.getId())));
                sampleEl.appendChild(createElement(doc, "name", s.getName()));
                sampleEl.appendChild(createElement(doc, "status", s.getStatus().name()));
                sampleEl.appendChild(createElement(doc, "owner", s.getOwnerUsername()));

                samplesEl.appendChild(sampleEl);
            }

            // так же присобачиваем образцы
            Element measEl = doc.createElement("measurements");
            root.appendChild(measEl);

            for (Measurement m : measurements) {
                Element mEl = doc.createElement("measurement");

                mEl.appendChild(createElement(doc, "id", String.valueOf(m.getId())));
                mEl.appendChild(createElement(doc, "sampleId", String.valueOf(m.getSampleId())));
                mEl.appendChild(createElement(doc, "param", m.getParam().name()));
                mEl.appendChild(createElement(doc, "value", String.valueOf(m.getValue())));
                mEl.appendChild(createElement(doc, "unit", m.getUnit()));
                mEl.appendChild(createElement(doc, "method", m.getMethod()));
                mEl.appendChild(createElement(doc, "createdAt", m.getMeasuredAt().toString()));
                measEl.appendChild(mEl);
            }

            //так же присобачиваем протоколы
            Element protEl = doc.createElement("protocols");
            root.appendChild(protEl);

            for (Protocol p : protocols) {
                Element pEl = doc.createElement("protocol");
                pEl.appendChild(createElement(doc, "id", String.valueOf(p.getId())));
                pEl.appendChild(createElement(doc, "name", p.getName()));
                protEl.appendChild(pEl);
            }

            // запись в файл
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.transform(new DOMSource(doc), new StreamResult(new File(path)));

            System.out.println("OK: данные сохранены в " + path);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения: " + e.getMessage());
        }
    }

    // загрузка
    public StorageData load(String path) {

        try {
            File file = new File(path);

            if (!file.exists()) {
                throw new IllegalArgumentException("Файл не найден");
            }

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(file); //Он берет свои внутренние инструменты (алгоритмы парсинга) и превращает текст файла в дерево объектов Java (Document).

            doc.getDocumentElement().normalize();

            Set<Sample> samples = new HashSet<>();
            Set<Measurement> measurements = new HashSet<>();
            Set<Protocol> protocols = new HashSet<>();

            NodeList sampleNodes = doc.getElementsByTagName("sample"); //NideList специальный тип данных (коллекция) в Java DOM API.

            for (int i = 0; i < sampleNodes.getLength(); i++) {
                Element el = (Element) sampleNodes.item(i); //кастинг как перестраховка

                Sample s = new Sample( // так как тип - Element я имею доступ к расширенным функциям а именно методы Element которых нет у Node
                        Long.parseLong(get(el, "id")),
                        get(el, "name"),
                        Enum.valueOf(domain.SampleStatus.class, get(el, "status")),
                        get(el, "owner")
                ); //собрали нормальный понятный жаве образец и добавили в нашу замечательную коллекцию

                samples.add(s);
            }

            NodeList measNodes = doc.getElementsByTagName("measurement");

            for (int i = 0; i < measNodes.getLength(); i++) {
                Element el = (Element) measNodes.item(i);

                Measurement m = new Measurement(
                        Long.parseLong(get(el, "id")),
                        Long.parseLong(get(el, "sampleId")),
                        Enum.valueOf(domain.MeasurementParam.class, get(el, "param")),
                        Double.parseDouble(get(el, "value")),
                        get(el, "unit"),
                        get(el, "method"),
                        Instant.parse(get(el, "createdAt")),
                        null,
                        Instant.now(),
                        Instant.now()
                );

                measurements.add(m);
            }

            NodeList protNodes = doc.getElementsByTagName("protocol");

            for (int i = 0; i < protNodes.getLength(); i++) {
                Element el = (Element) protNodes.item(i);

                Protocol p = new Protocol(
                        Long.parseLong(get(el, "id")),
                        get(el, "name")
                );

                protocols.add(p);
            }

            System.out.println("OK: данные загружены");

            return new StorageData(samples, measurements, protocols); //упаковвываем все загруженные данные в один объект

        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки: " + e.getMessage());
        }
    }

    // вспомогательный метод
    private Element createElement(Document doc, String name, String value) {
        Element el = doc.createElement(name);
        el.appendChild(doc.createTextNode(value));
        return el;
    }

    private String get(Element el, String tag) {//выдаст список нод лист и возьмет из него самый первый элемент
        return el.getElementsByTagName(tag).item(0).getTextContent();
    }
}
