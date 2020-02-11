package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.medha.avinder.uietianshub.ActivityGallery;
import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.models.Gallery;
import com.medha.avinder.uietianshub.utils.Tools;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class AdapterGallery extends RecyclerView.Adapter<AdapterGallery.MyViewHolder> {

    private Context context;
    private List<String> monthList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonth;
        RecyclerView recyclerView;

        MyViewHolder(View view) {
            super(view);
            tvMonth = view.findViewById(R.id.tvSavedUsersTitle);
            recyclerView = view.findViewById(R.id.recyclerView);
        }
    }


    public AdapterGallery(Context context, List<String> monthList) {
        this.context = context;
        this.monthList = monthList;
    }

    @NonNull
    @Override
    public AdapterGallery.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        return new AdapterGallery.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterGallery.MyViewHolder holder, int position) {
        String monthTitle = monthList.get(position);
        holder.tvMonth.setText(monthTitle);

        ArrayList<Gallery> galleryImageList = new ArrayList<>();

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 4);
        holder.recyclerView.setLayoutManager(mLayoutManager);
        holder.recyclerView.setItemAnimator(new DefaultItemAnimator());
        AdapterGalleryImage adapterGalleryImage = new AdapterGalleryImage(context, galleryImageList);
        holder.recyclerView.setHasFixedSize(true);
        holder.recyclerView.setAdapter(adapterGalleryImage);

        for (Gallery gallery : ActivityGallery.galleryList){
            try {
                if (Tools.getMonthFromDate(gallery.getTimestamp()).equals(monthTitle) && !galleryImageList.contains(gallery)){
                    galleryImageList.add(gallery);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        adapterGalleryImage.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return monthList.size();
    }
}