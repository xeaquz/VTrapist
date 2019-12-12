package com.example.vtrapist2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private ArrayList<Object> mArrayList;
    private RecordAdapter mAdapter;

    Map<String, Object> data = new HashMap<>();
    Map<String, Object> videoData = new HashMap<>();
    String type = "";
    String id = "";
    String VIDEO_ID;

    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);
        setContentView(R.layout.thumbnails);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        Intent intent = getIntent();
        id = intent.getExtras().getString("id");
        type = intent.getExtras().getString("type");
        mArrayList = new ArrayList<>();

        DocumentReference userRef = db.collection("user").document(id);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        data = document.getData();
                        type = data.get("type").toString();

                        DocumentReference docRef = db.collection("video").document(type);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                        videoData = document.getData();

                                        for (Map.Entry<String, Object> entry : videoData.entrySet()) {
                                            System.out.println("key : " + entry.getKey() + " , value : " + entry.getValue());
                                            mArrayList.add(entry.getValue());
                                        }

                                        mAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        mAdapter = new RecordAdapter(mArrayList, id);
        mRecyclerView.setAdapter(mAdapter);
    }
}
