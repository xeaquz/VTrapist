package com.example.vtrapist2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toolbar;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> user = new HashMap<>();
    Map<Object, Object> record = new HashMap<>();

    String uid;
    String userType;
    String userLevel;

    Button btnGo;
    EditText editTxt_name;
    EditText editTxt_age;
    EditText editTxt_gender;
    EditText editTxt_medical;

    Spinner spinner;
    ArrayList<String> typeList;
    ArrayAdapter<String> typeAdapter;
    ArrayList<String> levelList;
    ArrayAdapter<String> levelAdapter;

    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;

    @Override

    public void onStart() {

        super.onStart();

        // 활동을 초기화할 때 사용자가 현재 로그인되어 있는지 확인합니다.

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        Log.d("dddddd", "DocumentSnapshot added with ID: " + uid);

        getSupportActionBar().hide();


        DocumentReference docRef = db.collection("user").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Intent intent = new Intent(getApplicationContext(), Main.class);
                        Log.d("dddddd", uid);
                        intent.putExtra("uid", uid);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


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
                userType = typeList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        levelList = new ArrayList<>();
        levelList.add("1");
        levelList.add("2");
        levelList.add("3");
        levelList.add("4");

        levelAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, levelList);

        spinner = findViewById(R.id.spinner_level);
        spinner.setAdapter(levelAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                userLevel = levelList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnGo = findViewById(R.id.btnGo);
        editTxt_name = findViewById(R.id.editTxt_name);
        editTxt_age = findViewById(R.id.editTxt_age);
        editTxt_gender = findViewById(R.id.editTxt_gender);
        editTxt_medical = findViewById(R.id.editTxt_medical);


        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTxt_name.getText().toString();
                String age = editTxt_age.getText().toString();
                String gender = editTxt_gender.getText().toString();
                String medical = editTxt_medical.getText().toString();
                String level = userLevel;
                String type = userType;

                user.put("name", name);
                user.put("age", age);
                user.put("type", type);
                user.put("gender", gender);
                user.put("medical", medical);
                user.put("level", level);

                db.collection("user").document(uid).set(user);

                Log.d("dddddd", "DocumentSnapshot added with ID: " + uid);

                Intent intent2 = new Intent(getApplicationContext(), VideoList.class);
                intent2.putExtra("type", type);
                intent2.putExtra("uid", uid);
                startActivity(intent2);
                finish();
//                db.collection("user")
//                        .add(user)
//                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                            @Override
//                            public void onSuccess(DocumentReference documentReference) {
//                                id = documentReference.getId();
//                                Log.d("dddddd", "DocumentSnapshot added with ID: " + documentReference.getId());
//                                Intent intent = new Intent(getApplicationContext(), VideoList.class);
//                                intent.putExtra("type", type);
//                                intent.putExtra("id", uid);
//                                startActivity(intent);
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.w("dddddd", "Error adding document", e);
//                            }
//                        });

            }
        });


    }

}