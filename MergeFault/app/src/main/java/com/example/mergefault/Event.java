package com.example.mergefault;

import android.net.Uri;

import java.util.Calendar;

public class Event {

    private String eventName;
    private String orgName;
    private String location;
    private Calendar dateTime;
    private Integer attendeeLimit;
    private Uri eventPoster;

    public Event (String eventName, String orgName, String location, Calendar dateTime, Integer attendeeLimit, Uri eventPoster){
        this.orgName = orgName;
        this.eventName = eventName;
        this.location = location;
        this.dateTime = dateTime;
        this.attendeeLimit = attendeeLimit;
        this.eventPoster = eventPoster;
    }

    public void setEventName(String eventName)
    {
        this.eventName = eventName;
    }
    public void setOrgName(String orgName)
    {
        this.orgName = orgName;
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

    public String getOrgName(){
        return orgName;
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
}
