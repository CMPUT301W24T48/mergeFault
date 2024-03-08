package com.example.mergefault;

import android.net.Uri;

import java.util.Calendar;

/**
 * This class defines an Event
 */
public class Event {

    private String eventName;
    private String organizerId;
    private String location;
    private Calendar dateTime;
    private Integer attendeeLimit;
    private Uri eventPoster;
    private String description;
    private Boolean geoLocOn;
    private String eventID;

    /**
     * This is a constructor for the Event class
     * @param eventName
     * This is the String of the event name
     * @param organizerId
     * This is the String of the organizer id
     * @param location
     * this is the String of the event location/address
     * @param dateTime
     * this is the Calendar object containing the date and time of the event
     * @param attendeeLimit
     * this is the Integer of the attendee limit
     * @param eventPoster
     * this is the Uri of the event poster
     * @param description
     * this is the String of the event description
     * @param geoLocOn
     * this is the Boolean stating if the event will track geo location
     * @param eventID
     * this is the String of the generated eventID
     */
    public Event (String eventName, String organizerId, String location, Calendar dateTime, Integer attendeeLimit, Uri eventPoster, String description, Boolean geoLocOn, String eventID){
        this.organizerId = organizerId;
        this.eventName = eventName;
        this.location = location;
        this.dateTime = dateTime;
        this.attendeeLimit = attendeeLimit;
        this.eventPoster = eventPoster;
        this.description = description;
        this.geoLocOn = geoLocOn;
        this.eventID = eventID;
    }
    /**
     * This is a setter for the eventName
     * @param eventName
     * This is the String of the eventName the caller wants to set
     */
    public void setEventName(String eventName)
    {
        this.eventName = eventName;
    }
    /**
     * This is a setter for the organizerId
     * @param organizerId
     * This is the String of the organizerId the caller wants to set
     */
    public void setOrganizerId(String organizerId)
    {
        this.organizerId = organizerId;
    }
    /**
     * This is a setter for the location
     * @param location
     * This is the String of the location the caller wants to set
     */
    public void setLocation(String location) {
        this.location = location;
    }
    /**
     * This is a setter for the dateTime
     * @param dateTime
     * This is the Calendar object of the dateTime the caller wants to set
     */
    public void setDateTime(Calendar dateTime) {
        this.dateTime = dateTime;
    }
    /**
     * This is a setter for the attendeeLimit
     * @param attendeeLimit
     * This is the Integer of the attendeeLimit the caller wants to set
     */
    public void setAttendeeLimit(Integer attendeeLimit) {
        this.attendeeLimit = attendeeLimit;
    }
    /**
     * This is a setter for the eventPoster
     * @param eventPoster
     * This is the Uri of the eventPoster the caller wants to set
     */
    public void setEventPoster(Uri eventPoster) {
        this.eventPoster = eventPoster;
    }
    /**
     * This is a setter for the description
     * @param description
     * This is the String of the description the caller wants to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * This is a setter for the geoLocOn
     * @param geoLocOn
     * This is the Boolean of the geoLocOn the caller wants to set
     */
    public void setGeoLocOn(Boolean geoLocOn) {
        this.geoLocOn = geoLocOn;
    }
    /**
     * This is a setter for the eventId
     * @param eventId
     * This is the String of the eventId the caller wants to set
     */
    public void setEventID(String eventId){
        this.eventID = eventId;
    }
    /**
     *This is a getter for the organizerId
     * @return
     * This returns a String of the organizerId
     */
    public String getOrganizerId(){
        return organizerId;
    }
    /**
     *This is a getter for the eventName
     * @return
     * This returns a String of the eventName
     */
    public String getEventName(){
        return eventName;
    }
    /**
     *This is a getter for the location
     * @return
     * This returns a String of the location
     */
    public String getLocation() {
        return location;
    }
    /**
     *This is a getter for the dateTime
     * @return
     * This returns a Calendar object of the dateTime
     */
    public Calendar getDateTime() {
        return dateTime;
    }
    /**
     *This is a getter for the attendeeLimit
     * @return
     * This returns a Integer of the attendeeLimit
     */
    public Integer getAttendeeLimit() {
        return attendeeLimit;
    }
    /**
     *This is a getter for the eventPoster
     * @return
     * This returns a Uri of the eventPoster
     */
    public Uri getEventPoster() {
        return eventPoster;
    }
    /**
     *This is a getter for the geoLoc
     * @return
     * This returns a Boolean of the geoLoc
     */
    public Boolean getGeoLocOn() {
        return geoLocOn;
    }
    /**
     *This is a getter for the description
     * @return
     * This returns a String of the description
     */
    public String getDescription() {
        return description;
    }
    /**
     *This is a getter for the eventId
     * @return
     * This returns a String of the eventId
     */
    public String getEventID() {
        return eventID;
    }
}
