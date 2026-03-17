package com.soen345.ticketapp.model;

import com.google.firebase.Timestamp;

public class Event {

    private String id;
    private String title;
    private String location;
    private String description;
    private String status;
    private int availableSeats;
    private Timestamp eventDateTime;
    private String createdBy;

    public Event() {
    }

    public Event(String title, String location, String description,
                 Timestamp eventDateTime, int availableSeats,
                 String status, String createdBy) {
        this.title = title;
        this.location = location;
        this.description = description;
        this.eventDateTime = eventDateTime;
        this.availableSeats = availableSeats;
        this.status = status;
        this.createdBy = createdBy;
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

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public Timestamp getEventDateTime() {
        return eventDateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public void setEventDateTime(Timestamp eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}