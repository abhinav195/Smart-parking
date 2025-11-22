package com.example.smartparking.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class Payment extends AuditableEntity{
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "ticket_id", nullable = false, columnDefinition = "uuid", unique = true)
    private UUID ticketId;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, columnDefinition = "payment_method")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "payment_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "reference", length = 100)
    private String reference;

    protected Payment() { }

    public Payment(UUID id, UUID ticketId, long amountMinor, String currency, PaymentMethod method) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        if(amountMinor < 0){
            throw new BusinessRuleException("amountMinor must be >= 0", "invalid_amount");
        }
        this.amountMinor = amountMinor;
        if(currency == null || currency.isEmpty()){
            throw new BusinessRuleException("currency must not be null or empty", "invalid_currency");
        }
        this.currency = currency;
        this.method = Objects.requireNonNull(method, "method must not be null");
        this.status = PaymentStatus.INITIATED;
    }

    //getters
    public UUID getId() { return id; }
    public UUID getTicketId() { return ticketId; }
    public long getAmountMinor() { return amountMinor; }
    public String getCurrency() { return currency; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public Instant getPaidAt() { return paidAt; }
    public String getReference() { return reference; }

    //Business Operations
    public void succeed(String reference, Instant paidAt) {
        if(this.status == PaymentStatus.SUCCESS){
            throw new ConflictException("Payment "+ id + " is already successful");
        }
        this.reference = reference;
        this.paidAt = paidAt;
        this.status = PaymentStatus.SUCCESS;
    }
    public void fail(){
        if(this.status == PaymentStatus.SUCCESS){
            throw new ConflictException("Payment "+ id + " is already successful");
        }
        this.status = PaymentStatus.FAILED;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;
        return id.equals(payment.id);
    }
    @Override
    public int hashCode(){
        return Objects.hash(id);
    }
}

