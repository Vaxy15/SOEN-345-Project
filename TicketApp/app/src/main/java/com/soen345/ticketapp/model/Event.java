package com.soen345.ticketapp.model;

public class Event {
    private String id;
    private String title;
    private String location;
    private long dateTimeMillis;
    private int availableSeats;

    public Event() {} // Firestore needs no-arg constructor

    public Event(String title, String location, long dateTimeMillis, int availableSeats) {
        this.title = title;
        this.location = location;
        this.dateTimeMillis = dateTimeMillis;
        this.availableSeats = availableSeats;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getDateTimeMillis() { return dateTimeMillis; }
    public void setDateTimeMillis(long dateTimeMillis) { this.dateTimeMillis = dateTimeMillis; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}