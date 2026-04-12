package com.soen345.ticketapp.model;

public class Reservation {
    private String id;
    private String userId;
    private String eventId;
    private String eventTitle;
    private String eventLocation;
    private long   eventDateTimeMillis;
    private long   createdAt;

    public Reservation() {}

    public String getId()                            { return id; }
    public void   setId(String id)                   { this.id = id; }

    public String getUserId()                        { return userId; }
    public void   setUserId(String userId)           { this.userId = userId; }

    public String getEventId()                       { return eventId; }
    public void   setEventId(String eventId)         { this.eventId = eventId; }

    public String getEventTitle()                    { return eventTitle; }
    public void   setEventTitle(String eventTitle)   { this.eventTitle = eventTitle; }

    public String getEventLocation()                         { return eventLocation; }
    public void   setEventLocation(String eventLocation)     { this.eventLocation = eventLocation; }

    public long   getEventDateTimeMillis()                         { return eventDateTimeMillis; }
    public void   setEventDateTimeMillis(long eventDateTimeMillis) { this.eventDateTimeMillis = eventDateTimeMillis; }

    public long   getCreatedAt()                     { return createdAt; }
    public void   setCreatedAt(long createdAt)       { this.createdAt = createdAt; }
}