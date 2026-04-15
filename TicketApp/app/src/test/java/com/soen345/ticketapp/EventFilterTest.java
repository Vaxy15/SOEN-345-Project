package com.soen345.ticketapp;

import com.soen345.ticketapp.model.Event;
import com.soen345.ticketapp.util.EventFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Event Filter Tests")
class EventFilterTest {

    private List<Event> events;

    @BeforeEach
    void setUp() {
        Event e1 = new Event("Jazz Night",    "Montreal, Metropolis", "2026-06-01 20:00", "Concert", 80);
        Event e2 = new Event("Playoffs Game", "Montreal, Bell Centre","2026-06-05 19:00", "Sports",  200);
        Event e3 = new Event("Indie Film",    "Toronto, TIFF",        "2026-06-10 18:00", "Movie",   50);
        Event e4 = new Event("Tech Summit",   "Toronto, Metro Conv",  "2026-06-15 09:00", "Other",   300);
        Event e5 = new Event("Sold Out Show", "Montreal, MTelus",     "2026-06-20 21:00", "Concert", 0);

        e1.setId("e1"); e2.setId("e2"); e3.setId("e3");
        e4.setId("e4"); e5.setId("e5");

        Event e6 = new Event("Cancelled Gig", "Quebec City, Capitol", "2026-06-25 20:00", "Concert", 100);
        e6.setId("e6");
        e6.setCancelled(true);

        events = Arrays.asList(e1, e2, e3, e4, e5, e6);
    }

    // ── byLocation ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Filter by location returns only matching events")
    void byLocation_returnsMatchingEvents() {
        List<Event> result = EventFilter.byLocation(events, "Montreal");
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Filter by location is case-insensitive")
    void byLocation_isCaseInsensitive() {
        List<Event> upper = EventFilter.byLocation(events, "TORONTO");
        List<Event> lower = EventFilter.byLocation(events, "toronto");
        assertEquals(upper.size(), lower.size());
    }

    @Test
    @DisplayName("Filter by unknown location returns empty list")
    void byLocation_unknownLocation_returnsEmpty() {
        List<Event> result = EventFilter.byLocation(events, "Vancouver");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Filter by empty location returns all events")
    void byLocation_emptyString_returnsAll() {
        List<Event> result = EventFilter.byLocation(events, "");
        assertEquals(events.size(), result.size());
    }

    @Test
    @DisplayName("Filter by null location returns all events")
    void byLocation_null_returnsAll() {
        List<Event> result = EventFilter.byLocation(events, null);
        assertEquals(events.size(), result.size());
    }

    // ── byCategory ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Filter by Concert returns only concert events")
    void byCategory_concert_returnsCorrectCount() {
        List<Event> result = EventFilter.byCategory(events, "Concert");
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Filter by Sports returns only sports events")
    void byCategory_sports_returnsCorrectCount() {
        List<Event> result = EventFilter.byCategory(events, "Sports");
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Filter by All returns every event")
    void byCategory_all_returnsAll() {
        List<Event> result = EventFilter.byCategory(events, "All");
        assertEquals(events.size(), result.size());
    }

    @Test
    @DisplayName("Filter by null category returns all events")
    void byCategory_null_returnsAll() {
        List<Event> result = EventFilter.byCategory(events, null);
        assertEquals(events.size(), result.size());
    }

    // ── byLocationAndCategory ─────────────────────────────────────────────────

    @Test
    @DisplayName("Combined filter returns correct events")
    void byLocationAndCategory_returnsCorrectEvents() {
        List<Event> result = EventFilter.byLocationAndCategory(events, "Montreal", "Concert");
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Combined filter with no match returns empty list")
    void byLocationAndCategory_noMatch_returnsEmpty() {
        List<Event> result = EventFilter.byLocationAndCategory(events, "Toronto", "Sports");
        assertTrue(result.isEmpty());
    }

    // ── availableOnly ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("availableOnly excludes sold-out events")
    void availableOnly_excludesSoldOut() {
        List<Event> result = EventFilter.availableOnly(events);
        result.forEach(e -> assertTrue(e.getAvailableSeats() > 0));
    }

    @Test
    @DisplayName("availableOnly excludes cancelled events")
    void availableOnly_excludesCancelled() {
        List<Event> result = EventFilter.availableOnly(events);
        result.forEach(e -> assertFalse(e.isCancelled()));
    }

    @Test
    @DisplayName("availableOnly returns correct count")
    void availableOnly_returnsCorrectCount() {
        // e1, e2, e3, e4 are available (e5 sold out, e6 cancelled)
        List<Event> result = EventFilter.availableOnly(events);
        assertEquals(4, result.size());
    }
}