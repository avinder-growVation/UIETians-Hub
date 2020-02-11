package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.medha.avinder.uietianshub.ActivityPdfViewer;
import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.models.Workshop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class AdapterWorkshop extends RecyclerView.Adapter<AdapterWorkshop.MyViewHolder> {

    private Context context;
    private ArrayList<Workshop> workshopList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivContent;
        TextView ivSample1, ivSample2;

        MyViewHolder(View view) {
            super(view);
            ivContent = view.findViewById(R.id.ivContent);
            ivSample1 = view.findViewById(R.id.ivSample1);
            ivSample2 = view.findViewById(R.id.ivSample2);
        }
    }


    public AdapterWorkshop(Context context, ArrayList<Workshop> workshopList) {
        this.context = context;
        this.workshopList = workshopList;
    }

    @NonNull
    @Override
    public AdapterWorkshop.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workshop, parent, false);
        return new AdapterWorkshop.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterWorkshop.MyViewHolder holder, int position) {
        final Workshop workshop = workshopList.get(position);

        final Intent intent = new Intent(context, ActivityPdfViewer.class);
        intent.putExtra("Workshop Name", workshop.getName());
        intent.putExtra("Item", "Workshop");
        holder.ivContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (workshop.getContentLink() != null) {
                    intent.putExtra("Link", workshop.getContentLink());
                    intent.putExtra("Downloadable", false);
                    if (Build.VERSION.SDK_INT >= 21) {
                        context.startActivity(intent);
                    } else {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(workshop.getContentLink())));
                    }
                } else {
                    Toast.makeText(context, "The reference content is coming soon.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        File file = new File(context.getCacheDir(), workshop.getName() + "_logo");
        if (file.exists()) {
            try {
                Glide.with(context).load(BitmapFactory.decodeStream(new FileInputStream(file))).into(holder.ivContent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Glide.with(context).load(workshop.getLogo()).into(holder.ivContent);
        }

        holder.ivSample1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (workshop.getSample1Link() != null) {
                    intent.putExtra("Link", workshop.getSample1Link());
                    intent.putExtra("Downloadable", true);
                    if (Build.VERSION.SDK_INT >= 21) {
                        context.startActivity(intent);
                    } else {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(workshop.getSample1Link())));
                    }
                } else {
                    Toast.makeText(context, "This feature is coming soon.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.ivSample2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (workshop.getSample2Link() != null) {
                    intent.putExtra("Link", workshop.getSample2Link());
                    intent.putExtra("Downloadable", true);
                    if (Build.VERSION.SDK_INT >= 21) {
                        context.startActivity(intent);
                    } else {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(workshop.getSample2Link())));
                    }
                } else {
                    Toast.makeText(context, "This feature is coming soon", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return workshopList.size();
    }
}