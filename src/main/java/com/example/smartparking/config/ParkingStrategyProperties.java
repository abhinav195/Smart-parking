package com.example.smartparking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.parking")
public class ParkingStrategyProperties {
    public enum AllocationStrategy {
        ENTRANCE_NEAREST_RESERVATION_AWARE
    }
    public enum PricingStrategy {
        DEGRESSIVE_DAY_NIGHT_WEEKEND_WITH_PENALTY_AND_GRACE
    }
    public enum AvailabilityStrategy {
        PUSH_EVENTS
    }
    public enum ConcurrencyStrategy{
        CONSTRAINT_ONLY
    }
    private AllocationStrategy allocationStrategy = AllocationStrategy.ENTRANCE_NEAREST_RESERVATION_AWARE;
    private PricingStrategy pricingStrategy = PricingStrategy.DEGRESSIVE_DAY_NIGHT_WEEKEND_WITH_PENALTY_AND_GRACE;
    private AvailabilityStrategy availabilityStrategy = AvailabilityStrategy.PUSH_EVENTS;
    private ConcurrencyStrategy concurrencyStrategy = ConcurrencyStrategy.CONSTRAINT_ONLY;

    public AllocationStrategy getAllocationStrategy() {
        return allocationStrategy;
    }
    private void setAllocationStrategy(AllocationStrategy allocationStrategy) {
        this.allocationStrategy = allocationStrategy;
    }
    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }
    private void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }
    public AvailabilityStrategy getAvailabilityStrategy() {
        return availabilityStrategy;
    }
    private void setAvailabilityStrategy(AvailabilityStrategy availabilityStrategy) {
        this.availabilityStrategy = availabilityStrategy;
    }
    public ConcurrencyStrategy getConcurrencyStrategy() {
        return concurrencyStrategy;
    }
    private void setConcurrencyStrategy(ConcurrencyStrategy concurrencyStrategy) {
        this.concurrencyStrategy = concurrencyStrategy;
    }
}
