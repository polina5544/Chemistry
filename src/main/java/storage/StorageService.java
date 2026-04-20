package storage;

import storage.StorageData;
import storage.xmlStorage;

public class StorageService {

    private final xmlStorage storage = new xmlStorage();

    public StorageData load(String path) {
        return storage.load(path);
    }

    public void save(String path, StorageData data) {
        storage.save(path,
                data.getSamples(),
                data.getMeasurements(),
                data.getProtocols());
    }
}