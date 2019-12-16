package com.example.vtrapist2;

import java.util.HashMap;
import java.util.Map;

public class SessionInfo {

    public String userId;
    public String videoId;
    public int timePlayed;
    public String timeStarted;
    public String accelId;
    public String heartId;
    public String gyroId;
    public String type;
    public float samplingRate_a;

    public SessionInfo(){}
    public SessionInfo(String userId, String videoId, String timeStarted, int timePlayed, String heartId, String type, float samplingRate_a) {
        this.userId = userId;
        this.videoId = videoId;
        this.timeStarted = timeStarted;
        this.timePlayed = timePlayed;
        this.heartId = heartId;
        this.type = type;
        this.samplingRate_a = samplingRate_a;
    }

}
