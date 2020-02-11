package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.models.Committee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by Avinder on 29-01-2018.
 */

public class AdapterCommittee extends RecyclerView.Adapter<AdapterCommittee.MyViewHolder> {

    private Context context;
    private ArrayList<Committee> committeeList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        ImageView thumbnail;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvTitle);
            thumbnail = view.findViewById(R.id.ivThumbnail);
        }
    }


    public AdapterCommittee(Context context, ArrayList<Committee> committeeList) {
        this.context = context;
        this.committeeList = committeeList;
    }

    @NonNull
    @Override
    public AdapterCommittee.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_committee, parent, false);
        return new AdapterCommittee.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCommittee.MyViewHolder holder, int position) {
        Committee committee = committeeList.get(position);
        holder.title.setText(committee.getName());

        File file = new File(context.getCacheDir(), "committee_" + committee.getId() + "_cover");
        if (file.exists()) {
            try {
                Glide.with(context).load(BitmapFactory.decodeStream(new FileInputStream(file))).into(holder.thumbnail);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Glide.with(context).load(committee.getCoverImage()).into(holder.thumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return committeeList.size();
    }
}