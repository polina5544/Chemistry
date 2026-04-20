package storage;

import domain.Measurement;
import domain.Protocol;
import domain.Sample;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class xmlStorage {

    public void save(String path,
                     Set<Sample> samples,
                     Set<Measurement> measurements,
                     Set<Protocol> protocols) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("data");
            doc.appendChild(root);

            // samples
            Element samplesEl = doc.createElement("samples");
            root.appendChild(samplesEl);

            for (Sample s : samples) {
                Element el = doc.createElement("sample");
                el.appendChild(create(doc, "id",       String.valueOf(s.getId())));
                el.appendChild(create(doc, "name",     s.getName()));
                el.appendChild(create(doc, "type",     s.getType() != null ? s.getType() : ""));
                el.appendChild(create(doc, "location", s.getLocation() != null ? s.getLocation() : ""));
                el.appendChild(create(doc, "status",   s.getStatus().name()));
                el.appendChild(create(doc, "owner",    s.getOwnerUsername() != null ? s.getOwnerUsername() : ""));
                samplesEl.appendChild(el);
            }

            // measurements
            Element measEl = doc.createElement("measurements");
            root.appendChild(measEl);

            for (Measurement m : measurements) {
                Element el = doc.createElement("measurement");
                el.appendChild(create(doc, "id",        String.valueOf(m.getId())));
                el.appendChild(create(doc, "sampleId",  String.valueOf(m.getSampleId())));
                el.appendChild(create(doc, "param",     m.getParam().name()));
                el.appendChild(create(doc, "value",     String.valueOf(m.getValue())));
                el.appendChild(create(doc, "unit",      m.getUnit()));
                el.appendChild(create(doc, "method",    m.getMethod()));
                el.appendChild(create(doc, "createdAt", m.getMeasuredAt().toString()));
                measEl.appendChild(el);
            }

            // protocols
            Element protEl = doc.createElement("protocols");
            root.appendChild(protEl);

            for (Protocol p : protocols) {
                Element el = doc.createElement("protocol");
                el.appendChild(create(doc, "id",   String.valueOf(p.getId())));
                el.appendChild(create(doc, "name", p.getName()));
                protEl.appendChild(el);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(new File(path)));

        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка сохранения: " + e.getMessage());
        }
    }

    public StorageData load(String path) {
        try {
            File file = new File(path);
            if (!file.exists())
                throw new IllegalArgumentException("Файл не найден: " + path);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(file);

            new FileValidator().validate(doc);

            Set<Sample> samples   = new HashSet<>();
            Set<Measurement> measurements = new HashSet<>();
            Set<Protocol> protocols  = new HashSet<>();

            // samples
            NodeList sampleNodes = doc.getElementsByTagName("sample");
            for (int i = 0; i < sampleNodes.getLength(); i++) {
                Element el = (Element) sampleNodes.item(i);

                // Используем полный конструктор - type и location берём из файла
                Sample s = new Sample(
                        Long.parseLong(get(el, "id")),
                        get(el, "name"),
                        getOrDefault(el, "type", "unknown"),
                        getOrDefault(el, "location", "unknown"),
                        Enum.valueOf(domain.SampleStatus.class, get(el, "status")),
                        getOrDefault(el, "owner", "unknown"),
                        Instant.now(),
                        Instant.now()
                );
                samples.add(s);
            }

            // measurements
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

            //protocols
            NodeList protNodes = doc.getElementsByTagName("protocol");
            for (int i = 0; i < protNodes.getLength(); i++) {
                Element el = (Element) protNodes.item(i);
                Protocol p = new Protocol(
                        Long.parseLong(get(el, "id")),
                        get(el, "name")
                );
                protocols.add(p);
            }

            return new StorageData(samples, measurements, protocols);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка загрузки: " + e.getMessage());
        }
    }

    private Element create(Document doc, String name, String value) {
        Element el = doc.createElement(name);
        el.appendChild(doc.createTextNode(value != null ? value : ""));
        return el;
    }

    private String get(Element el, String tag) {
        NodeList list = el.getElementsByTagName(tag);
        if (list == null || list.getLength() == 0)
            throw new IllegalArgumentException("Отсутствует тег <" + tag + ">");
        return list.item(0).getTextContent().trim();
    }

    // Возвращает значение тега или defaultValue, если тег отсутствует
    private String getOrDefault(Element el, String tag, String defaultValue) {
        NodeList list = el.getElementsByTagName(tag);
        if (list == null || list.getLength() == 0) return defaultValue;
        String val = list.item(0).getTextContent().trim();
        return val.isEmpty() ? defaultValue : val;
    }
}