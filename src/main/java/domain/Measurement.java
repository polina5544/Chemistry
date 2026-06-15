package domain;

import domain.MeasurementParam;

import java.time.Instant;
import java.util.Objects;

public class Measurement {

    private long id;
    private long sampleId;
    private MeasurementParam param;
    private double value;
    private String unit;
    private String method;
    private Instant measuredAt;
    private String ownerUsername;
    private Instant createdAt;
    private Instant updatedAt;

    // Конструктор для создания нового измерения (без id)
    public Measurement(long sampleId, MeasurementParam param, double value,
                       String unit, String method, Instant measuredAt, String ownerUsername) {
        this.sampleId = sampleId;
        this.param = param;
        this.value = value;
        this.unit = unit;
        this.method = method;
        this.measuredAt = measuredAt;
        this.ownerUsername = ownerUsername;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Конструктор для загрузки из БД
    public Measurement(long id, long sampleId, MeasurementParam param, double value,
                       String unit, String method, Instant measuredAt, String ownerUsername,
                       Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sampleId = sampleId;
        this.param = param;
        this.value = value;
        this.unit = unit;
        this.method = method;
        this.measuredAt = measuredAt;
        this.ownerUsername = ownerUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public long getSampleId() { return sampleId; }
    public MeasurementParam getParam() { return param; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public String getMethod() { return method; }
    public Instant getMeasuredAt() { return measuredAt; }
    public String getOwnerUsername() { return ownerUsername; }


    public void setValue(double value) { this.value = value; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setMethod(String method) { this.method = method; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Measurement that = (Measurement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}