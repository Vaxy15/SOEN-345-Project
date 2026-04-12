// app/src/test/java/com/soen345/ticketapp/BookingRepositoryTest.java
package com.soen345.ticketapp;

import com.soen345.ticketapp.util.ReservationValidator;
import com.soen345.ticketapp.model.Event;
import com.soen345.ticketapp.model.Reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Booking Integration Tests")
class BookingRepositoryTest {

    @Test
    @DisplayName("Full booking flow: available event + no prior booking = OK")
    void fullBookingFlow_availableEvent_returnsOK() {
        Event event = new Event("Jazz Night", "Montreal", "2026-06-01", "Concert", 10);
        event.setId("evt-1");

        ReservationValidator.Result result =
            ReservationValidator.canReserve(event, new ArrayList<>());

        assertEquals(ReservationValidator.Result.OK, result);
    }

    @Test
    @DisplayName("Full booking flow: after reserving, duplicate booking blocked")
    void fullBookingFlow_afterReserving_duplicateBlocked() {
        Event event = new Event("Jazz Night", "Montreal", "2026-06-01", "Concert", 10);
        event.setId("evt-1");

        Reservation existing = new Reservation();
        existing.setEventId("evt-1");

        ReservationValidator.Result result =
            ReservationValidator.canReserve(event, Collections.singletonList(existing));

        assertEquals(ReservationValidator.Result.ALREADY_BOOKED, result);
    }

    @Test
    @DisplayName("Full booking flow: sold-out event blocks reservation")
    void fullBookingFlow_soldOut_blocksReservation() {
        Event event = new Event("Jazz Night", "Montreal", "2026-06-01", "Concert", 0);
        event.setId("evt-1");

        ReservationValidator.Result result =
            ReservationValidator.canReserve(event, new ArrayList<>());

        assertEquals(ReservationValidator.Result.NO_SEATS, result);
    }

    @Test
    @DisplayName("Full booking flow: cancelled event blocks reservation")
    void fullBookingFlow_cancelledEvent_blocksReservation() {
        Event event = new Event("Jazz Night", "Montreal", "2026-06-01", "Concert", 50);
        event.setId("evt-1");
        event.setCancelled(true);

        ReservationValidator.Result result =
            ReservationValidator.canReserve(event, new ArrayList<>());

        assertEquals(ReservationValidator.Result.EVENT_CANCELLED, result);
    }

    @Test
    @DisplayName("Cancellation flow: reservation with ID can be cancelled")
    void cancellationFlow_validReservation_canBeCancelled() {
        Reservation r = new Reservation();
        r.setId("res-001");
        r.setEventId("evt-1");
        r.setUserId("user-001");

        assertTrue(ReservationValidator.canCancel(r));
    }

    @Test
    @DisplayName("Cancellation flow: reservation without ID cannot be cancelled")
    void cancellationFlow_noId_cannotBeCancelled() {
        Reservation r = new Reservation();
        r.setEventId("evt-1");

        assertFalse(ReservationValidator.canCancel(r));
    }
}