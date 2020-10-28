package com.example.vtrapist2;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class SaveTimestamp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_timestamp);

        Button btnSave = findViewById(R.id.btn_save);

        Log.d("dddddd", "1");

        ArrayList<Double> data = new ArrayList<>();
        double time;

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readFile();

            }
        });

    }

    public String readFile() {
        StringBuffer data = new StringBuffer();

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            try {
                File f = new File(path, "gyroY.txt");
                BufferedReader buffer = new BufferedReader(new FileReader(f));
                String str = buffer.readLine();
                Log.d("dddddd", "2");
                while (str != null) {
                    data.append(str);
                    str = buffer.readLine();
                }
                buffer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("dddddd", data.toString());
        }
        return data.toString();

    }
}
