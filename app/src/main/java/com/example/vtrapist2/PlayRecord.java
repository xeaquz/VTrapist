package com.example.vtrapist2;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
    String type;
    int videoTime = 0;
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
        USER_ID = intent.getExtras().getString("id");
        type = intent.getExtras().getString("type");

        VIDEO_ID = "PSKEmWhjqVU";
        VIDEO_TIME = 180;

        String sData = readFile();


        // split data to array
        sData = sData.substring(1, sData.lastIndexOf("]"));
        splitData = sData.split(",");
        len = splitData.length;
        samplingRate = (float)len/(float)VIDEO_TIME;
        Log.d("dddddd", Float.toString(samplingRate));

        //getSignalData();
        record.put("signal", signal);

        db.collection("gyro")
                .add(record)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("dddddd", "PlayRecord DocumentSnapshot added with ID: " + documentReference.getId());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("dddddd", "PlayRecord Error adding document", e);
                    }
                });

        TimerTask TT = new TimerTask() {
            @Override
            public void run() {
                videoTime++;
                cnt+=samplingRate;
                Log.d("dddddd", Integer.toString(videoTime));
                Log.d("dddddd", Float.toString(cnt));
                if (videoTime < VIDEO_TIME-5) {
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
                        if (flag == 0) //first start
                            timer.schedule(TT, 0, 1000);
                        else //restart
                            tempTask();
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
        db.collection("session")
                .whereEqualTo("userId", USER_ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                sessionInfo = document.toObject(SessionInfo.class);

                                db.collection("accel").document(sessionInfo.accelId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot document = task.getResult();
                                                signal = document.getData();
                                                Log.d("dddddd", signal.toString());
                                            }
                                        });
                        }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

    public String readFile() {
        StringBuffer data = new StringBuffer();

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            try {
                File f = new File(path, "gyroY.txt");
                BufferedReader buffer = new BufferedReader(new FileReader(f));
                String str = buffer.readLine();
                while (str != null) {
                    data.append(str);
                    str = buffer.readLine();
                }
                buffer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("dddddd", data.toString());
        }
        return data.toString();

    }

    public void tempTask() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                videoTime++;
                cnt+=samplingRate;
                Log.d("dddddd", Integer.toString(videoTime));
                Log.d("dddddd", Float.toString(cnt));
                if (videoTime < VIDEO_TIME-5) {
                    addEntry(Float.parseFloat(splitData[(int) cnt]));
                }
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 1000);
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
        LineDataSet set = new LineDataSet(null, "Example");
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
