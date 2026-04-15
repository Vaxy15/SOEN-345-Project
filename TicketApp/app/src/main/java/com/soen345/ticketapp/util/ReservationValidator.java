package com.soen345.ticketapp.util;

import com.soen345.ticketapp.model.Event;
import com.soen345.ticketapp.model.Reservation;
import java.util.List;

public class ReservationValidator {

    public enum Result { OK, NO_SEATS, ALREADY_BOOKED, EVENT_CANCELLED }

    public static Result canReserve(Event event, List<Reservation> userReservations) {
        if (event.isCancelled())             return Result.EVENT_CANCELLED;
        if (event.getAvailableSeats() <= 0)  return Result.NO_SEATS;
        for (Reservation r : userReservations) {
            if (r.getEventId() != null && r.getEventId().equals(event.getId()))
                return Result.ALREADY_BOOKED;
        }
        return Result.OK;
    }

    public static boolean canCancel(Reservation reservation) {
        return reservation != null && reservation.getId() != null;
    }
}