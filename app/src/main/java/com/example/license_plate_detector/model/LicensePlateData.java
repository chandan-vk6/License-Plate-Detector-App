package com.example.license_plate_detector.model;

import com.google.firebase.Timestamp;

public class LicensePlateData {
    private String licensePlateNumber;
    private String imageBase64;

    private String caseNumber;
    private  String userId;
    private String username;
    private Timestamp timeAdded;

    public LicensePlateData() {
        // Default constructor required for Firestore
    }

    public LicensePlateData(String licensePlateNumber,String imageBase64, String caseNumber, String userId, String username, Timestamp timeAdded) {
        this.licensePlateNumber = licensePlateNumber;
        this.imageBase64 = imageBase64;
        this.caseNumber = caseNumber;
       this.userId = userId;
        this.username = username;
        this.timeAdded = timeAdded;
    }


    public String getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public void setLicensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }
}
