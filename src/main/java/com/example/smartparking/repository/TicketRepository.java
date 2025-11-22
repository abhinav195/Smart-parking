package com.example.smartparking.repository;

import com.example.smartparking.domain.Ticket;
import com.example.smartparking.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findBySpotIdAndStatus(UUID spotId, TicketStatus status);

    Optional<Ticket> findByVehicleIdAndLotIdAndStatus(
            UUID vehicleId,
            UUID lotId,
            TicketStatus status);
}