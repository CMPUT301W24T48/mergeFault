package com.example.mergefault;

/**
 * The Attendee class represents an attendee of an event.
 * It contains information such as name, phone number, email, notification preferences, and geolocation preferences.
 */
public class Attendee {

    private String name;
    private String phoneNum;
    private String emailId;
    private Boolean notificationPref;
    private Boolean geolocationPref;
    private String profImageURL;
    private Integer checkInCount;
    private Boolean checkedIn;
    private String attendeeId;

    /**
     * Constructs an Attendee object for event sub-collection on firebase.
     * @param name              The name of the attendee.
     * @param phoneNum          The phone number of the attendee.
     * @param emailId           The email ID of the attendee.
     * @param notificationPref  The notification preference of the attendee.
     * @param geolocationPref   The geolocation preference of the attendee.
     * @param profImageURL      The URL of the profile image.
     * @param checkInCount      The check-in count of the event.
     * @param checkedIn         The boolean if attendee has checked in.
     * @param attendeeId        The string of the attendee id.
     */
    public Attendee(String name, String phoneNum, String emailId, Boolean notificationPref, Boolean geolocationPref, String profImageURL, Integer checkInCount, Boolean checkedIn, String attendeeId) {
        this.name = name;
        this.phoneNum = phoneNum;
        this.emailId = emailId;
        this.notificationPref = notificationPref;
        this.geolocationPref = geolocationPref;
        this.profImageURL = profImageURL;
        this.checkInCount = checkInCount;
        this.checkedIn = checkedIn;
        this.attendeeId = attendeeId;
    }

    /**
     * Constructs an Attendee object for attendee collection on firebase.
     * @param name              The name of the attendee.
     * @param phoneNum          The phone number of the attendee.
     * @param emailId           The email ID of the attendee.
     * @param notificationPref  The notification preference of the attendee.
     * @param geolocationPref   The geolocation preference of the attendee.
     * @param profImageURL      The URL of the profile image.
     */
    public Attendee(String name, String phoneNum, String emailId, Boolean notificationPref, Boolean geolocationPref, String profImageURL, String attendeeId) {
        this.name = name;
        this.phoneNum = phoneNum;
        this.emailId = emailId;
        this.notificationPref = notificationPref;
        this.geolocationPref = geolocationPref;
        this.profImageURL = profImageURL;
        this.attendeeId = attendeeId;
    }

    /**
     * Retrieves the name of the attendee.
     *
     * @return The name of the attendee.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the phone number of the attendee.
     *
     * @return The phone number of the attendee.
     */
    public String getPhoneNum() {
        return phoneNum;
    }

    /**
     * Retrieves the email ID of the attendee.
     *
     * @return The email ID of the attendee.
     */
    public String getEmailId() {
        return emailId;
    }

    /**
     * Retrieves the notification preference of the attendee.
     *
     * @return The notification preference of the attendee.
     */
    public Boolean getNotificationPref() {
        return notificationPref;
    }

    /**
     * Retrieves the geolocation preference of the attendee.
     *
     * @return The geolocation preference of the attendee.
     */
    public Boolean getGeolocationPref() {
        return geolocationPref;
    }

    /**
     * Retrieves the image URL of the attendee.
     * @return The image URL of the attendee.
     */
    public String getProfImageURL(){
        return profImageURL;
    }

    /**
     * Retrieves the check-in count of the attendee
     * @return The check-in count of the attendee
     */
    public Integer getCheckInCount(){
        return checkInCount;
    }

    /**
     * Retrieves the boolean of if an attendee has checked in
     * @return The boolean if attendee has checked in
     */
    public Boolean getCheckedIn() {
        return checkedIn;
    }

    /**
     * Retrieves the attendee id of the attendee
     * @return The string attendee id of the attendee
     */
    public String getAttendeeId() {
        return attendeeId;
    }

    /**
     * Sets the name of the attendee.
     *
     * @param name The name of the attendee.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the phone number of the attendee.
     *
     * @param phoneNum The phone number of the attendee.
     */
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    /**
     * Sets the email ID of the attendee.
     *
     * @param emailId The email ID of the attendee.
     */
    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    /**
     * Sets the notification preference of the attendee.
     *
     * @param notificationPref The notification preference of the attendee.
     */
    public void setNotificationPref(Boolean notificationPref) {
        this.notificationPref = notificationPref;
    }

    /**
     * Sets the geolocation preference of the attendee.
     *
     * @param geolocationPref The geolocation preference of the attendee.
     */
    public void setGeolocationPref(Boolean geolocationPref) {
        this.geolocationPref = geolocationPref;
    }

    /**
     * Sets the profile image url of the attendee
     * @param profImageURL The profile image url of the attendee
     */
    public void setProfImageURL(String profImageURL){
        this.profImageURL = profImageURL;
    }

    /**
     * Sets the check in count fo the attendee
     * @param checkInCount The check in count fo the attendee
     */
    public void setCheckInCount(Integer checkInCount) {
        this.checkInCount = checkInCount;
    }

    /**
     * Sets the boolean of if the attendee has checked in
     * @param checkedIn The boolean of if the attendee has checked in
     */
    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    /**
     * Sets the attendee id of the attendee
     * @param attendeeId The attendee id of the attendee
     */
    public void setAttendeeId(String attendeeId) {
        this.attendeeId = attendeeId;
    }
}

