package service;

import db.SampleRepository;
import domain.Sample;
import domain.SampleStatus;
import validation.SampleValidation;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

// SampleService - бизнес-логика для образцов
// Каждый вызов метода идёт напрямую в PostgreSQL через SampleRepository.
// Цепочка: Команда/UI → SampleService (логика) → SampleRepository (SQL) → PostgreSQL

public class SampleService {

    // Единственное хранилище - PostgreSQL через репозиторий
    private final SampleRepository repo = new SampleRepository();
    private final SampleCache cache = new SampleCache();

//     Создать новый образец через CLI (sample_add)
//     Валидируем поля потом сохраняем в БД потом возвращаем с id от БД
//     owner логин создателя (берётся из UserSession автоматически)

    public Sample add(String name, String type, String location, String owner) {
        // Создаём объект с id=0 - заглушка, настоящий id придёт из БД после INSERT
        Sample sample = new Sample(0L, name, type, location,
                SampleStatus.ACTIVE, owner, Instant.now(), Instant.now());

        // Проверяем бизнес-правила: не пустые поля, длина и т.д.
        SampleValidation.validate(sample);

        // repo.save() делает INSERT и возвращает объект с реальным id от PostgreSQL
        return repo.save(sample);
    }

    public Sample getById(long id) {
        Sample s = repo.findById(id);
        if (s == null) {
            throw new NoSuchElementException("Ошибка: образец с id =" + id + " не найден");
        }
        return s;
    }

    public void init() {
        List<Sample> allFromDb = repo.findAll();
        cache.loadAll(allFromDb);
        System.out.println("Загружено " + allFromDb.size() + " образцов в кэш");
    }

    // Добавить готовый объект Sample (используется из UI диалога Add)
    // id в объекте игнорируется — БД присвоит свой
    // Возвращает сохранённый объект с реальным id

    public Sample addSample(Sample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("Образец не может ничего не содержать");
        }
        SampleValidation.validate(sample);
        System.out.println("Сохранение образца с владельцем: " + sample.getOwnerUsername());
        Sample saved = repo.save(sample); // сохраняем образец в БД и получаем айди
        System.out.println("Образец сохранён. Владелец: " + saved.getOwnerUsername());
        cache.add(saved);                   // синхронизация в кэш
        return saved;
    }

    // Обновить поля образца в БД
    public void update(Sample sample) {
        SampleValidation.validate(sample);
        repo.update(sample);
        cache.update(sample);
    }

    // Удалить образец. Измерения удалятся через CASCADE в схеме БД
    public void deleteSample(long id) {
        // прост удаляется из БД
        repo.delete(id);
        cache.remove(id);
    }

    public void refresh() {
        init();  // перезагрузка кэша из БД
    }

    public List<Sample> getAll() {
        return cache.getAll(); // всё получается из кеша, а не из БД, что гораздо быстрее
    }

    public List<Sample> getByStatus(SampleStatus status) {
        return repo.findByStatus(status);
    }
}