package com.example.vtrapist2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> user = new HashMap<>();
    Map<Object, Object> record = new HashMap<>();

    String id;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




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

                db.collection("user")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                id = documentReference.getId();
                                Log.d("dddddd", "DocumentSnapshot added with ID: " + documentReference.getId());
                                Intent intent = new Intent(getApplicationContext(), VideoList.class);
                                intent.putExtra("type", type);
                                intent.putExtra("id", id);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("dddddd", "Error adding document", e);
                            }
                        });

            }
        });


    }

    public class UserInfo {
        public String userId;
        public String age;
        public String gender;
        public String phobia;
        public String medicalHistory;
        public String levelOfDepression;

        public UserInfo(String userId, String age, String gender, String phobia, String medicalHistory, String levelOfDepression) {
            this.userId = userId;
            this.age = age;
            this.gender = gender;
            this.phobia = phobia;
            this.medicalHistory = medicalHistory;
            this.levelOfDepression = levelOfDepression;
        }
    }

/*
        type.put("1", "acro");
        type.put("2", "spider");

        db.collection("users").document(id).collection("type")
                .add(type)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("dddddd", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("dddddd", "Error adding document", e);
                    }
                });
*/

}