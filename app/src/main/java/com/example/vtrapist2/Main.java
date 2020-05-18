package com.example.vtrapist2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class Main extends AppCompatActivity {

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

    private String uid;
    private String name;
    private String date;
    private String newDate;

    private SessionInfo sessionInfo;
    private SessionInfo newSessionInfo;

    private TextView txtView_hello;
    private TextView txtView_session;
    private Button btnStart;
    private Button btnHistory;

    private ChatController chatController;
    private BluetoothDevice connectingDevice;

    Map<String, Object> data = new HashMap<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        connectBLE();

        txtView_hello = findViewById(R.id.txtView_hello);
        txtView_session = findViewById(R.id.txtView_session);
        btnStart = findViewById(R.id.btnStart);
        btnHistory = findViewById(R.id.btnHistory);

        // set action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#49A5F6")));

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        Log.d("dddddd", "DocumentSnapshot added with ID: " + uid);

        // get user name and set TextView
        DocumentReference docRef = db.collection("user").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        data = document.getData();
                        name = data.get("name").toString();
                        txtView_hello.setText(name + "님 어서오세요!");

                    } else {
                        Log.d(TAG, "No such document");
                        txtView_hello.setText("데이터 불러오기 실패");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        // get session data and set textView
        db.collection("session")
                .whereEqualTo("userId", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                newSessionInfo = document.toObject(SessionInfo.class);
                                if (sessionInfo != null) {
                                    // compare date to show latest record
                                    compareDate();
                                }
                                else {
                                    sessionInfo = newSessionInfo;
                                }

                                txtView_session.setText("마지막 세션: " + sessionInfo.timeStarted);
                                txtView_session.append("\n\n비디오 공포증 종류: " + sessionInfo.type);
                                txtView_session.append("\n\n플레이 시간: " + sessionInfo.timePlayed/60 + "분" + sessionInfo.timePlayed%60 + "초");

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), VideoList.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RecordList.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
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
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_LONG).show();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    chatController = new ChatController(this, handler);
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
            byte[] send = message.getBytes();
            chatController.write(send);
//            status.append(send.toString());
        }
    }

    // compare date data to show latest history
    public void compareDate() {

        int yy1, mm1, dd1, yy2, mm2, dd2;
        int h1, m1, h2, m2;

        date = sessionInfo.timeStarted;
        newDate = newSessionInfo.timeStarted;

        yy1 = Integer.parseInt(date.substring(0, 4));
        mm1 = Integer.parseInt(date.substring(5, 7));
        dd1 = Integer.parseInt(date.substring(8, 10));
        h1 = Integer.parseInt(date.substring(11, 13));
        m1 = Integer.parseInt(date.substring(14, 16));

        yy2 = Integer.parseInt(newDate.substring(0, 4));
        mm2 = Integer.parseInt(newDate.substring(5, 7));
        dd2 = Integer.parseInt(newDate.substring(8, 10));
        h2 = Integer.parseInt(newDate.substring(11, 13));
        m2 = Integer.parseInt(newDate.substring(14, 16));

        if (yy1 < yy2)
            sessionInfo = newSessionInfo;
        else if (yy1 == yy2) {
            if (mm1 < mm2)
                sessionInfo = newSessionInfo;
            else if (mm1 == mm2) {
                if (dd1 < dd2)
                    sessionInfo = newSessionInfo;
                else if (dd1 == dd2) {
                    if (h1 < h2)
                        sessionInfo = newSessionInfo;
                    else if (h1 == h2) {
                        if (m1 < m2)
                            sessionInfo = newSessionInfo;
                    }
                }
            }
        }
    }

}
