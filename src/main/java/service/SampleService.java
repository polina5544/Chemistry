package service;

import domain.*;
import validation.SampleValidation;

import java.time.Instant;
import java.util.*;

public class SampleService {

    private final Map<Long, Sample> storage = new HashMap<>();
    private long nextId = 1;

    public Sample add(String name, String type, String location, String owner) {

        Sample sample = new Sample(
                nextId,
                name,
                type,
                location,
                SampleStatus.ACTIVE,
                owner,
                Instant.now(),
                Instant.now()
        );

        SampleValidation.validate(sample);

        storage.put(nextId, sample);
        nextId++;

        return sample;
    }

    public Sample getById(long id) {
        Sample s = storage.get(id);
        if (s == null)
            throw new NoSuchElementException("Ошибка: объект с id " + id + " не найден");
        return s;
    }

    public List<Sample> getAll() {
        return new ArrayList<>(storage.values());
    }

    public void update(long id, String name, String type, String location) {

        Sample s = getById(id);

        s.setName(name);
        s.setType(type);
        s.setLocation(location);
        s.setUpdatedAt(Instant.now());

        SampleValidation.validate(s);
    }

    public void remove(long id) {
        if (storage.remove(id) == null)
            throw new NoSuchElementException("Ошибка: объект не найден");
    }
}