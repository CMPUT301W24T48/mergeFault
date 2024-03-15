package com.example.mergefault;

public class Attendee {
    private String name;
    private Integer phoneNum;
    private String emailId;
    private Boolean notificationPref;
    private Boolean geolocationPref;

    public Attendee (String name, Integer phoneNum, String emailId, Boolean notificationPref, Boolean geolocationPref) {
        this.name = name;
        this.phoneNum = phoneNum;
        this.emailId = emailId;
        this.geolocationPref = geolocationPref;
        this.notificationPref = notificationPref;
    }

    public String getName() {
        return name;
    }

    public Integer getPhoneNum() {
        return phoneNum;
    }

    public String getEmailId() {
        return emailId;
    }

    public Boolean getNotificationPref() {
        return notificationPref;
    }

    public Boolean getGeolocationPref() {
        return geolocationPref;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNum(Integer phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
    public void setNotificationPref(Boolean notificationPref) {
        this.notificationPref = notificationPref;
    }

    public void setGeolocationPref(Boolean geolocationPref) {
        this.geolocationPref = geolocationPref;
    }

}
