package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.medha.avinder.uietianshub.ActivityFullscreenImage;
import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.models.Timeline;

import java.util.ArrayList;

public class AdapterTimeline extends RecyclerView.Adapter<AdapterTimeline.MyViewHolder>{

    private Context context;
    private ArrayList<Timeline> timelineList;


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTimeElapsed, tvDetails;
        ImageView ivThumbnail, ivPlaceholder;

        MyViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvTimeElapsed = view.findViewById(R.id.tvTimeElapsed);
            ivThumbnail = view.findViewById(R.id.ivThumbnail);
            tvDetails = view.findViewById(R.id.tvDetails);
            ivPlaceholder = view.findViewById(R.id.ivPlaceholder);
        }
    }


    public AdapterTimeline(Context context, ArrayList<Timeline> timelineList) {
        this.context = context;
        this.timelineList = timelineList;
    }

    @NonNull
    @Override
    public AdapterTimeline.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline, parent, false);
        return new AdapterTimeline.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterTimeline.MyViewHolder holder, int position) {
        final Timeline timeline = timelineList.get(position);
        holder.tvTitle.setText(timeline.getTitle());
        holder.tvTimeElapsed.setText(timeline.getTimestamp());
        holder.tvDetails.setText(timeline.getDetails());
        Linkify.addLinks(holder.tvDetails, Linkify.ALL);

        if (timeline.getImage() != null) {
            Glide.with(context).asBitmap().load(timeline.getImage()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    holder.ivThumbnail.setImageBitmap(resource);
                    holder.ivPlaceholder.setVisibility(View.GONE);
                    holder.ivThumbnail.setVisibility(View.VISIBLE);
                }
            });
        } else {
            holder.ivThumbnail.setVisibility(View.GONE);
            holder.ivPlaceholder.setVisibility(View.GONE);
        }

        holder.ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageIntent = new Intent(context, ActivityFullscreenImage.class);
                imageIntent.putExtra("Link", timeline.getImage());
                imageIntent.putExtra("Image Name", timeline.getTitle() + "_" + timeline.getId());
                imageIntent.putExtra("Item", "Notice");
                context.startActivity(imageIntent);
            }
        });
    }

    @Override
    public int getItemCount () {
        return timelineList.size();
    }
}
