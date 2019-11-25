package com.example.vtrapist2;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

// read signal file
// store data to firebase
// show graph with video
public class PlayRecord extends YouTubeBaseActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> signal = new HashMap<>();
    Map<String, Object> record = new HashMap<>();

    YouTubePlayerView youTubeView;
    Button btnStart, btnStop;
    YouTubePlayer.OnInitializedListener listener;
    YouTubePlayer youTubePlayer;

    TextView heart;
    Button end;
    LineChart lineChart;

    String VIDEO_ID;
    String USER_ID;
    String type;
    Integer videoTime = 0;
    String[] splitData;

    Integer flag = 0;
    Timer timer = new Timer();


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video);

        Intent intent = getIntent();
        VIDEO_ID = intent.getExtras().getString("videoId");
        USER_ID = intent.getExtras().getString("id");
        type = intent.getExtras().getString("type");
/*
        // write file
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(getFilesDir() + "test.txt", true));
            bw.write("[0.096954346, 0.2336731, 0.49803162, 0.5968323, 0.56051636, 0.83929443, 1.3210144, 1.5901794, 1.8230286, 2.124771, 2.1680298, 1.4037933, 0.82380676, 0.5792084, 0.3079071, 0.085739136, -0.03816223, -0.073410034, -0.104385376, -0.1695404, -0.15992737, -0.19036865, -0.0953064, 0.014709473, 0.11457825, 0.03553772, -0.104385376, -0.24377441, -0.39491272, -0.6208191, -1.1906586, -1.6184387, -1.8379364, -2.0221863, -2.398697, -2.7784119, -2.7266083, -1.8048248]\n");
            bw.close();

            Toast.makeText(this,"저장완료", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }*/

        // read file
        String sData = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader(getFilesDir()+"test.txt"));
            String readStr = "";
            String str = null;
            while(((str = br.readLine()) != null)){
                readStr += str +"\n";
            }
            sData = readStr;
            br.close();

            Toast.makeText(this, readStr.substring(0, readStr.length()-1), Toast.LENGTH_SHORT).show();

        }catch (FileNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this, "File not Found", Toast.LENGTH_SHORT).show();
        }catch (IOException e) {
            e.printStackTrace();
        }

        // split data to array
        sData = sData.substring(1, sData.lastIndexOf("]"));
        splitData = sData.split(",");
        int len = splitData.length;

        for (int i = 0; i < len; i++) {
            signal.put(Integer.toString(i), splitData[i]);
        }

        TimerTask TT = new TimerTask() {
            @Override
            public void run() {
                videoTime++;
                addEntry(Float.parseFloat(splitData[videoTime]));
            }
        };

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

    public void tempTask() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                videoTime++;
                addEntry(Float.parseFloat(splitData[videoTime]));
                //todo
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
