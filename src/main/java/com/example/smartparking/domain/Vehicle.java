package com.example.smartparking.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "vehicle")
public class Vehicle extends AuditableEntity{
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "license_plate", nullable = false, length = 50)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false, columnDefinition = "spot_size")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SpotSize size;

    protected Vehicle() {
        // for JPA
    }

    public Vehicle(UUID id, String licensePlate, SpotSize size) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.licensePlate = Objects.requireNonNull(licensePlate, "licensePlate must not be null");
        this.size = Objects.requireNonNull(size, "size must not be null");
    }

    //getters
    public UUID getId() { return id; }
    public String getLicensePlate() { return licensePlate; }
    public SpotSize getSize() { return size; }

    //setters
    public void updatePlate(String licensePlate) {
        this.licensePlate = Objects.requireNonNull(licensePlate, "licensePlate must not be null");
    }
    public void setSize(SpotSize size) {
        this.size = Objects.requireNonNull(size, "size must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        return id.equals(vehicle.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
