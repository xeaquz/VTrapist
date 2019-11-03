package com.example.vtrapist2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder>{
    private ArrayList<Object> mList;
    public static final String API_KEY = "AIzaSyBY9yA9muDZwvNjX2_KEHYxzVR7DPDgUXI";
    public String VIDEO_ID = "";
    public String USER_ID;

    private TextView txtView_name;


    public class RecordViewHolder extends RecyclerView.ViewHolder {
        public RecordViewHolder(View view) {
            super(view);
            txtView_name = view.findViewById(R.id.txtView_name);
        }
    }

    public RecordAdapter(){}

    public RecordAdapter(ArrayList<Object> list, String id){
        this.mList = list;
        this.USER_ID = id;
    }

    public RecordAdapter.RecordViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.record_adapter, viewGroup, false);
        RecordAdapter.RecordViewHolder viewHolder = new RecordAdapter.RecordViewHolder(view);
        return viewHolder;
    }

    public void onBindViewHolder(@NonNull RecordAdapter.RecordViewHolder viewholder, int position) {
        final RecordViewHolder myViewHolder = (RecordViewHolder) viewholder;
        String name = mList.get(position).toString();
        txtView_name.setText(name);
    }

    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
}
