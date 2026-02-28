package domain;

import java.time.Instant;

public final class Sample {
    public long id;
    public String name;
    public String type;
    public String location;
    public SampleStatus status;
    public String ownerUsername;
    public Instant createdAt;
    public Instant updatedAt;

    public Sample(long id, String name, String type, String location, String owner) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Название пустое");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("Тип пустой");
        if (location == null || location.isBlank()) throw new IllegalArgumentException("Локация пустая");

        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.status = SampleStatus.ACTIVE;
        this.ownerUsername = owner;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Sample s && id == s.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}