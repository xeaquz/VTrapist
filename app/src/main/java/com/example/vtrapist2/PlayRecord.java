package com.example.vtrapist2;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

// read signal file
// store data to firebase
// show graph with video
public class PlayRecord extends YouTubeBaseActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> signal = new HashMap<>();
    Map<String, Object> record = new HashMap<>();
    Map<String, Object> data = new HashMap<>();

    SessionInfo sessionInfo;

    YouTubePlayerView youTubeView;
    Button btnStart, btnStop;
    YouTubePlayer.OnInitializedListener listener;
    YouTubePlayer youTubePlayer;

    TextView heart;
    Button end;
    LineChart lineChart;

    String VIDEO_ID;
    String USER_ID;
    int VIDEO_TIME;
    String HEART_ID;
    int videoTime = 0;
    float samplingRate_a;
    int timePlayed;

    Object signalData;
    String[] splitData;
    int signalCnt = 0;

    int flag = 0;
    Timer timer = new Timer();
    float cnt = 0;
    int len;
    float samplingRate;


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video);

        Intent intent = getIntent();
        VIDEO_ID = intent.getExtras().getString("videoId");
        USER_ID = intent.getExtras().getString("userId");
        HEART_ID = intent.getExtras().getString("heartId");
        samplingRate = intent.getExtras().getFloat("samplingRate_a");
        timePlayed = intent.getExtras().getInt("timePlayed");

        DocumentReference docRef = db.collection("heart").document(HEART_ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        data = document.getData();
                        signalData = data.get("signal");

                        String sData = signalData.toString();

                        // split data to array
                        sData = sData.substring(1, sData.lastIndexOf("]"));
                        splitData = sData.split(",");
                        len = splitData.length;
                        Log.d("dddddd", Float.toString(len));
                        Log.d("dddddd", Float.toString(samplingRate));

                        Log.d("dddddd", signalData.toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });



        TimerTask TT = new TimerTask() {
            @Override
            public void run() {
                videoTime++;
                cnt += samplingRate/10;
                if (videoTime/10 < timePlayed) {
                    addEntry(Float.parseFloat(splitData[(int) cnt]));
                }
            }
        };


        btnStart = findViewById(R.id.youtubeBtnStart);
        btnStop = findViewById(R.id.youtubeBtnStop);
        youTubeView = findViewById(R.id.youtubeView);

        heart=(TextView)findViewById(R.id.heart);
        end=(Button)findViewById(R.id.End);
        lineChart=findViewById(R.id.chart);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        LineData data = new LineData();
        lineChart.setData(data);
        listener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.cueVideo(VIDEO_ID);

                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        youTubePlayer.play();
                        VIDEO_TIME = youTubePlayer.getDurationMillis();
                        if (flag == 0) //first start
                            timer.schedule(TT, 0, 100);
                        else { //restart
                            tempTask();
                        }
                    }
                });
                btnStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        youTubePlayer.pause();
                        timer.cancel();
                        flag = 1;
                    }
                });
                end.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        btnStart.setEnabled(false);
                        btnStop.setEnabled(false);
                        end.setEnabled(false);
                    }
                });
            }
            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.d("dddddd", "fail to initialize youtube player");
            }
        };
        youTubeView.initialize("AIzaSyBY9yA9muDZwvNjX2_KEHYxzVR7DPDgUXI", listener);

    }

    public void getSignalData() {
        DocumentReference docRef = db.collection("heart").document(HEART_ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        data = document.getData();
                        signalData = data.get("signal");
                        Log.d("dddddd", signalData.toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }


    public void tempTask() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                videoTime++;
                cnt+=samplingRate/10;
                Log.d("dddddd", Integer.toString(videoTime));
                Log.d("dddddd", Float.toString(cnt));
                if (videoTime < timePlayed) {
                    addEntry(Float.parseFloat(splitData[(int) cnt]));
                }
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 100);
    }


    private void addEntry(Float dataValue) {
        LineData data = lineChart.getData();
        if(data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if(set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), dataValue), 0);
            data.notifyDataChanged();

            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(10);
            lineChart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Heart");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawValues(false);
        return set;
    }



}
