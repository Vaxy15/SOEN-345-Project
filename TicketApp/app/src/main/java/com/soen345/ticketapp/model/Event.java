package com.soen345.ticketapp.model;

public class Event {
    private String  id;
    private String  title;
    private String  location;
    private String  time;
    private String  category;
    private int     availableSeats;
    private boolean cancelled;
    private long    dateTimeMillis;

    public Event() {}

    public Event(String title, String location, String time,
                 String category, int availableSeats) {
        this.title          = title;
        this.location       = location;
        this.time           = time;
        this.category       = category;
        this.availableSeats = availableSeats;
        this.cancelled      = false;
    }

    public String  getId()                         { return id; }
    public void    setId(String id)                { this.id = id; }

    public String  getTitle()                      { return title; }
    public void    setTitle(String title)          { this.title = title; }

    public String  getLocation()                   { return location; }
    public void    setLocation(String location)    { this.location = location; }

    public String  getTime()                       { return time; }
    public void    setTime(String time)            { this.time = time; }

    public String  getCategory()                   {
        return (category == null || category.isEmpty()) ? "General" : category;
    }
    public void    setCategory(String category)    { this.category = category; }

    public int     getAvailableSeats()                   { return availableSeats; }
    public void    setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public boolean isCancelled()                   { return cancelled; }
    public void    setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public long    getDateTimeMillis()                   { return dateTimeMillis; }
    public void    setDateTimeMillis(long dateTimeMillis){ this.dateTimeMillis = dateTimeMillis; }
}