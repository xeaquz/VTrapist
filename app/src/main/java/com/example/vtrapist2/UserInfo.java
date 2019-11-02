package com.example.vtrapist2;

public class UserInfo {

    public String userId;
    public String age;
    public String gender;
    public String phobia;
    public String medicalHistory;
    public String levelOfDepression;

    public UserInfo(String userId, String age, String gender, String phobia, String medicalHistory, String levelOfDepression) {
        this.userId = userId;
        this.age = age;
        this.gender = gender;
        this.phobia = phobia;
        this.medicalHistory = medicalHistory;
        this.levelOfDepression = levelOfDepression;
    }
}
