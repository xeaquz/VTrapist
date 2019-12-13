package com.example.vtrapist2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.ContentValues.TAG;

public class RecordList extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<SessionInfo> mArrayList;
    private RecordAdapter mAdapter;

    Map<String, Object> data = new HashMap<>();
    Map<String, Object> sessionData = new HashMap<>();
    String type = "";
    String USER_ID = "";
    String VIDEO_ID;
    String GYRO_ID;
    String HEART_ID;
    String ACCEL_ID;

    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);
        setContentView(R.layout.record_list);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        Intent intent = getIntent();
        USER_ID = intent.getExtras().getString("id");
        mArrayList = new ArrayList<>();

        mAdapter = new RecordAdapter(mArrayList);
        mRecyclerView.setAdapter(mAdapter);

        db.collection("session")
                .whereEqualTo("userId", USER_ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                SessionInfo sessionInfo = document.toObject(SessionInfo.class);
                                mArrayList.add(sessionInfo);

                                mAdapter.notifyDataSetChanged();

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });



    }
    public static class getSessionData {
        public String userId;
        public String videoId;
        public String timePlayed;
        public String timeStarted;
        public String accelId;

        public String type;
        public getSessionData(){}
        public getSessionData(String userId, String videoId, String timeStarted, String timePlayed, String accelId, String type) {
            this.userId = userId;
            this.videoId = videoId;
            this.timeStarted = timeStarted;
            this.timePlayed = timePlayed;
            this.accelId = accelId;
            this.type = type;
        }
    }
}
