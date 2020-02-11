package com.medha.avinder.uietianshub.puWifi;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.puWifi.widget.LogoutService;

public class NotificationService extends IntentService{

    public NotificationService(String name) {
        super(name);
    }

    public NotificationService(){
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if(intent == null)
            return;

        String message = intent.getStringExtra("message");

        boolean showAction = intent.getBooleanExtra("showAction", true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.icon_pu_login)
        .setContentTitle("PU Wifi")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_pu_large))
        .setDefaults(Notification.DEFAULT_VIBRATE);

        if(showAction){
            Intent logoutIntent = new Intent(this, LogoutService.class);
            PendingIntent logoutPendingIntent = PendingIntent.getService(this, 0, logoutIntent, 0);
            builder.addAction(0, "Logout", logoutPendingIntent);
        }

        Intent in = new Intent(this, ActivityPuWifi.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ActivityPuWifi.class);
        stackBuilder.addNextIntent(in);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        notificationManager.notify(0, notification);

        this.stopSelf();
    }
}
