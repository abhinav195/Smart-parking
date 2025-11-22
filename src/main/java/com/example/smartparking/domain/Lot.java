package com.example.smartparking.domain;

import jakarta.persistence.*;

import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
@Entity
@Table(name = "lot" )
public class Lot extends AuditableEntity{
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezoneId;

    @Column(name = "maintenance_mode", nullable = false)
    private boolean maintenanceMode;

    protected Lot() {
        // for JPA
    }

    public Lot(UUID id, String name, String address, ZoneId timezone) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.address = address;
        this.timezoneId = Objects.requireNonNull(timezone, "timezone").getId();
        this.maintenanceMode = false;
    }
    //getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public ZoneId getTimezone() { return ZoneId.of(timezoneId); }
    public boolean getMaintenanceMode() { return maintenanceMode; }

    //Business Operations
    public void rename(String newName) {
        this.name = Objects.requireNonNull(newName, "name should not be null");
    }
    public void setAddress(String newAddress) {
        this.address = newAddress;
    }
    public void changeTimezone(ZoneId timezone) {
        this.timezoneId = Objects.requireNonNull(timezone, "timezone").getId();
    }
    public void enableMaintenanceMode() {
        this.maintenanceMode = true;
    }
    public void disableMaintenanceMode() {
        this.maintenanceMode = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Lot)) return false;
        Lot lot = (Lot) o;
        return id.equals(lot.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
