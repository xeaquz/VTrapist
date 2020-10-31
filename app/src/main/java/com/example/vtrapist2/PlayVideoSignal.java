package com.example.vtrapist2;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.BODY_SENSORS;
import static android.content.ContentValues.TAG;

public class PlayVideoSignal extends YouTubeBaseActivity {
    private static final int RECOVERY_REQUEST = 1;
    public static final String API_KEY = "AIzaSyBY9yA9muDZwvNjX2_KEHYxzVR7DPDgUXI";

    // * 블루투스 관련 * //
    private Button btnConnectW, btnConnectP;

    private Dialog dialog; // 블루투스 창

    private ArrayAdapter<String> chatAdapter;
    private ArrayList<String> chatMessages;
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private ArrayAdapter<String> discoveredDevicesAdapter;
    private ChatController chatController;
    private BluetoothDevice connectingDevice;

    private YouTubePlayerView youTubeView;
    private Button btnStart, btnStop;
    private YouTubePlayer.OnInitializedListener listener;
    private YouTubePlayer youTubePlayer;

    private SensorManager mSensorManager;
    private Sensor mHeartRate; // HeartRate 센서
    // 장치를 터치하는 사람의 현재 heart rate를 기록하는 센서
    private SensorEventListener heartLs;

    private Button btnEnd;
    private LineChart lineChart;

    private String VIDEO_ID;
    private String USER_ID;
    private String type;
    private String timeStarted;
    private int timePlayed;
    private float samplingRate_a;
    private int duration;

    private Object timestamp = new ArrayList<>();
    private Map<String, Object> tempData = new HashMap<>();
    private String[] splitData;
    private ArrayList<Integer> stamp = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Map<Object, Object> record = new HashMap<>();
    private Map<Object, Object> session = new HashMap<>();
    private ArrayList<Float> signal = new ArrayList<>();
    private float curSignal;
    private String sessionId;
    private String heartId;

