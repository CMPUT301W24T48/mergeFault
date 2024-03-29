package com.example.mergefault;

/**
 * The Attendee class represents an attendee of an event.
 * It contains information such as name, phone number, email, notification preferences, and geolocation preferences.
 */
public class Attendee {

    private String name;
    private Integer phoneNum;
    private String emailId;
    private Boolean notificationPref;
    private Boolean geolocationPref;
    private String profImageURL;
    private Integer checkInCount;

    /**
     * Constructs an Attendee object with the specified information.
     *
     * @param name              The name of the attendee.
     * @param phoneNum          The phone number of the attendee.
     * @param emailId           The email ID of the attendee.
     * @param notificationPref  The notification preference of the attendee.
     * @param geolocationPref   The geolocation preference of the attendee.
     * @param profImageURL      The URL of the profile image.
     */
    public Attendee(String name, Integer phoneNum, String emailId, Boolean notificationPref, Boolean geolocationPref, String profImageURL, Integer checkInCount) {
        this.name = name;
        this.phoneNum = phoneNum;
        this.emailId = emailId;
        this.notificationPref = notificationPref;
        this.geolocationPref = geolocationPref;
        this.profImageURL = profImageURL;
        this.checkInCount = checkInCount;
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
    public Integer getPhoneNum() {
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
     *
     * @return The check-in count of the attendee
     */
    public Integer getCheckInCount(){
        return checkInCount;
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
    public void setPhoneNum(Integer phoneNum) {
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
    public void setProfImageURL(String profImageURL){
        this.profImageURL = profImageURL;
    }
}

