package com.medha.avinder.uietianshub.puWifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Simar Arora on 2/3/2015.
 * This App is Licensed under GNU General Public License. A copy of this license can be found in the root of this project.
 *
 */
public class LogoutTask extends AsyncTask<Void, String, Void> {

    private Context context;
    private static final String logoutURL = "https://securelogin.pu.ac.in/cgi-bin/login?cmd=logout";

    public LogoutTask(Context context) {
        this.context = context;

        //Check if Wifi is connected
        if (Functions.isConnectedToWifi(context)) {
            if (!Functions.isPUCampus(context)) {
                if (Build.VERSION.SDK_INT >= 21)
                    ConnectivityManager.setProcessDefaultNetwork(null);
                //Functions.sendNotification(context, "Not Connected To PU@Campus", false);
                if (Functions.isPUWifiActivity(context)) {
                    ActivityPuWifi.tvStatus.setText("Not Connected To PU@Campus");
                    Functions.setvibration(context);
                }
                this.cancel(true);
            }
        } else {
            //Functions.sendNotification(context, "Not Connected To Wifi", false);
            if (Functions.isPUWifiActivity(context)) {
                ActivityPuWifi.tvStatus.setText("Not Connected To Wifi");
                Functions.setvibration(context);
            }
            this.cancel(true);
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            //Set up Connection
            URL url = new URL(logoutURL);
            connection = (HttpURLConnection) url.openConnection();
            inputStream = connection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            //Check for result
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("Logout")) {
                    publishProgress("Logout Successful");
                    break;
                } else if (line.contains("User not logged in")) {
                    publishProgress("User not logged in");
                    break;
                }
            }
        } catch (Exception ignored) {
        } finally {
            //Close Connection
            if (connection != null)
                connection.disconnect();
            //Close InputStream
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //Close BufferedReader
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        if (Functions.isPUWifiActivity(context)) {
            if (values[0].equals("Logout Successful")) {
                ActivityPuWifi.tvStatus.setText("Logged Out");
                Functions.setStatus(context,"Logged Out");
            } else {
                ActivityPuWifi.tvStatus.setText("User not logged in");
                Functions.setStatus(context,"User not logged in");
            }
            Functions.setvibration(context);
        } else {
            Functions.sendNotification(context, values[0], false);
            if (values[0].equals("Logout Successful")) {
                Functions.setStatus(context, "Logged Out");
            } else {
                Functions.setStatus(context, "User not logged in");
            }
        }
    }
}
