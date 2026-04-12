package com.soen345.ticketapp;

import com.soen345.ticketapp.model.Reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reservation Model Tests")
class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
        reservation.setId("res-001");
        reservation.setUserId("user-001");
        reservation.setEventId("event-001");
        reservation.setEventTitle("Rock Concert");
        reservation.setCreatedAt(1710000000000L);
    }

    @Test
    @DisplayName("Default constructor creates non-null object")
    void defaultConstructor_createsObject() {
        assertNotNull(new Reservation());
    }

    @Test
    @DisplayName("setId and getId work correctly")
    void setId_getId() {
        assertEquals("res-001", reservation.getId());
    }

    @Test
    @DisplayName("setUserId and getUserId work correctly")
    void setUserId_getUserId() {
        assertEquals("user-001", reservation.getUserId());
    }

    @Test
    @DisplayName("setEventId and getEventId work correctly")
    void setEventId_getEventId() {
        assertEquals("event-001", reservation.getEventId());
    }

    @Test
    @DisplayName("setEventTitle and getEventTitle work correctly")
    void setEventTitle_getEventTitle() {
        assertEquals("Rock Concert", reservation.getEventTitle());
    }

    @Test
    @DisplayName("setCreatedAt and getCreatedAt work correctly")
    void setCreatedAt_getCreatedAt() {
        assertEquals(1710000000000L, reservation.getCreatedAt());
    }

    @Test
    @DisplayName("Default constructor produces null fields")
    void defaultConstructor_nullFields() {
        Reservation r = new Reservation();
        assertNull(r.getId());
        assertNull(r.getUserId());
        assertNull(r.getEventId());
    }

    @Test
    @DisplayName("Two reservations with different IDs are not equal by ID")
    void twoReservations_differentIds_areDistinct() {
        Reservation r2 = new Reservation();
        r2.setId("res-002");
        assertNotEquals(reservation.getId(), r2.getId());
    }
}