package service;

import domain.*;
import utilits.IDgenerator;
import validation.SampleValidation;

import java.time.Instant;
import java.util.*;

public class SampleService {

    // Единое хранилище — только Map
    private final Map<Long, Sample> storage = new LinkedHashMap<>();

    public Sample add(String name, String type, String location, String owner) {
        long id = IDgenerator.nextId();
        Sample sample = new Sample(
                id,
                name,
                type,
                location,
                SampleStatus.ACTIVE,
                owner,
                Instant.now(),
                Instant.now()
        );
        SampleValidation.validate(sample);
        storage.put(id, sample);
        return sample;
    }

    public Sample getById(long id) {
        Sample s = storage.get(id);
        if (s == null)
            throw new NoSuchElementException("Ошибка: объект с id " + id + " не найден");
        return s;
    }

    // Используется из UI (Add через диалог)
    public void addSample(Sample sample) {
        if (sample == null)
            throw new IllegalArgumentException("Sample не может быть null");
        if (storage.containsKey(sample.getId()))
            throw new IllegalArgumentException("Sample с таким id уже существует: " + sample.getId());
        storage.put(sample.getId(), sample);
    }

    // Используется после загрузки из XML
    public void setSamples(Collection<Sample> newSamples) {
        storage.clear();
        for (Sample s : newSamples) {
            storage.put(s.getId(), s);
        }
    }

    public void deleteSample(long id) {
        if (storage.remove(id) == null)
            throw new IllegalArgumentException("Sample с id =" + id + " не найден");
    }

    public List<Sample> getAll() {
        return new ArrayList<>(storage.values());
    }
}
