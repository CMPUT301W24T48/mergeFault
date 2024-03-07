package com.example.mergefault;

import android.net.Uri;

import java.util.Calendar;

public class Event {

    private String eventName;
    private String organizerId;
    private String location;
    private Calendar dateTime;
    private Integer attendeeLimit;
    private Uri eventPoster;
    private String description;
    private Boolean geoLocOn;

    public Event (String eventName, String organizerId, String location, Calendar dateTime, Integer attendeeLimit, Uri eventPoster, String description, Boolean geoLocOn){
        this.organizerId = organizerId;
        this.eventName = eventName;
        this.location = location;
        this.dateTime = dateTime;
        this.attendeeLimit = attendeeLimit;
        this.eventPoster = eventPoster;
        this.description = description;
        this.geoLocOn = geoLocOn;
    }

    public void setEventName(String eventName)
    {
        this.eventName = eventName;
    }
    public void setOrganizerId(String organizerId)
    {
        this.organizerId = organizerId;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public void setDateTime(Calendar dateTime) {
        this.dateTime = dateTime;
    }

    public void setAttendeeLimit(Integer attendeeLimit) {
        this.attendeeLimit = attendeeLimit;
    }

    public void setEventPoster(Uri eventPoster) {
        this.eventPoster = eventPoster;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGeoLocOn(Boolean geoLocOn) {
        this.geoLocOn = geoLocOn;
    }

    public String getOrganizerId(){
        return organizerId;
    }
    public String getEventName(){
        return eventName;
    }
    public String getLocation() {
        return location;
    }

    public Calendar getDateTime() {
        return dateTime;
    }

    public Integer getAttendeeLimit() {
        return attendeeLimit;
    }

    public Uri getEventPoster() {
        return eventPoster;
    }

    public Boolean getGeoLocOn() {
        return geoLocOn;
    }

    public String getDescription() {
        return description;
    }
}
