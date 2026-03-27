package service;

import domain.*;
import utilits.IDgenerator;
import validation.SampleValidation;

import java.time.Instant;
import java.util.*;

public class SampleService {

    private final Map<Long, Sample> storage = new HashMap<>();
    long Id = IDgenerator.nextId();

    public Sample add(String name, String type, String location, String owner) {

        Sample sample = new Sample(
                Id,
                name,
                type,
                location,
                SampleStatus.ACTIVE,
                owner,
                Instant.now(),
                Instant.now()
        );

        SampleValidation.validate(sample);

        storage.put(Id, sample);

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

    public void update(Sample sample) {
        SampleValidation.validate(sample);
    }

    public void remove(long id) {
        if (storage.remove(id) == null)
            throw new NoSuchElementException("Ошибка: объект не найден");
    }
}