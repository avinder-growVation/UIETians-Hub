package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.medha.avinder.uietianshub.ActivityPdfViewer;
import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.models.Faculty;

import java.util.ArrayList;

public class AdapterFaculty extends RecyclerView.Adapter<AdapterFaculty.MyViewHolder>{

    private Context context;
    private ArrayList<Faculty> facultyList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPosition, tvMail, tvContact, tvCV;

        MyViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvName);
            tvPosition = view.findViewById(R.id.tvPosition);
            tvMail = view.findViewById(R.id.tvMail);
            tvContact = view.findViewById(R.id.tvContact);
            tvCV = view.findViewById(R.id.tvCV);
        }
    }


    public AdapterFaculty(Context context, ArrayList<Faculty> facultyList) {
        this.context = context;
        this.facultyList = facultyList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faculty, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final Faculty faculty = facultyList.get(position);
        holder.tvName.setText(faculty.getName());
        holder.tvPosition.setText(faculty.getPosition());
        if (faculty.getMail() != null) {
            holder.tvMail.setText(faculty.getMail());
        } else {
            holder.tvMail.setText("N/A");
        }
        if (faculty.getContact() != null) {
            holder.tvContact.setText(faculty.getContact());
        } else {
            holder.tvContact.setText("N/A");
        }

        Linkify.addLinks(holder.tvMail, Linkify.EMAIL_ADDRESSES);
        Linkify.addLinks(holder.tvContact, Linkify.PHONE_NUMBERS);
        if (faculty.getCvLink() != null) {
            holder.tvCV.setText("Click for More");
        }else{
            holder.tvCV.setText("CV Not Available");
        }
        holder.tvCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (faculty.getCvLink() != null) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        Intent intent = new Intent(context, ActivityPdfViewer.class);
                        intent.putExtra("Faculty Name", faculty.getName());
                        intent.putExtra("Item", "Faculty");
                        intent.putExtra("Link", faculty.getCvLink());
                        context.startActivity(intent);
                    } else {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(faculty.getCvLink())));
                    }
                } else {
                    Toast.makeText(context, "Currently the cv is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
        @Override
        public int getItemCount () {
            return facultyList.size();
    }
}
