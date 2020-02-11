package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.models.Timeline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Avinder on 02-01-2018.
 */

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.MyViewHolder> {
    private Context context;
    private ArrayList<Timeline> notificationList;
    private String time;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle, tvTimestamp;

        public MyViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvTimestamp = view.findViewById(R.id.tvTimestamp);
        }
    }


    public AdapterNotification(Context context, ArrayList<Timeline> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Timeline timeline = notificationList.get(position);
        holder.tvTitle.setText(timeline.getTitle());
        try {
            final String CurrentDate =  new SimpleDateFormat("dd MMM yyyy 'at' h:mm a", Locale.getDefault()).format(new Date());
            String FinalDate = timeline.getTimestamp();

            Date date1;
            Date date2;

            SimpleDateFormat dates = new SimpleDateFormat("dd MMM yyyy 'at' h:mm a", Locale.getDefault());

            date1 = dates.parse(CurrentDate);
            date2 = dates.parse(FinalDate);

            long difference = Math.abs(date1.getTime() - date2.getTime());
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;
            long monthsInMilli = daysInMilli * 30;

            long elapsedMonths = difference / monthsInMilli;
            difference = difference % monthsInMilli;

            long elapsedDays = difference / daysInMilli;
            difference = difference % daysInMilli;

            long elapsedHours = difference / hoursInMilli;
            difference = difference % hoursInMilli;

            long elapsedMinutes = difference / minutesInMilli;
            difference = difference % minutesInMilli;

            long elapsedSeconds = difference / secondsInMilli;

            time = null;
            if (elapsedMonths > 0){
                if (elapsedMonths == 1){
                    time = elapsedMonths + " month ago";
                } else {
                    time = elapsedMonths + " months ago";
                }
            } else {
                if (elapsedDays > 0) {
                    if (elapsedDays == 1) {
                        time = elapsedDays + " day ago ";
                    } else {
                        time = elapsedDays + " days ago ";
                    }
                } else {
                    if (elapsedHours > 0) {
                        if (elapsedHours == 1) {
                            time = elapsedHours + " hour ago";
                        } else {
                            time = elapsedHours + " hours ago";
                        }
                    } else {
                        if (elapsedMinutes > 0) {
                            if (elapsedMinutes == 1) {
                                time = elapsedMinutes + " min ago";
                            } else {
                                time = elapsedMinutes + " mins ago";
                            }
                        } else {
                            if (elapsedSeconds >= 0) {
                                time = elapsedSeconds + " secs ago";
                            }
                        }
                    }
                }
            }

        } catch (Exception exception) {
            Log.e("DIDN'T WORK", "exception " + exception);
        }
        holder.tvTimestamp.setText(time);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}