package storage;

public class StorageService {

    private final xmlStorage storage = new xmlStorage();

    public StorageData load(String path) {
        return storage.load(path);
    }

    public void save(String path, StorageData data) {
        storage.save(path,
                data.samples(),
                data.measurements(),
                data.protocols(),
                data.users());
    }
}