package com.example.vtrapist2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import static android.content.ContentValues.TAG;

public class RecordList extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<SessionInfo> mArrayList;
    private RecordAdapter mAdapter;

    private TextView txtView_name;

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

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#49A5F6")));

        txtView_name = findViewById(R.id.txtView_name);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        Intent intent = getIntent();
        USER_ID = intent.getExtras().getString("uid");

        // get user name and set TextView
        DocumentReference docRef = db.collection("user").document(USER_ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        data = document.getData();
                        String name = data.get("name").toString();
                        txtView_name.setText(name + "님의 기록입니다!");

                    } else {
                        Log.d(TAG, "No such document");
                        txtView_name.setText("데이터 불러오기 실패");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

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

}