    private Integer flag = 0, flag_pw = 0;
    private Timer timer = new Timer();
    TimerTask TT = new TimerTask() {
        @Override
        public void run() {
            addEntry(curSignal);
            signal.add(curSignal);

        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video_signal);


        findViewsByIds();
        connectBLE();

        // get intent
        Intent intent = getIntent();
        VIDEO_ID = intent.getExtras().getString("videoId");
        USER_ID = intent.getExtras().getString("id");
        type = intent.getExtras().getString("type");

        getTimestamp();

        Log.d("dddddd1", VIDEO_ID);
        // set sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // get current time
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time = new Date();
        timeStarted = fm.format(time);



        // graph
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(true);

        LineData data = new LineData();
        lineChart.setData(data);

        LineDataSet data2;

        // youtube
        listener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.cueVideo(VIDEO_ID);

                btnConnectW.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view){
                        flag_pw = 1;
                        initBLE();
                    }
                });
                btnConnectP.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view){
                        flag_pw = 0;
                        initBLE();
                    }
                });


                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage("start");
                        youTubePlayer.play();
                        duration = (int)youTubePlayer.getDurationMillis()/1000;
                        Log.d("dddddd", Integer.toString(duration));

                        if (flag == 0) //first start
                            timer.schedule(TT, 0, 100);
                        else //restart
                            tempTask();
                    }
                });
                btnStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage("stop");

                        youTubePlayer.pause();
                        timer.cancel();
                        flag = 1;
                    }
                });
                btnEnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        sendMessage("stop");

                        btnStart.setEnabled(false);
                        btnStop.setEnabled(false);
                        btnEnd.setEnabled(false);
                        timer.cancel();

                        timePlayed = youTubePlayer.getCurrentTimeMillis()/1000;
                        samplingRate_a = signal.size()/timePlayed;
                        int tmp = signal.size();
                        Log.d("dddddd", Integer.toString(tmp) + " " + timePlayed);

                        // put signal
                        record.put("signal", signal);
                        record.put("sessionId", "");
                        db.collection("heart")
                                .add(record)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d("dddddd", "PlayVideo DocumentSnapshot added with ID: " + documentReference.getId() + timePlayed);
                                        heartId = documentReference.getId();

                                        // put session
                                        session.put("userId", USER_ID);
                                        session.put("videoId", VIDEO_ID);
                                        session.put("type", type);
                                        session.put("timeStarted", timeStarted);
                                        session.put("heartId", heartId);
                                        session.put("timePlayed", timePlayed);
                                        session.put("samplingRate_a", samplingRate_a);
                                        db.collection("session")
                                                .add(session)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d("dddddd", "PlayVideo DocumentSnapshot added with ID: " + documentReference.getId());
                                                        sessionId = documentReference.getId();

                                                        // update session id of signal collection
                                                        db.collection("heart").document(heartId)
                                                                .update("sessionId", sessionId)
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
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("dddddd", "Error adding document", e);
                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("dddddd", "PlayVideo Error adding document", e);
                                    }
                                });
                        finish();

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


    public void getTimestamp() {
        DocumentReference docRef = db.collection("videos").document(VIDEO_ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        tempData = document.getData();
                        timestamp = tempData.get("timestamp");

                        String sData = timestamp.toString();

                        // split data to array
                        sData = sData.substring(1, sData.lastIndexOf("]"));
                        splitData = sData.split(",");
                        for(int i = 0; i < splitData.length; i++) {
                            stamp.add((int)Float.parseFloat(splitData[i])/100);
                        }
                        Log.d("dddddd", timestamp.toString());

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void connectBLE() {
        // support BLE : 기기에서 블루투스를 지원하는지 확인
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish(); // 지원안되면 블루투스 꺼버리기
        }

        // Active BLE : 장치가 BLE를 사용하는지 확인 <-> 사용하지 않을 경우, 켜도록 요청
        if (!bluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        // 블루투스 연결 창 보여주기

        //chat adapter 설정
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        //listView.setAdapter(chatAdapter);
    }

    // * 블루투스 관련 * //

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            setStatus("Connected to: " + connectingDevice.getName());
                            sendMessage(VIDEO_ID);
                            btnConnectW.setEnabled(true);
                            break;
                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting...");
                            btnConnectW.setEnabled(false);
                            break;
                        case ChatController.STATE_LISTEN:
                            setStatus("Listen...");
                            break;
                        case ChatController.STATE_NONE:
                            setStatus("Not connected");
                            break;

                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    chatMessages.add("Me: " + writeMessage);
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    chatMessages.add(connectingDevice.getName() + ":  " + readMessage);
//                    chatAdapter.notifyDataSetChanged();

                    String sigType, tempSig;
                    sigType = readMessage.substring(0, 1);
                    tempSig = readMessage.substring(1);

                    BigDecimal tempSig2 = new BigDecimal(tempSig);
                    curSignal = tempSig2.floatValue();

                    Toast.makeText(getApplicationContext(), curSignal+" ", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
//                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
//                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    /*
     * 블루투스 초기화
     * */
    private void initBLE(){ // == showPrinterPickDialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Bluetooth Devices");

        // Discovery BLE :
        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        // 블루투스 어댑터 초기화
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // listview를 dialog에 위치시키기 & 어댑터 붙이기
        ListView listView = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        // 디바이스가 찾아졌을때, 브로드캐스트를 위해 등록
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // discovery가 완료되었을때, 브로드캐스트를 위해 등록
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices(); // 페어링된 장치 목록 가져오기

        // 페어링된 장치가 있다면, 그것들을 어레이 어댑터에 추가하기
        if (pairedDevices.size() > 0 ){
            for (BluetoothDevice device : pairedDevices){
                pairedDevicesAdapter.add(device.getName() + "\n"+ device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add("No devices have been paired");
        }

        // Listview에 있는 아이템 클릭시 이벤트 핸들링 (Paring)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery(); // 아이템 클릭시 Discovery 그만하기
                String info = ((TextView) view).getText().toString(); // 해당 기기의 이름과
                String address = info.substring(info.length() - 17); // 주소를 얻어오기

                if(flag_pw == 0)
                    connectToDevice(address);
                dialog.dismiss(); // Dialog 사라지게
            }

        });

        // Listview2에 있는 아이템 클릭시 이벤트 핸들링 (Discovering)
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery(); // 아이템 클릭시 Discovery 그만하기
                String info = ((TextView) view).getText().toString(); // 해당 기기의 이름과
                String address = info.substring(info.length() - 17); // 주소를 얻어오기

                connectToDevice(address);
                dialog.dismiss(); // Dialog 사라지게
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();

    }

    private void setStatus(String s) {
    }

    private void connectToDevice(String deviceAddress){
        bluetoothAdapter.cancelDiscovery(); // Discovery 그만 하도록 하기
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device);
    }

    private void findViewsByIds(){

        btnConnectW = findViewById(R.id.btnConnectW);
        btnConnectP = findViewById(R.id.btnConnectP);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        youTubeView = findViewById(R.id.youtubeView);

        btnEnd = (Button) findViewById(R.id.btnEnd);
        lineChart = findViewById(R.id.chart);


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    if(flag_pw == 0) chatController = new ChatController(this, handler);
                } else {
                    Toast.makeText(this, "Bluetooth still disabled, turn off application!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            chatController = new ChatController(this, handler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatController != null)
            chatController.stop();
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
            }
        }
    };

    private void sendMessage(String message) {

        if (message.length() > 0) {
            Log.d("dddddd", message);
            byte[] send = message.getBytes();
            chatController.write(send);
        }
    }

    // 스마트폰 내에 센서 데이터를 txt 파일로 저장
    public void saveData(){

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            try {
                // * Save Heart Rate Data * //
                // Save Heart Rate Data
                File f = new File(path, "heart.txt");
                FileWriter fw = new FileWriter(f, false);
                PrintWriter out = new PrintWriter(fw);
                out.println(signal);
                out.close();
            } catch(IOException e) {
                e.printStackTrace();
            }

        }
        else {
            Toast.makeText(getApplicationContext(), "외부 메모리 읽기 쓰기 불가능",Toast.LENGTH_SHORT).show();
        }
    }

    //chart----------------------------------------------------------------------------------------
    // add value to chart
    private void addEntry(Float dataValue) {
        LineData data = lineChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
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
        LineDataSet set = new LineDataSet(null, "heart");
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

    // using when restart timer
    public void tempTask() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                addEntry(curSignal);
                signal.add(curSignal);
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 100);
    }

}