package com.soen345.ticketapp;

import org.junit.Test;

import static org.junit.Assert.*;

import com.soen345.ticketapp.model.Reservation;

public class ReservationTest {

    @Test
    public void defaultConstructor_createsObject() {
        Reservation reservation = new Reservation();
        assertNotNull(reservation);
    }

    @Test
    public void setIdTest() {
        Reservation reservation = new Reservation();
        reservation.setId("R1");

        assertEquals("R1", reservation.getId());
    }

    @Test
    public void setEventIdTest() {
        Reservation reservation = new Reservation();
        reservation.setEventId("E1");

        assertEquals("E1", reservation.getEventId());
    }

    @Test
    public void setUserIdTest() {
        Reservation reservation = new Reservation();
        reservation.setUserId("U1");

        assertEquals("U1", reservation.getUserId());
    }

    @Test
    public void setEventTitleTest() {
        Reservation reservation = new Reservation();
        reservation.setEventTitle("Jazz Festival");

        assertEquals("Jazz Festival", reservation.getEventTitle());
    }

    @Test
    public void setCreatedAtTest() {
        Reservation reservation = new Reservation();
        long createdAt = 1711111111111L;

        reservation.setCreatedAt(createdAt);

        assertEquals(createdAt, reservation.getCreatedAt());
    }
}