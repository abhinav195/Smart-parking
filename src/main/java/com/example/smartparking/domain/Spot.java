package com.example.smartparking.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Objects;
import java.util.UUID;
@Entity
@Table(name = "spot")
public class Spot extends AuditableEntity{
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "floor_id", nullable = false, columnDefinition = "uuid")
    private UUID floorId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false, columnDefinition = "spot_size")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SpotSize size;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "spot_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SpotStatus status;

    protected Spot() {
        // for JPA
    }

    public Spot(UUID id, UUID floorId, String code, SpotSize size, SpotStatus status) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.floorId = Objects.requireNonNull(floorId, "floorId must not be null");
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.size = Objects.requireNonNull(size, "size must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    //getters
    public UUID getId() { return id; }
    public UUID getFloorId() { return floorId; }
    public String getCode() { return code; }
    public SpotSize getSize() { return size; }
    public SpotStatus getStatus() { return status; }

    //Business Operations
    public void relabel(String newCode) {
        this.code = Objects.requireNonNull(newCode, "Code should not be null");
    }
    public void markAvailable() {
        this.status = SpotStatus.AVAILABLE;
    }
    public void reserve(){
        if(status != SpotStatus.AVAILABLE){
            throw new BusinessRuleException(
                    "Cannot occupy spot " + code + " with status " + status, "spot_not_available"
            );
        }
        this.status = SpotStatus.RESERVED;
    }
    public void occupy(){
        if(status != SpotStatus.AVAILABLE && status != SpotStatus.RESERVED){
            throw new BusinessRuleException(
                    "Cannot occupy spot " + code + " with status " + status, "spot_not_reserved"
            );
        }
        this.status = SpotStatus.OCCUPIED;
    }
    public void outOfService(){
        this.status = SpotStatus.OUT_OF_SERVICE;
    }
    public boolean isAvailable(){
        return status == SpotStatus.AVAILABLE;
    }
    public boolean isCompatible(SpotSize vehicleSize){
        return switch (this.size){
            case SMALL -> vehicleSize == SpotSize.SMALL || vehicleSize == SpotSize.MEDIUM || vehicleSize == SpotSize.LARGE;
            case MEDIUM -> vehicleSize == SpotSize.MEDIUM || vehicleSize == SpotSize.LARGE;
            case LARGE -> vehicleSize == SpotSize.LARGE;
            case EV -> vehicleSize == SpotSize.EV;
            case BIKE -> vehicleSize == SpotSize.BIKE;
        };
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Spot)) return false;
        Spot spot = (Spot) o;
        return id.equals(spot.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

