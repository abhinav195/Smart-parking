package com.example.smartparking.domain;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "floor")
public class Floor extends AuditableEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "lot_id", nullable = false, columnDefinition = "uuid")
    private UUID lotId;

    @Column(name = "label", nullable = false, length = 50)
    private String label;

    @Column(name = "ordering", nullable = false)
    private int ordering;


    protected Floor() {
        // for JPA
    }

    public Floor(UUID id, UUID lotId, String label, int ordering) {
        this.id = Objects.requireNonNull(id, "id should not be null");
        this.lotId = Objects.requireNonNull(lotId, "lotId should not be null");
        this.label = Objects.requireNonNull(label, "label should not be null");
        this.ordering = ordering;
    }

    //getters
    public UUID getId() { return id; }
    public UUID getLotId() { return lotId; }
    public String getLabel() { return label; }
    public int getOrdering() { return ordering; }

    //Business Operations
    public void relabel(String newLabel) {
        this.label = Objects.requireNonNull(newLabel, "label should not be null");
    }
    public void reorder(int newOrdering) {
        this.ordering = newOrdering;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Floor)) return false;
        Floor floor = (Floor) o;
        return id.equals(floor.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

