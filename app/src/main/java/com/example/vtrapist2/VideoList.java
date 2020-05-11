package com.example.vtrapist2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView.OnInitializedListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.ContentValues.TAG;

public class VideoList extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<Object> mArrayList;
    private ThumbnailAdapter mAdapter;

    private Spinner spinner;
    private ArrayAdapter<String> typeAdapter;
    private ArrayList<String> typeList;

    private Map<String, Object> data = new HashMap<>();
    private Map<String, Object> videoData = new HashMap<>();
    private String type = "";
    private String id = "";
    private String VIDEO_ID;

    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);
        setContentView(R.layout.video_list);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#49A5F6")));

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        Intent intent = getIntent();
        id = intent.getExtras().getString("uid");

        // set type list spinner
        typeList = new ArrayList<>();
        typeList.add("height");
        typeList.add("spider");

        typeAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, typeList);

        spinner = findViewById(R.id.spinner_type);
        spinner.setAdapter(typeAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getVideo(typeList.get(i));
                type = typeList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // set thumbnail
        mArrayList = new ArrayList<>();
        mAdapter = new ThumbnailAdapter(mArrayList, id, type);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLinearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);


    }

    public void getVideo(String type) {
        mArrayList.clear();
        mAdapter.notifyDataSetChanged();
        db.collection("videos")
                .whereEqualTo("type", type)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                mArrayList.add(document.getId());
                                mAdapter.notifyDataSetChanged();

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
