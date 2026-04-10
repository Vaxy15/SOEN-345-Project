package com.soen345.ticketapp;

import org.junit.Test;

import static org.junit.Assert.*;

import com.soen345.ticketapp.model.Event;

public class EventTest {

    @Test
    public void defaultConstructor_createsObject() {
        Event event = new Event();
        assertNotNull(event);
    }

    @Test
    public void parameterizedConstructor_setsFieldsCorrectly() {
        long time = 1710000000000L;

        Event event = new Event("Concert", "Montreal", "Music", time, 50);

        assertEquals("Concert", event.getTitle());
        assertEquals("Montreal", event.getLocation());
        assertEquals("Music", event.getCategory());
        assertEquals(time, event.getDateTimeMillis());
        assertEquals(50, event.getAvailableSeats());
        assertFalse(event.getCancelled());
    }

    @Test
    public void setId_getId_worksCorrectly() {
        Event event = new Event();
        event.setId("E123");

        assertEquals("E123", event.getId());
    }

    @Test
    public void setTitle_getTitle_worksCorrectly() {
        Event event = new Event();
        event.setTitle("Movie Night");

        assertEquals("Movie Night", event.getTitle());
    }

    @Test
    public void setLocation_getLocation_worksCorrectly() {
        Event event = new Event();
        event.setLocation("Laval");

        assertEquals("Laval", event.getLocation());
    }

    @Test
    public void getCategory_returnsDefaultGeneral_whenCategoryIsNull() {
        Event event = new Event();
        event.setCategory(null);

        assertEquals("General", event.getCategory());
    }

    @Test
    public void getCategory_returnsActualCategory_whenCategoryIsSet() {
        Event event = new Event();
        event.setCategory("Sports");

        assertEquals("Sports", event.getCategory());
    }

    @Test
    public void setDateTimeMillis_getDateTimeMillis_worksCorrectly() {
        Event event = new Event();
        long time = 1720000000000L;

        event.setDateTimeMillis(time);

        assertEquals(time, event.getDateTimeMillis());
    }

    @Test
    public void setAvailableSeats_getAvailableSeats_worksCorrectly() {
        Event event = new Event();
        event.setAvailableSeats(120);

        assertEquals(120, event.getAvailableSeats());
    }

    @Test
    public void setCancelled_getCancelled_worksCorrectly() {
        Event event = new Event();
        event.setCancelled(true);

        assertTrue(event.getCancelled());
    }
}