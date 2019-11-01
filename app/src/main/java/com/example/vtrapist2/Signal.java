package com.example.vtrapist2;


import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.BODY_SENSORS;

public class Signal extends AppCompatActivity {
    SensorManager mSensorManager;
    Sensor mHeartRate; // HeartRate 센서
    // 장치를 터치하는 사람의 현재 heart rate를 기록하는 센서
    SensorEventListener heartLs;
    TextView heart;
    TextView acc;
    Button resume;
    Button pause;

    ArrayList<Float> heartRate = new ArrayList<>();

    private static final int MY_PERMISSIONS_REQUEST_BODY_SENSORS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signal);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
// System Service 생성 (눈에는 안 보이는 서비스)
        heartLs = new heartListener();
        acc = (TextView)findViewById(R.id.accuracy);
        heart=(TextView)findViewById(R.id.heart);
        resume=(Button)findViewById(R.id.Resume);
        pause=(Button)findViewById(R.id.Pause);
// Resume 버튼 눌렸을 때 Sensor 서비스 시작
        resume.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                SensorOnResume();
            }
        });
// Pause 버튼 눌렸을 때 Pause 서비스 시작
        pause.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                SensorOnPause();
            }

        });
        checkPermission();
    } // onCreate()
    /**
     * Permission check.
     */
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
            SensorOnResume();
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
                    SensorOnResume();
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
        super.onResume(); // 시작
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
        super.onPause(); // 멈추기
        mSensorManager.unregisterListener(heartLs); // 등록한 listener 해제
        Toast.makeText(this,"Heart Rate Sensor Paused...",Toast.LENGTH_SHORT).show();
    }
    private class heartListener implements SensorEventListener {
        // 센서 값이 변할 때 이벤트 발생
// 센서 하나만 이용하므로 values[0]
// 여러 개의 센서 이용시 values[0~n]
        public void onSensorChanged(SensorEvent event){
            float value = event.values[0];
            float accuracy = event.accuracy;
            heartRate.add(value);
            heart.setText(heart.getText().toString() + String.format("%.2f",value));
            acc.setText(acc.getText().toString() + String.format("%.2f",accuracy));
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
