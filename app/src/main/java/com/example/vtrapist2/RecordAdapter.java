package com.example.vtrapist2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder>{
    private ArrayList<SessionInfo> mList;
    public static final String API_KEY = "AIzaSyBY9yA9muDZwvNjX2_KEHYxzVR7DPDgUXI";
    public String VIDEO_ID = "";
    public String USER_ID;

    private Button btn_name;


    public class RecordViewHolder extends RecyclerView.ViewHolder {
        public RecordViewHolder(View view) {
            super(view);
            btn_name = view.findViewById(R.id.btn_name);

            btn_name.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent;
                    Context context = view.getContext();
                    intent = new Intent(context, PlayRecord.class);

                    int pos = getAdapterPosition();
                    intent.putExtra("videoId", mList.get(pos).videoId);
                    intent.putExtra("userId", mList.get(pos).userId);
                    intent.putExtra("accelId", mList.get(pos).accelId);
                    intent.putExtra("samplingRate_a", mList.get(pos).samplingRate_a);

                    context.startActivity(intent);
                    //youTubePlayer.play();

                }});
        }
    }

    public RecordAdapter(){}

    public RecordAdapter(ArrayList<SessionInfo> list){
        this.mList = list;
    }

    public RecordAdapter.RecordViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.record_adapter, viewGroup, false);
        RecordAdapter.RecordViewHolder viewHolder = new RecordAdapter.RecordViewHolder(view);
        return viewHolder;
    }

    public void onBindViewHolder(@NonNull RecordAdapter.RecordViewHolder viewholder, int position) {
        final RecordViewHolder myViewHolder = (RecordViewHolder) viewholder;
        String name = mList.get(position).timeStarted;
        Log.d("dddddd", name);
        btn_name.setText(name);


    }

    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
}
