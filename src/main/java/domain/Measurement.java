package domain;

import domain.MeasurementParam;

import java.time.Instant;

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

    public Measurement(long id,
                       long sampleId,
                       MeasurementParam param,
                       double value,
                       String unit,
                       String method,
                       Instant measuredAt,
                       String ownerUsername,
                       Instant createdAt,
                       Instant updatedAt) {

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
}