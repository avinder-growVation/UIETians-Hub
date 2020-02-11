package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.medha.avinder.uietianshub.ActivityFullscreenImage;
import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.models.Gallery;

import java.util.ArrayList;

/**
 * Created by Avinder on 29-01-2018.
 */

public class AdapterWorkshopImage extends RecyclerView.Adapter<AdapterWorkshopImage.MyViewHolder> {

    private Context context;
    private ArrayList<Gallery> imageList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvImageName;

        MyViewHolder(View view) {
            super(view);
            ivThumbnail = view.findViewById(R.id.ivThumbnail);
            tvImageName = view.findViewById(R.id.tvImageName);
        }
    }


    public AdapterWorkshopImage(Context context, ArrayList<Gallery> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public AdapterWorkshopImage.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workshop_image, parent, false);
        return new AdapterWorkshopImage.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterWorkshopImage.MyViewHolder holder, int position) {
        final Gallery gallery = imageList.get(position);
        holder.tvImageName.setText(gallery.getTimestamp());
        Glide.with(context).load(gallery.getLink()).into(holder.ivThumbnail);

        final Intent intent = new Intent(context, ActivityFullscreenImage.class);
        intent.putExtra("Image Name", gallery.getTimestamp() + gallery.getId());
        intent.putExtra("Item", "Workshop Image");
        intent.putExtra("Link", gallery.getLink());
        holder.ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(intent);
            }
        });

        holder.tvImageName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }
}