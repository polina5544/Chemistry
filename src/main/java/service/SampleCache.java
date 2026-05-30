package service;

import domain.Sample;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SampleCache {
    // мэп с ключом = id образца, значение = сам образец
    private final Map<Long, Sample> cache = new ConcurrentHashMap<>();

    // при старте приложения все образцы подгружаются в табличку из бд
    public void loadAll(List<Sample> samples) {
        cache.clear();
        for (Sample s : samples) {
            cache.put(s.getId(), s);
        }
    }

    // получает образцы для отображения в таблице
    public List<Sample> getAll() {
        return new ArrayList<>(cache.values());
    }

    public void add(Sample sample) {
        cache.put(sample.getId(), sample);
    }
    public void update(Sample sample) {
        cache.put(sample.getId(), sample);
    }
    public void remove(Long id) {
        cache.remove(id);
    }

    // чекает, есть ли образец уже в кеше
    public boolean contains(Long id) {
        return cache.containsKey(id);
    }
}
