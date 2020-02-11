package com.medha.avinder.uietianshub.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.medha.avinder.uietianshub.ActivityNoticeBoard;
import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.connection.Api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Tools {

    public static long getAppVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 100;
    }

    public static Api getApi(Context context) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        try {
            clientBuilder.sslSocketFactory(getSSLConfig(context).getSocketFactory());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        clientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.baseUrl("https://uietians-hub-b7769.firebaseio.com/");
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create());
        if (isPUCampus(context)) {
            retrofitBuilder.client(clientBuilder.build());
        }
        Retrofit retrofit = retrofitBuilder.build();

        return retrofit.create(Api.class);
    }

    public static boolean createDirIfNotExists(String path) {
        boolean result = true;

        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("TravellerLog :: ", "Problem creating Image folder");
                result = false;
            }
        }
        return result;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static boolean isPUCampus(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

    public static void showNotification(Context context, String title, String message) {
        Intent intent = new Intent(context, ActivityNoticeBoard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "channelId")
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(100, mBuilder.build());
    }

    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static SSLContext getSSLConfig(Context context) throws CertificateException, IOException,
            KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        // Loading CAs from an InputStream
        CertificateFactory cf = null;
        cf = CertificateFactory.getInstance("X.509");

        Certificate ca;
        // I'm using Java7. If you used Java6 close it manually with finally.
        try (InputStream cert = context.getResources().openRawResource(R.raw.pu)) {
            ca = cf.generateCertificate(cert);
        }

        // Creating a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Creating a TrustManager that trusts the CAs in our KeyStore.
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Creating an SSLSocketFactory that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext;
    }

    /**
     * Sending Data Notification
     */
    public static void sendDataNotification(String title, String message, String token) {
        HashMap<String,String> map = new HashMap<>();
        map.put("title", title);
        map.put("message", message);

        RequestDataMessage requestDataNotification = new RequestDataMessage();
        requestDataNotification.setSendNotificationModel(map);
        requestDataNotification.setToken(token);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api api = retrofit.create(Api.class);

        Call<ResponseBody> responseBodyCall = api.sendDataNotification(requestDataNotification);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                Log.e("Notification","Sent");
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("Notification",t.getMessage());
            }
        });
    }

    public static class RequestDataMessage {
        @SerializedName("to")
        private String token;

        @SerializedName("data")
        private HashMap<String, String> sendNotificationModel;

        void setSendNotificationModel(HashMap<String, String> sendNotificationModel) {
            this.sendNotificationModel = sendNotificationModel;
        }

        void setToken(String token) {
            this.token = token;
        }
    }

    public static String getBranchAbbreviation(String key) {
        HashMap<String, String> branchAbbreviationMap = new HashMap<>();
        branchAbbreviationMap.put("Computer Science", "Cse");
        branchAbbreviationMap.put("Electronics and Communication", "Ece");
        branchAbbreviationMap.put("Information Technology", "It");
        branchAbbreviationMap.put("Electrical and Electronics", "Eee");
        branchAbbreviationMap.put("Mechanical", "Mec");
        branchAbbreviationMap.put("Biotechnology", "Bio");
        branchAbbreviationMap.put("Applied Sciences", "App");

        return branchAbbreviationMap.get(key);
    }

    public static String getAbbreviationBranch(String key) {
        HashMap<String, String> abbreviationBranchMap = new HashMap<>();
        abbreviationBranchMap.put("Cse", "Computer Science");
        abbreviationBranchMap.put("Ece", "Electronics and Communication");
        abbreviationBranchMap.put("It", "Information Technology");
        abbreviationBranchMap.put("Eee", "Electrical and Electronics");
        abbreviationBranchMap.put("Mec", "Mechanical");
        abbreviationBranchMap.put("Bio", "Biotechnology");

        return abbreviationBranchMap.get(key);
    }

    public static String getMonthFromDate(String date) throws ParseException {
        Date d = new SimpleDateFormat("dd.MM.yy 'at' h:mm a", Locale.getDefault()).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int month = cal.get(Calendar.MONTH);

        if (month == 0) {
            return "January";
        } else if (month == 1) {
            return "February";
        } else if (month == 2) {
            return "March";
        } else if (month == 3) {
            return "April";
        } else if (month == 4) {
            return "May";
        } else if (month == 5) {
            return "June";
        } else if (month == 6) {
            return "July";
        } else if (month == 7) {
            return "August";
        } else if (month == 8) {
            return "September";
        } else if (month == 9) {
            return "October";
        } else if (month == 10) {
            return "November";
        } else {
            return "December";
        }
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}
