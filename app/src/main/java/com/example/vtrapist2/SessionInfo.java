package com.example.vtrapist2;

public class SessionInfo {

    public String userId;
    public String videoId;
    public String gyroId;
    public String accelId;
    public String heartId;
    public String phobia;

    public SessionInfo(String userId, String videoId, String gyroId, String accelId, String heartId, String phobia) {
        this.userId = userId;
        this.videoId = videoId;
        this.gyroId = gyroId;
        this.accelId = accelId;
        this.heartId = heartId;
        this.phobia = phobia;
    }
}
