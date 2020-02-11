package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.medha.avinder.uietianshub.ActivityPdfViewer;
import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.models.Subject;

import java.util.ArrayList;

public class AdapterSyllabus extends RecyclerView.Adapter<AdapterSyllabus.MyViewHolder> {
    private Context context;
    private ArrayList<Subject> subjectList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivThumbnail;

        MyViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            ivThumbnail = view.findViewById(R.id.ivThumbnail);
        }
    }

    public AdapterSyllabus(Context context, ArrayList<Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public AdapterSyllabus.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_syllabus, parent, false);
        return new AdapterSyllabus.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSyllabus.MyViewHolder holder, int position) {
        final Subject subject = subjectList.get(position);
        holder.tvTitle.setText(subject.getTitle());

        final Intent intent = new Intent(context, ActivityPdfViewer.class);
        intent.putExtra("BranchSem", subject.getBranchSem());
        intent.putExtra("Link", subject.getLink());
        intent.putExtra("Pages", subject.getPage());
        intent.putExtra("Item", "Syllabus");

        holder.ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 21) {
                    context.startActivity(intent);
                } else {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(subject.getLink())));
                }
            }
        });
        holder.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 21) {
                    context.startActivity(intent);
                } else {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(subject.getLink())));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }
}