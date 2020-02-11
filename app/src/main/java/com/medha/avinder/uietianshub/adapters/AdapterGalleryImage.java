package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.medha.avinder.uietianshub.ActivityFullscreenImage;
import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.models.Gallery;

import java.util.ArrayList;
import java.util.List;

public class AdapterGalleryImage extends RecyclerView.Adapter<AdapterGalleryImage.MyViewHolder> {

    private Context context;
    private List<Gallery> galleryList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;

        MyViewHolder(View view) {
            super(view);
            ivThumbnail = view.findViewById(R.id.ivThumbnail);
        }
    }


    public AdapterGalleryImage(Context context, ArrayList<Gallery> galleryList) {
        this.context = context;
        this.galleryList = galleryList;
    }

    @NonNull
    @Override
    public AdapterGalleryImage.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false);
        return new AdapterGalleryImage.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterGalleryImage.MyViewHolder holder, int position) {
        final Gallery gallery = galleryList.get(position);
        holder.ivThumbnail.setMinimumHeight(holder.ivThumbnail.getWidth());
        Glide.with(context).load(gallery.getLink()).into(holder.ivThumbnail);

        holder.ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityFullscreenImage.class);
                intent.putExtra("Link", gallery.getLink());
                intent.putExtra("Image Name", gallery.getTimestamp() + "_" + gallery.getId());
                intent.putExtra("Item", "Gallery");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }
}