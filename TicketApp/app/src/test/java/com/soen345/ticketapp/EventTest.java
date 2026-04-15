package com.soen345.ticketapp;

import com.soen345.ticketapp.model.Event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Event Model Tests")
class EventTest {

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event("Rock Concert", "Montreal", "2026-03-10 20:00", "Concert", 100);
        event.setId("event-001");
    }

    @Test
    @DisplayName("Default constructor creates non-null object")
    void defaultConstructor_createsObject() {
        assertNotNull(new Event());
    }

    @Test
    @DisplayName("Parameterized constructor sets title correctly")
    void constructor_setsTitle() {
        assertEquals("Rock Concert", event.getTitle());
    }

    @Test
    @DisplayName("Parameterized constructor sets location correctly")
    void constructor_setsLocation() {
        assertEquals("Montreal", event.getLocation());
    }

    @Test
    @DisplayName("Parameterized constructor sets category correctly")
    void constructor_setsCategory() {
        assertEquals("Concert", event.getCategory());
    }

    @Test
    @DisplayName("Parameterized constructor sets time correctly")
    void constructor_setsTime() {
        assertEquals("2026-03-10 20:00", event.getTime());
    }

    @Test
    @DisplayName("Parameterized constructor sets availableSeats correctly")
    void constructor_setsAvailableSeats() {
        assertEquals(100, event.getAvailableSeats());
    }

    @Test
    @DisplayName("New event is not cancelled by default")
    void constructor_isNotCancelledByDefault() {
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("getCategory returns General when category is null")
    void getCategory_returnsGeneral_whenNull() {
        Event e = new Event();
        e.setCategory(null);
        assertEquals("General", e.getCategory());
    }

    @Test
    @DisplayName("setId and getId work correctly")
    void setId_getId() {
        event.setId("E999");
        assertEquals("E999", event.getId());
    }

    @Test
    @DisplayName("setTitle and getTitle work correctly")
    void setTitle_getTitle() {
        event.setTitle("Jazz Night");
        assertEquals("Jazz Night", event.getTitle());
    }

    @Test
    @DisplayName("setLocation and getLocation work correctly")
    void setLocation_getLocation() {
        event.setLocation("Toronto");
        assertEquals("Toronto", event.getLocation());
    }

    @Test
    @DisplayName("setTime and getTime work correctly")
    void setTime_getTime() {
        event.setTime("2026-08-01 18:30");
        assertEquals("2026-08-01 18:30", event.getTime());
    }

    @Test
    @DisplayName("setAvailableSeats and getAvailableSeats work correctly")
    void setAvailableSeats_getAvailableSeats() {
        event.setAvailableSeats(200);
        assertEquals(200, event.getAvailableSeats());
    }

    @Test
    @DisplayName("setCancelled to true sets cancelled correctly")
    void setCancelled_true() {
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("Cancelled event can be restored")
    void setCancelled_canBeRestored() {
        event.setCancelled(true);
        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }
}