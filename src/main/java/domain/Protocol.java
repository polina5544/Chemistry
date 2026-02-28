package domain;

import java.time.Instant;
import java.util.Set;

public final class Protocol {
    public long id;
    public String name;
    public Set<MeasurementParam> requiredParams;
    public Instant createdAt;

    public Protocol(long id, String name, Set<MeasurementParam> params) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Имя пустое");
        if (params == null || params.isEmpty()) throw new IllegalArgumentException("Нет параметров");

        this.id = id;
        this.name = name;
        this.requiredParams = params;
        this.createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Protocol p && id == p.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}