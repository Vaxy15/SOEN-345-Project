package com.soen345.ticketapp.model;

public class Event {
    private String id;
    private String title;
    private String location;
    private String category;
    private long dateTimeMillis;
    private int availableSeats;
    private boolean cancelled;

    public Event() {}

    public Event(String title, String location, String category, long dateTimeMillis, int availableSeats) {
        this.title = title;
        this.location = location;
        this.category = category;
        this.dateTimeMillis = dateTimeMillis;
        this.availableSeats = availableSeats;
        this.cancelled = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category == null ? "General" : category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDateTimeMillis() {
        return dateTimeMillis;
    }

    public void setDateTimeMillis(long dateTimeMillis) {
        this.dateTimeMillis = dateTimeMillis;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
