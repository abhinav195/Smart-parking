package com.example.smartparking.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ticket")
public class Ticket extends AuditableEntity{
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "lot_id", nullable = false, columnDefinition = "uuid")
    private UUID lotId;

    @Column(name = "spot_id", nullable = false, columnDefinition = "uuid")
    private UUID spotId;

    @Column(name = "vehicle_id", nullable = false, columnDefinition = "uuid")
    private UUID vehicleId;

    @Column(name = "entry_at", nullable = false)
    private Instant entryAt;

    @Column(name = "exit_at")
    private Instant exitAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ticket_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TicketStatus status;

    protected Ticket() { }

    public Ticket(UUID id, UUID spotId, UUID vehicleId, UUID lotId, Instant enteryAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.spotId = Objects.requireNonNull(spotId, "spotId must not be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "vehicleId must not be null");
        this.lotId = Objects.requireNonNull(lotId, "lotId must not be null");
        this.entryAt = Objects.requireNonNull(enteryAt, "entryAt must not be null");
        this.status = TicketStatus.OPEN;
    }

    //getters
    public UUID getId() { return id; }
    public UUID getSpotId() { return spotId; }
    public UUID getVehicleId() { return vehicleId; }
    public UUID getLotId() { return lotId; }
    public Instant getEntryAt() { return entryAt; }
    public Instant getExitAt() { return exitAt; }
    public TicketStatus getStatus() { return status; }

    //Business Operations
    public void close(Instant exitAt){
        if(this.status == TicketStatus.CLOSED){
            throw new ConflictException("Ticket is already closed");
        }
        if(exitAt.isBefore(entryAt)){
            throw new BusinessRuleException("Exit time must be after entry time", "invalid_exit_time");
        }
        this.exitAt = Objects.requireNonNull(exitAt, "exitAt must not be null");
        this.status = TicketStatus.CLOSED;
    }
    public boolean isOpen(){
        return this.status == TicketStatus.OPEN;
    }
    public Duration getDuration(){
        if(exitAt == null){
            return Duration.between(entryAt, Instant.now());
        }
        return Duration.between(entryAt, exitAt);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Ticket)) return false;
        Ticket ticket = (Ticket) o;
        return id.equals(ticket.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
