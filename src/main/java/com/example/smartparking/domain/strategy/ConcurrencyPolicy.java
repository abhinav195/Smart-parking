package com.example.smartparking.domain.strategy;

public interface ConcurrencyPolicy {
    enum Mode {CONSTRAINT_ONLY}
    Mode mode();
}
