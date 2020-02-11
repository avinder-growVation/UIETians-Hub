package com.medha.avinder.uietianshub;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medha.avinder.uietianshub.adapters.AdapterWorkshop;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.data.SharedPreference;
import com.medha.avinder.uietianshub.models.Workshop;
import com.medha.avinder.uietianshub.utils.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.medha.avinder.uietianshub.ActivityMain.hmVersions;

public class ActivityWorkshops extends AppCompatActivity {
    private RecyclerView recyclerView;

    private DatabaseHelper db;
    private SharedPreference sharedPreference;

    private AdapterWorkshop adapterWorkshop;
    private ArrayList<Workshop> workshopList;

    private ProgressDialog pdLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        ((TextView)findViewById(R.id.tvTitle)).setText("Workshops");

        db = new DatabaseHelper(this);
        sharedPreference = new SharedPreference(this);

        init();
    }

    private void init() {
        recyclerView = findViewById(R.id.recyclerView);

        workshopList = new ArrayList<>();

        adapterWorkshop = new AdapterWorkshop(getApplicationContext(), workshopList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterWorkshop);

        pdLoading = getProgressDialog();
        if (Tools.isOnline(this)) {
            checkVersion();
        } else {
            showSnackbar();
            getWorkshops(false);
        }

        findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void checkVersion() {
        // If version conflict occurs, fetch latest workshop content. else get workshop from SQLite.
        if (hmVersions != null) {
            if (hmVersions.containsKey("Workshops")) {
                int workshopsLatestVersion = hmVersions.get("Workshops");
                if (workshopsLatestVersion != sharedPreference.getWorkshopsVersion()) {
                    fetchWorkshops(workshopsLatestVersion);
                } else {
                    getWorkshops(false);
                }
            } else {
                fetchWorkshops(0);
            }
        } else {
            getWorkshops(false);
        }
    }

    /**
     * This method fetches workshops from Firebase Server and store in SQLite.
     */
    private void fetchWorkshops(final int workshopsLatestVersion) {
        pdLoading.show();

        Call<JsonArray> call = Tools.getApi(getApplicationContext()).getWorkshops();
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                if (response.body() != null){
                    JsonArray jsonArray = response.body();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        if (!jsonArray.get(i).toString().equals("null")) {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                            Workshop workshop = new Workshop();
                            workshop.setName(jsonObject.get("name").getAsString());
                            workshop.setLogo(jsonObject.get("logo").getAsString());
                            if (jsonObject.get("contentLink") != null) {
                                workshop.setContentLink(jsonObject.get("contentLink").getAsString());
                            }
                            if (jsonObject.get("sample1Link") != null) {
                                workshop.setSample1Link(jsonObject.get("sample1Link").getAsString());
                            }
                            if (jsonObject.get("sample2Link") != null) {
                                workshop.setSample2Link(jsonObject.get("sample2Link").getAsString());
                            }

                            int rows = db.updateWorkshop(workshop);
                            if (rows == 0) {
                                db.insertWorkshop(workshop);
                            }
                        }
                        getWorkshops(true);
                        if (workshopsLatestVersion != 0) {
                            sharedPreference.setWorkshopsVersion(workshopsLatestVersion);
                        }
                    }
                } else {
                    Toast.makeText(ActivityWorkshops.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                pdLoading.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                pdLoading.dismiss();
                Toast.makeText(ActivityWorkshops.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method fetches workshops name from SQLite.
     */
    private void getWorkshops(boolean update) {
        ArrayList<Workshop> list = new ArrayList<>(db.getAllWorkshops());
        int count = 0;
        for (Workshop workshop : list) {
            workshopList.add(workshop);
            adapterWorkshop.notifyItemInserted(count);
            count++;
        }
        new SaveLogosInStorage().execute(update);
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(ActivityWorkshops.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading Workshop Content");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clMain), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    /**
     * This class stores logos in storage in background.
     */
    private class SaveLogosInStorage extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... booleans) {
            for (final Workshop workshop : db.getAllWorkshops()) {
                File file = new File(getCacheDir(), workshop.getName() + "_logo");
                if (booleans[0]) {
                    saveImageFromUrlToStorage(workshop.getLogo(), file);
                } else {
                    if (!file.exists()) {
                        saveImageFromUrlToStorage(workshop.getLogo(), file);
                    }
                }
            }
            return null;
        }
    }

    /**
     * This method downloads image and updates SQLite.
     */
    private void saveImageFromUrlToStorage(String link, File file) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}