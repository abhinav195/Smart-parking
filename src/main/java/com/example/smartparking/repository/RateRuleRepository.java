package com.example.smartparking.repository;

import com.example.smartparking.domain.RateRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RateRuleRepository extends JpaRepository<RateRule, Long> {

    List<RateRule> findByRateCardIdOrderByStartMinuteAsc(UUID rateCardId);
}