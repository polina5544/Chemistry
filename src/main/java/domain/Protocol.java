package domain;

import java.time.Instant;
import java.util.Set;

public class Protocol {

    private long id;
    private String name;
    private Set<MeasurementParam> requiredParams; //Коллекция Set, которая хранит обязательные параметры, Set тк нельзя дублировать параметры
    private String ownerUsername;
    private Instant createdAt;
    private Instant updatedAt;

    public Protocol(long id,
                    String name,
                    Set<MeasurementParam> requiredParams,
                    String ownerUsername,
                    Instant createdAt,
                    Instant updatedAt) {

        this.id = id;
        this.name = name;
        this.requiredParams = requiredParams;
        this.ownerUsername = ownerUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public String getOwnerUsername() { return ownerUsername; }
    public String getName() { return name; }
    public Set<MeasurementParam> getRequiredParams() { return requiredParams; }
}