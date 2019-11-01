package com.example.vtrapist2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;


public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {
    private ArrayList<Object> mList;
    public static final String API_KEY = "AIzaSyBY9yA9muDZwvNjX2_KEHYxzVR7DPDgUXI";
    public String VIDEO_ID = "";
    String id;

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

    public ThumbnailAdapter(ArrayList<Object> list, String id){
        this.mList = list;
        this.id = id;
    }

    public ThumbnailViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler, viewGroup, false);
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
                Intent intent;
                Context context = view.getContext();
                VIDEO_ID = mList.get(position).toString();
                intent = new Intent(context, PlayVideo.class);
                intent.putExtra("videoId", VIDEO_ID);
                intent.putExtra("id", id);
                context.startActivity(intent);
                //youTubePlayer.play();

            }});
    }

    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
}
