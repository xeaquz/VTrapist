package com.example.vtrapist2;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

public class Main extends AppCompatActivity {

    private SwipeRefreshLayout mySwipeRefreshLayout;
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
    private Button btnSaveTimestamp;

    Map<String, Object> data = new HashMap<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        txtView_hello = findViewById(R.id.txtView_hello);
        txtView_session = findViewById(R.id.txtView_session);
        btnStart = findViewById(R.id.btnStart);
        btnHistory = findViewById(R.id.btnHistory);
        btnSaveTimestamp = findViewById(R.id.btnSaveTimestamp);
        mySwipeRefreshLayout = findViewById(R.id.refresh_layout);

        // set action bar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#49A5F6")));
        
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        setSessionInfo();
                        mySwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

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

        btnSaveTimestamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SaveTimestamp.class);
                startActivity(intent);
            }
        });
    }

    protected void onResume() {
        super.onResume();
        setSessionInfo();
    }

    public void setSessionInfo() {
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
