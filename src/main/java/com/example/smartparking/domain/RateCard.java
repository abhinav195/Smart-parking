package com.example.smartparking.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "rate_card")
public class RateCard extends AuditableEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(name = "lot_id", columnDefinition = "uuid")
    private UUID lotId;

    @Column(name = "floor_id", columnDefinition = "uuid")
    private UUID floorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", columnDefinition = "spot_size")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SpotSize size;
//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "rateCardId", cascade = CascadeType.ALL, orphanRemoval = true)
//    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    @Transient
    private List<RateRule> rules = new ArrayList<>();

    protected RateCard() {
        // for JPA
    }

    public RateCard(UUID id, String name, String currency, Instant effectiveFrom, Instant effectiveTo) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom must not be null");
        this.effectiveTo = effectiveTo;
    }

    //getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public Instant getEffectiveFrom() { return effectiveFrom; }
    public Instant getEffectiveTo() { return effectiveTo; }
    public UUID getLotId() { return lotId; }
    public UUID getFloorId() { return floorId; }
    public SpotSize getSize() { return size; }
    public List<RateRule> getRules() { return rules; }

    //business operations
    public void scopeToLot(UUID lotId){
        this.lotId = lotId;
    }
    public void scopeToFloor(UUID floorId){
        this.floorId = floorId;
    }
    public void scopeToSize(SpotSize size){
        this.size = size;
    }
    public void rename(String name){
        this.name = name;
    }
    public void setEffectiveTo(Instant effectiveTo){
        this.effectiveTo = effectiveTo;
    }
    public void addRule(RateRule rule){
        this.rules.add(Objects.requireNonNull(rule, "rule must not be null"));
    }
    public void clearRules(){
        this.rules.clear();
    }
    public boolean isActive(Instant at){
        return !at.isBefore(effectiveFrom) && (effectiveTo == null || at.isBefore(effectiveTo));
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof RateCard)) return false;
        RateCard rateCard = (RateCard) o;
        return id.equals(rateCard.id);
    }
    @Override
    public int hashCode(){
        return Objects.hash(id);
    }
}
