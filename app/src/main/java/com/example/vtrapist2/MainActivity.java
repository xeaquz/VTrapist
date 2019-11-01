package com.example.vtrapist2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> user = new HashMap<>();
    Map<String, Object> type = new HashMap<>();
    Map<Object, Object> record = new HashMap<>();

    String id;

    Button btnGo;
    EditText editTxt_name;
    EditText editTxt_age;
    EditText editTxt_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGo = findViewById(R.id.btnGo);
        editTxt_name = findViewById(R.id.editTxt_name);
        editTxt_age = findViewById(R.id.editTxt_age);
        editTxt_type = findViewById(R.id.editTxt_type);

        record.put("1", 3);
        record.put("2", 3);
        record.put("3", 2);
        record.put("4", 1);
        record.put("5", 3);

        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time = new Date();
        String time1 = fm.format(time);

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTxt_name.getText().toString();
                String age = editTxt_age.getText().toString();
                String type = editTxt_type.getText().toString();

                //record.put("time", time1);
                user.put("name", name);
                user.put("age", age);
                user.put("type", type);

                record.put("type", type);
                user.put(time1, record);


                db.collection("users")
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