package com.example.vtrapist2;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class VideoInfo {
    public String videoId;
    public String fps;
    public String playTime;
    public ArrayList<String> keyframeTime;
    public String type;

    public VideoInfo(){}
    public VideoInfo(String videoId, String fps, String playTime, ArrayList<String> keyframeTime, String type) {
        this.videoId = videoId;
        this.fps = fps;
        this.playTime = playTime;
        this.keyframeTime = keyframeTime;
        this.type = type;
    }
}
