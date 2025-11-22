package com.example.smartparking.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.Objects;

@Entity
@Table(name = "rate_rule")
public class RateRule extends AuditableEntity{
    public enum Unit { MINUTE, HOUR, FLAT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "rate_card_id", nullable = false, columnDefinition = "uuid")
    private java.util.UUID rateCardId;

    @Column(name = "start_minute", nullable = false)
    private int startMinute;

    @Column(name = "end_minute")
    private Integer endMinute;

    @Column(name = "price_per_unit", nullable = false)
    private long pricePerUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, columnDefinition = "rate_unit")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Unit unit;

    protected RateRule() {
        // for JPA
    }

    public RateRule(int startMinute, Integer endMinute, long pricePerUnit, Unit unit) {
        if (startMinute < 0) throw new IllegalArgumentException("startMinute must be >= 0");
        if (endMinute != null && endMinute <= startMinute) {
            throw new IllegalArgumentException("endMinute must be > startMinute");
        }
        if (pricePerUnit < 0) throw new IllegalArgumentException("pricePerUnit must be >= 0");
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.pricePerUnit = pricePerUnit;
        this.unit = Objects.requireNonNull(unit, "unit must not be null");
    }

    //getters
    public int getStartMinute() { return startMinute; }
    public Integer getEndMinute() { return endMinute; }
    public long getPricePerUnit() { return pricePerUnit; }
    public Unit getUnit() { return unit; }

    @Override
    public String toString() {
        return "RateRule{" +
                "startMinute=" + startMinute +
                ", endMinute=" + endMinute +
                ", pricePerUnit=" + pricePerUnit +
                ", unit=" + unit +
                '}';
    }
}
