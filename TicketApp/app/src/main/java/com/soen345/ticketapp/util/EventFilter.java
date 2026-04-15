package com.soen345.ticketapp.util;

import com.soen345.ticketapp.model.Event;
import java.util.ArrayList;
import java.util.List;

public class EventFilter {

    public static List<Event> byLocation(List<Event> events, String location) {
        List<Event> result = new ArrayList<>();
        if (location == null || location.trim().isEmpty()) return new ArrayList<>(events);
        for (Event e : events) {
            if (e.getLocation() != null &&
                    e.getLocation().toLowerCase().contains(location.toLowerCase())) {
                result.add(e);
            }
        }
        return result;
    }

    public static List<Event> byCategory(List<Event> events, String category) {
        List<Event> result = new ArrayList<>();
        if (category == null || category.equals("All")) return new ArrayList<>(events);
        for (Event e : events) {
            if (category.equalsIgnoreCase(e.getCategory())) {
                result.add(e);
            }
        }
        return result;
    }

    public static List<Event> byLocationAndCategory(List<Event> events,
                                                    String location,
                                                    String category) {
        return byCategory(byLocation(events, location), category);
    }

    public static List<Event> availableOnly(List<Event> events) {
        List<Event> result = new ArrayList<>();
        for (Event e : events) {
            if (!e.isCancelled() && e.getAvailableSeats() > 0) result.add(e);
        }
        return result;
    }
}