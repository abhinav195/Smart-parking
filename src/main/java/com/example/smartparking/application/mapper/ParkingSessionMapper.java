package com.example.smartparking.application.mapper;

import com.example.smartparking.application.ParkingSessionService.CheckInResult;
import com.example.smartparking.application.ParkingSessionService.CheckOutResult;
import com.example.smartparking.domain.Payment;
import com.example.smartparking.domain.Spot;
import com.example.smartparking.domain.Ticket;

import org.springframework.stereotype.Component;

@Component
public class ParkingSessionMapper {

    /**
     * Builds the CheckInResult DTO from the newly created ticket and allocated spot.
     */
    public CheckInResult toCheckInResult(Ticket ticket, Spot spot) {
        return new CheckInResult(
                ticket.getId(),
                spot.getId(),
                spot.getFloorId(),
                spot.getCode(),
                spot.getSize(),
                ticket.getEntryAt()
        );
    }

    /**
     * Builds the CheckOutResult DTO from the closed ticket and its payment.
     */
    public CheckOutResult toCheckOutResult(Ticket ticket, Payment payment) {
        return new CheckOutResult(
                ticket.getId(),
                payment.getId(),
                payment.getAmountMinor(),
                payment.getCurrency(),
                ticket.getEntryAt(),
                ticket.getExitAt()
        );
    }
}