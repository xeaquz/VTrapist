package com.example.vtrapist2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.Manifest.permission.BODY_SENSORS;

public class PlayVideo extends YouTubeBaseActivity {
    YouTubePlayerView youTubeView;
    Button btnStart, btnStop;
    YouTubePlayer.OnInitializedListener listener;
    YouTubePlayer youTubePlayer;
    SensorManager mSensorManager;
    Sensor mHeartRate; // HeartRate 센서
    // 장치를 터치하는 사람의 현재 heart rate를 기록하는 센서
    SensorEventListener heartLs;

    TextView heart;
    Button end;
    LineChart lineChart;

    Thread thread;

    String VIDEO_ID;
    String USER_ID;
    Integer videoTime = 0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<Object, Object> record = new HashMap<>();

    Integer flag  = 0;
    Timer timer = new Timer();
    TimerTask TT = new TimerTask() {
        @Override
        public void run() {
            videoTime++;
        }
    };

    private static final int MY_PERMISSIONS_REQUEST_BODY_SENSORS = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video);

        btnStart = findViewById(R.id.youtubeBtnStart);
        btnStop = findViewById(R.id.youtubeBtnStop);
        youTubeView = findViewById(R.id.youtubeView);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        heartLs = new heartListener();

        heart=(TextView)findViewById(R.id.heart);
        end=(Button)findViewById(R.id.End);
        lineChart=findViewById(R.id.chart);

        Intent intent = getIntent();
        VIDEO_ID = intent.getExtras().getString("videoId");
        USER_ID = intent.getExtras().getString("id");

        checkPermission();

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
                        SensorOnResume();

                        if(flag == 0) //first start
                            timer.schedule(TT, 0, 1000);
                        else //restart
                            tempTask();
                    }
                });
                btnStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        youTubePlayer.pause();
                        SensorOnPause();
                        timer.cancel();
                        flag = 1;
                    }
                });
                end.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date time = new Date();
                        String time1 = fm.format(time);

                        db.collection("users").document(USER_ID)
                                .update(time1, record)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("dddddd", "DocumentSnapshot successfully updated!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("dddddd", "Error updating document", e);
                                    }
                                });
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

    public void tempTask() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                videoTime++;
                //todo
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 1000);
    }

    private void checkPermission() {
        if (checkSelfPermission(BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
// Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
// Explain to the user why we need to write the permission.
                Toast.makeText(this, "BODY_SENSORS", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{BODY_SENSORS},
                    MY_PERMISSIONS_REQUEST_BODY_SENSORS);
// MY_PERMISSION_REQUEST_STORAGE is an
// app-defined int constant
        } else {
// 다음 부분은 항상 허용일 경우에 해당이 됩니다.
            mHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
// getDefaultSensor(int type) - 주어진 타입에 대한 디폴트 센서 얻기
// heart rate monitor 얻어오기
// android.permission.BODY_SENSORS 없다면 detDefaultSensor 에 의해 값 안 얻어짐.
            if(mHeartRate == null) {
                Toast.makeText(this, "No Heart Rate Sensor",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this,"Yes Heart Rate Sensor",Toast.LENGTH_SHORT).show();
            }
            //SensorOnResume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BODY_SENSORS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "BODY_SENSORS Granted", Toast.LENGTH_SHORT).show();
                    mHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
// getDefaultSensor(int type) - 주어진 타입에 대한 디폴트 센서 얻기
// heart rate monitor 얻어오기
// android.permission.BODY_SENSORS 없다면 detDefaultSensor 에 의해 값 안 얻어짐.
                    if(mHeartRate == null) {
                        Toast.makeText(this,"No Heart Rate Sensor",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(this,"Yes Heart Rate Sensor",Toast.LENGTH_SHORT).show();
                    }
                    //SensorOnResume();
// permission was granted, yay! do the
// calendar task you need to do.

                } else {

// permission denied, boo! Disable the
// functionality that depends on this permission.
                }
                break;
        }
    }
    protected void SensorOnResume() {
        //super.onResume(); // 시작
        if(mSensorManager.registerListener(heartLs,mHeartRate,
                SensorManager.SENSOR_DELAY_UI)){
            Toast.makeText(this,"Register Listener",Toast.LENGTH_SHORT).show();
        }
// 지정된 Sampling 주파수에서 특정 sensor에 대해 SensorEventListener 등록
// ~ FASTEST - 최대한 빠르게
// ~ GAME - 게임에 적합한 속도
// ~ UI - UI 수정에 적합한 속도
// ~ NORMAL - 화면 방향 변화를 모니터링 하기에 적합한 속도
        Toast.makeText(this,"Heart Rate Sensor Resume...",Toast.LENGTH_SHORT).show();
    }
    protected void SensorOnPause() {
        //super.onPause(); // 멈추기
        mSensorManager.unregisterListener(heartLs); // 등록한 listener 해제
        Toast.makeText(this,"Heart Rate Sensor Paused...",Toast.LENGTH_SHORT).show();
    }
    private class heartListener implements SensorEventListener {
        // 센서 값이 변할 때 이벤트 발생
// 센서 하나만 이용하므로 values[0]
// 여러 개의 센서 이용시 values[0~n]
        public void onSensorChanged(SensorEvent event){
            float value = event.values[0];
            Log.d("dddddd", Integer.toString(videoTime));

            record.put(Integer.toString(videoTime), value);
            addEntry(value);

            heart.setText(heart.getText().toString() + String.format("%.2f",value));
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
