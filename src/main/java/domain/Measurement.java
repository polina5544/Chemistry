package domain;

import java.time.Instant;

public final class Measurement {
    public long id;
    public long sampleId;
    public MeasurementParam param;
    public double value;
    public String unit;
    public String method;
    public Instant measuredAt;

    public Measurement(long id, long sampleId, MeasurementParam param,
                       double value, String unit, String method) {
        if (unit == null || unit.isBlank()) throw new IllegalArgumentException("unit empty");
        if (method == null || method.isBlank()) throw new IllegalArgumentException("method empty");

        this.id = id;
        this.sampleId = sampleId;
        this.param = param;
        this.value = value;
        this.unit = unit;
        this.method = method;
        this.measuredAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Measurement m && id == m.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}