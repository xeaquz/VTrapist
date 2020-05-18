package com.example.vtrapist2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {
    private ArrayList<Object> mList;
    public static final String API_KEY = "AIzaSyBY9yA9muDZwvNjX2_KEHYxzVR7DPDgUXI";
    public String VIDEO_ID = "";

    private String id;
    private String type;

    Map<String, Object> data = new HashMap<>();

    private YouTubeThumbnailView youTubeThumbnailView;
    private YouTubeThumbnailLoader youTubeThumbnailLoader;

    public class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        protected YouTubeThumbnailView youTubeThumbnailView;
        public ThumbnailViewHolder(View view) {
            super(view);
            this.youTubeThumbnailView = view.findViewById(R.id.youtubeThumbnailView);
        }
    }

    public ThumbnailAdapter(){}

    public ThumbnailAdapter(ArrayList<Object> list, String id, String type){
        this.mList = list;
        this.id = id;
        this.type = type;
    }

    public ThumbnailViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.thumbnail_adapter, viewGroup, false);
        ThumbnailViewHolder viewHolder = new ThumbnailViewHolder(view);
        return viewHolder;
    }

    public void onBindViewHolder(@NonNull ThumbnailViewHolder viewholder, int position) {
        YouTubeThumbnailLoader youTubeThumbnailLoader;

        viewholder.youTubeThumbnailView.initialize(API_KEY, new YouTubeThumbnailView.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {

                youTubeThumbnailLoader.setVideo(mList.get(position).toString());
                youTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                    @Override
                    public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                        youTubeThumbnailLoader.release();
                    }
                    @Override
                    public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                        youTubeThumbnailLoader.release();
                    }
                });
            }

            @Override
            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });
        viewholder.youTubeThumbnailView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                VIDEO_ID = mList.get(position).toString();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("videos").document(VIDEO_ID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                data = document.getData();
                                type = data.get("type").toString();

                                Intent intent = new Intent(context, PlayVideoSignal.class);
                                intent.putExtra("videoId", VIDEO_ID);
                                intent.putExtra("id", id);
                                intent.putExtra("type", type);
                                context.startActivity(intent);
                                //youTubePlayer.play();

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });



            }});
    }

    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
}
