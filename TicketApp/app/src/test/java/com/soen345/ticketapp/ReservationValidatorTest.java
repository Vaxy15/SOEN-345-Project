package com.soen345.ticketapp;

import com.soen345.ticketapp.model.Event;
import com.soen345.ticketapp.model.Reservation;
import com.soen345.ticketapp.util.ReservationValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reservation Validator Tests")
class ReservationValidatorTest {

    private Event event;
    private List<Reservation> noReservations;

    @BeforeEach
    void setUp() {
        event = new Event("Jazz Night", "Montreal", "2026-06-01 20:00", "Concert", 50);
        event.setId("event-001");
        noReservations = new ArrayList<>();
    }

    @Test
    @DisplayName("Can reserve when seats available and not already booked")
    void canReserve_seatsAvailable_notBooked_returnsOK() {
        assertEquals(ReservationValidator.Result.OK,
                ReservationValidator.canReserve(event, noReservations));
    }

    @Test
    @DisplayName("Cannot reserve when event is sold out")
    void canReserve_noSeats_returnsNoSeats() {
        event.setAvailableSeats(0);
        assertEquals(ReservationValidator.Result.NO_SEATS,
                ReservationValidator.canReserve(event, noReservations));
    }

    @Test
    @DisplayName("Cannot reserve when seats is negative")
    void canReserve_negativeSeats_returnsNoSeats() {
        event.setAvailableSeats(-1);
        assertEquals(ReservationValidator.Result.NO_SEATS,
                ReservationValidator.canReserve(event, noReservations));
    }

    @Test
    @DisplayName("Cannot reserve the same event twice")
    void canReserve_alreadyBooked_returnsAlreadyBooked() {
        Reservation existing = new Reservation();
        existing.setUserId("user-001");
        existing.setEventId("event-001");
        existing.setEventTitle("Jazz Night");
        assertEquals(ReservationValidator.Result.ALREADY_BOOKED,
                ReservationValidator.canReserve(event, Collections.singletonList(existing)));
    }

    @Test
    @DisplayName("Can reserve a different event even if another is booked")
    void canReserve_differentEventBooked_returnsOK() {
        Reservation other = new Reservation();
        other.setUserId("user-001");
        other.setEventId("event-999");
        other.setEventTitle("Other Show");
        assertEquals(ReservationValidator.Result.OK,
                ReservationValidator.canReserve(event, Collections.singletonList(other)));
    }

    @Test
    @DisplayName("Cannot reserve a cancelled event")
    void canReserve_cancelledEvent_returnsEventCancelled() {
        event.setCancelled(true);
        assertEquals(ReservationValidator.Result.EVENT_CANCELLED,
                ReservationValidator.canReserve(event, noReservations));
    }

    @Test
    @DisplayName("Cancelled check takes priority over no-seats check")
    void canReserve_cancelledAndNoSeats_returnsCancelled() {
        event.setCancelled(true);
        event.setAvailableSeats(0);
        assertEquals(ReservationValidator.Result.EVENT_CANCELLED,
                ReservationValidator.canReserve(event, noReservations));
    }

    @Test
    @DisplayName("Can cancel a reservation that has an ID")
    void canCancel_withId_returnsTrue() {
        Reservation r = new Reservation();
        r.setId("res-001");
        r.setUserId("user-001");
        r.setEventId("event-001");
        assertTrue(ReservationValidator.canCancel(r));
    }

    @Test
    @DisplayName("Cannot cancel a reservation with no ID")
    void canCancel_withoutId_returnsFalse() {
        Reservation r = new Reservation();
        r.setUserId("user-001");
        r.setEventId("event-001");
        assertFalse(ReservationValidator.canCancel(r));
    }

    @Test
    @DisplayName("Cannot cancel a null reservation")
    void canCancel_null_returnsFalse() {
        assertFalse(ReservationValidator.canCancel(null));
    }
}