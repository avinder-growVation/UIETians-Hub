package com.medha.avinder.uietianshub.puWifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;

import com.medha.avinder.uietianshub.data.DatabaseHelper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Simar Arora on 2/3/2015.
 * This App is Licensed under GNU General Public License. A copy of this license can be found in the root of this project.
 */
public class LoginTask extends AsyncTask<Void, String, Void> {

    private Context context;
    private static final String loginURL = "https://securelogin.pu.ac.in/cgi-bin/login?cmd=login";

    public LoginTask(Context context, boolean fromBroadcastReceiver) {
        this.context = context;
        if (!fromBroadcastReceiver) {
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
                if (Functions.isPUWifiActivity(context)) {
                    ActivityPuWifi.tvStatus.setText("Not Connected To Wifi");
                    Functions.setvibration(context);
                }
                //Functions.sendNotification(context, "Not Connected To Wifi", false);
                this.cancel(true);
            }
        }
    }

    /**
     * Performed on background thread
     *
     * @param params
     * @return result in onPostExecute()
     */


    @Override
    protected Void doInBackground(Void... params) {

        int oldTimeStamp = Functions.getTimeStamp(context);
        int newTimeStamp = (int) (System.currentTimeMillis() / 1000L);

        if (newTimeStamp - oldTimeStamp > 10) {
            Functions.setTimeStamp(context, newTimeStamp);
        } else {
            return null;
        }

        DatabaseHelper db = new DatabaseHelper(context);
        //Get Active Username and Password
        String userName = Functions.getActiveUserName(context);
        String password = db.getPasswordFromUsername(userName);

        //Post Parameters
        String urlParameters = "user=" + userName + "&password=" + password;

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            URL url = new URL(loginURL);

            //Open Connection and define its properties
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Set output stream for post parameters
            DataOutputStream dataOutputStream = new DataOutputStream(
                    connection.getOutputStream());
            dataOutputStream.writeBytes(urlParameters);
            dataOutputStream.flush();
            dataOutputStream.close();

            //Get InputStream from connection
            inputStream = connection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            //Check for Result and publish progress accordingly
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("External Welcome Page")) {
                    publishProgress("Login Successful", "true");
                    break;
                } else if (line.contains("Authentication failed")) {
                    publishProgress("Authentication Failed", "false");
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

    /**
     * Publish Progress
     *
     * @param values
     */
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        boolean showAction = Boolean.parseBoolean(values[1]);
        if (Functions.isPUWifiActivity(context)) {
            if (values[0].equals("Login Successful")) {
                ActivityPuWifi.tvStatus.setText("Logged In as " + Functions.getActiveUserName(context));
                Functions.setStatus(context,"Logged In");
            } else {
                ActivityPuWifi.tvStatus.setText("Authentication Failed");
                Functions.setStatus(context,"Authentication Failed");
            }
            Functions.setvibration(context);
        } else {
            Functions.sendNotification(context, values[0], showAction);
            if (values[0].equals("Login Successful")) {
                Functions.setStatus(context, "Logged In");
            } else {
                Functions.setStatus(context, "Authentication Failed");
            }
        }
    }
}