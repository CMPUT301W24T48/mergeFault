package com.example.mergefault;

import android.net.Uri;

import java.util.Calendar;

public class Event {
    private String location;
    private Calendar dateTime;
    private Integer attendeeLimit;
    private Uri eventPoster;

    public Event (String location, Calendar dateTime, Integer attendeeLimit, Uri eventPoster){
        this.location = location;
        this.dateTime = dateTime;
        this.attendeeLimit = attendeeLimit;
        this.eventPoster = eventPoster;
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
}
