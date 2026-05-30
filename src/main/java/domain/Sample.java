package domain;

import service.UserSession;
import utilits.IDgenerator;

import java.time.Instant;

public class Sample {

    private long id;
    private String name;
    private String type;
    private String location;
    private SampleStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String ownerUsername;

    public Sample(long id,
                  String name,
                  String type,
                  String location,
                  SampleStatus status,
                  String ownerUsername,
                  Instant createdAt,
                  Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.status = status;
        this.ownerUsername = ownerUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

    // Краткий конструктор для UI и загрузки из XML
    // type и location получают заглушку, чтобы объект прошёл через валидацию
    // ВАЖНО: после загрузки из XML используй полный конструктор (см. xmlStorage)
    public Sample(long id, String name, SampleStatus status, String ownerUsername) {
        this.id = IDgenerator.nextId();
        this.name = name;
        this.type = "unknown";
        this.location = "unknown";
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getLocation() { return location; }
    public SampleStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(SampleStatus status) { this.status = status; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}