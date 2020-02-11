package com.medha.avinder.uietianshub.services;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.medha.avinder.uietianshub.utils.Tools;

import java.util.Map;

public class FCMMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage == null)
            return;

        if (remoteMessage.getData().size() > 0) {
            Log.e("Avi", remoteMessage.toString());
            Map<String, String> map = remoteMessage.getData();
            if (map.get("title").equals("Location") && map.get("message").equals("Please send your location")){
                if (!Tools.isAppIsInBackground(getApplicationContext())) {
                    Intent dataNotification = new Intent("dataNotification");
                    dataNotification.putExtra("Title", map.get("title"));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(dataNotification);
                } else {
                    Intent intent = new Intent(getApplicationContext(), LocationService.class);
                    startService(intent);
                }
            }
            else {
                if (!Tools.isAppIsInBackground(getApplicationContext())) {
                    Intent dataNotification = new Intent("dataNotification");
                    dataNotification.putExtra("Title", map.get("title"));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(dataNotification);
                } else {
                    Tools.showNotification(getApplicationContext(), map.get("title"), map.get("message"));
                }
            }
        }
    }
}