package com.medha.avinder.uietianshub.puWifi;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Vibrator;

import com.medha.avinder.uietianshub.BuildConfig;

public class Functions {

    private static final String KEY_INITIALISE = "initialBit";
    private static final String KEY_AUTO_LOGIN = "autoLogin";
    private static final String KEY_ACTIVE_USER = "activeUser";
    private static final String PREFERENCES_NAME = "puSharedPreferences";
    private static final String WIFI_KEY = "wifiKey";
    private static final String KEY_STATUS = "status";
    private static final String KEY_TIMESTAMP = "timestamp";

    static void setTimeStamp(Context context, int timestamp) {
        writeToSharedPreferences(context, KEY_TIMESTAMP, timestamp + "");
    }

    static int getTimeStamp(Context context) {
        String str = readFromSharedPreferences(context, KEY_TIMESTAMP);
        if (str.isEmpty())
            return 0;
        return Integer.parseInt(str);
    }

    public static boolean isInitialised(Context context) {
        String str = readFromSharedPreferences(context, KEY_INITIALISE);
        switch (str) {
            case "":
            case "0":
                return false;
        }
        return true;
    }

    static void initialise(Context context) {
        writeToSharedPreferences(context, KEY_INITIALISE, 1 + "");
    }

    static void disable(Context context) {
        writeToSharedPreferences(context, KEY_INITIALISE, 0 + "");
    }

    static String getActiveUserName(Context context) {
        return readFromSharedPreferences(context, KEY_ACTIVE_USER);
    }

    static void setActiveUser(Context context, String userName) {
        writeToSharedPreferences(context, KEY_ACTIVE_USER, userName);
    }

    private static String readFromSharedPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, 0);
        return sharedPreferences.getString(key, "");
    }


    private static void writeToSharedPreferences(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(key, value);
        sharedPreferencesEditor.apply();
    }

    static boolean isPUCampus(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                if (ssid != null) {
                    ssid = ssid.toLowerCase();
                    return ssid.contains("pu@campus");
                }
            }
        }
        return false;
    }

    static void sendNotification(Context context, String message, boolean showAction) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("message", message);
        intent.putExtra("showAction", showAction);
        context.startService(intent);
    }

    static boolean isConnectedToWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network network : networks) {
                networkInfo = connectivityManager.getNetworkInfo(network);
                if ((networkInfo.getType() == ConnectivityManager.TYPE_WIFI) && (networkInfo.getState().equals(NetworkInfo.State.CONNECTED))) {
                    ConnectivityManager.setProcessDefaultNetwork(network);
                    return true;
                }
            }
            return false;

        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return (networkInfo != null) && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) && (networkInfo.getState().equals(NetworkInfo.State.CONNECTED));
        }
    }

    static boolean isDisconnectedFlagSet(Context context) {
        String str = readFromSharedPreferences(context, WIFI_KEY);
        if (str.equals(""))
            return true;
        int x = Integer.parseInt(str);
        return x == 1;
    }

    static void setDisconnectedFromPUCampusFlag(Context context, int value) {
        writeToSharedPreferences(context, WIFI_KEY, value + "");
    }

    static boolean isPUWifiActivity(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn.getClassName().contains("ActivityPuWifi");
    }

    static void setStatus(Context context, String status) {
        writeToSharedPreferences(context, KEY_STATUS, status);
    }

    static String getStatus(Context context){
        return readFromSharedPreferences(context, KEY_STATUS);
    }

    static boolean isAutoLoginEnabled(Context context) {
        String str = readFromSharedPreferences(context, KEY_AUTO_LOGIN);
        return str.equals(BuildConfig.FLAVOR) || Boolean.parseBoolean(str);
    }

    public static void setAutoLoginEnabled(Context context, boolean checked) {
        writeToSharedPreferences(context, KEY_AUTO_LOGIN, checked + BuildConfig.FLAVOR);
    }

    static void setvibration(Context context){
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);

    }
}
