package com.example.smartparking.application.mapper;

import com.example.smartparking.application.ParkingQueryService.FloorSummary;
import com.example.smartparking.application.ParkingQueryService.LotSummary;
import com.example.smartparking.application.ParkingQueryService.SpotSummary;
import com.example.smartparking.application.ParkingQueryService.ActiveTicketSummary;
import com.example.smartparking.domain.*;

import org.springframework.stereotype.Component;

@Component
public class ParkingQueryMapper {

    public LotSummary toLotSummary(Lot lot) {
        return new LotSummary(
                lot.getId(),
                lot.getName(),
                lot.getAddress()
        );
    }

    public FloorSummary toFloorSummary(Floor floor) {
        return new FloorSummary(
                floor.getId(),
                floor.getLabel(),
                floor.getOrdering()
        );
    }

    public SpotSummary toSpotSummary(Spot spot) {
        return new SpotSummary(
                spot.getId(),
                spot.getFloorId(),
                spot.getCode(),
                spot.getSize(),
                spot.getStatus()
        );
    }

    /**
     * Builds an ActiveTicketSummary from ticket, spot and vehicle.
     * Assumes the ticket is OPEN and spot belongs to the same lot.
     */
    public ActiveTicketSummary toActiveTicketSummary(
            Ticket ticket,
            Spot spot
    ) {
        return new ActiveTicketSummary(
                ticket.getId(),
                ticket.getLotId(),
                ticket.getSpotId(),
                spot.getCode(),
                spot.getSize()
        );
    }
}