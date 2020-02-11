package com.medha.avinder.uietianshub;

import android.app.ProgressDialog;
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
import com.medha.avinder.uietianshub.adapters.AdapterWorkshopImage;
import com.medha.avinder.uietianshub.models.Gallery;
import com.medha.avinder.uietianshub.utils.Tools;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityWorkshopImages extends AppCompatActivity {
    private RecyclerView recyclerView;

    private String workshopName;

    private ArrayList<Gallery> workshopImageList;
    private AdapterWorkshopImage adapterWorkshopImage;

    private ProgressDialog pdLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        ((TextView) findViewById(R.id.tvTitle)).setText("Workshop Images");

        workshopName = getIntent().getStringExtra("Workshop");

        init();
    }

    private void init() {
        recyclerView = findViewById(R.id.recyclerView);

        workshopImageList = new ArrayList<>();

        adapterWorkshopImage = new AdapterWorkshopImage(this, workshopImageList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterWorkshopImage);

        pdLoading = getProgressDialog();
        if (Tools.isOnline(this)) {
            fetchImages(workshopName);
        } else {
            showSnackbar();
        }

        findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * This method fetches workshops from Firebase Server and store in SQLite.
     */
    private void fetchImages(String workshop) {
        pdLoading.show();

        Call<JsonArray> call = Tools.getApi(getApplicationContext()).getWorkshopImages(workshop);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                if (response.body() != null){
                    JsonArray jsonArray = response.body();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        if (!jsonArray.get(i).toString().equals("null")) {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                            Gallery gallery = new Gallery();
                            gallery.setId(i + 1);
                            gallery.setTimestamp(jsonObject.get("timestamp").getAsString());
                            gallery.setLink(jsonObject.get("link").getAsString());

                            workshopImageList.add(gallery);
                        }
                        adapterWorkshopImage.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(ActivityWorkshopImages.this, "No images available yet. Would be uploaded soon", Toast.LENGTH_SHORT).show();
                }
                pdLoading.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                pdLoading.dismiss();
                if (t.getMessage().equals("Expected a com.google.gson.JsonArray but was com.google.gson.JsonNull")) {
                    Toast.makeText(ActivityWorkshopImages.this, "No images available yet. Would be uploaded soon", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityWorkshopImages.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(ActivityWorkshopImages.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading Workshop Images");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clMain), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
