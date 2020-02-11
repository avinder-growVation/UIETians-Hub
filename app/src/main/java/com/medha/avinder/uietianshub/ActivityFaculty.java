package com.medha.avinder.uietianshub;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medha.avinder.uietianshub.adapters.AdapterFaculty;
import com.medha.avinder.uietianshub.adapters.AdapterSpinner;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.data.SharedPreference;
import com.medha.avinder.uietianshub.models.Faculty;
import com.medha.avinder.uietianshub.utils.Tools;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.medha.avinder.uietianshub.ActivityMain.hmVersions;

public class ActivityFaculty extends AppCompatActivity {
    private RecyclerView recyclerView;

    private DatabaseHelper db;
    private SharedPreference sharedPreference;

    private ArrayList<Faculty> facultyList;
    private AdapterFaculty adapterFaculty;

    private ProgressDialog pdLoading;

    private String spSelectedBranch = "Computer Science";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);
        sharedPreference = new SharedPreference(this);

        init();
    }

    private void init() {
        setContentView(R.layout.activity_recycler_view);
        ((TextView) findViewById(R.id.tvTitle)).setText("Faculty Details");

        Spinner spBranch = findViewById(R.id.spFacultyBranch);
        recyclerView = findViewById(R.id.recyclerView);
        findViewById(R.id.rlFacultyBranch).setVisibility(View.VISIBLE);

        facultyList = new ArrayList<>();

        adapterFaculty = new AdapterFaculty(this, facultyList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterFaculty);

        pdLoading = getProgressDialog();

        final String[] branches = {"Computer Science", "Electronics and Communication", "Information Technology", "Electrical and Electronics", "Mechanical", "Biotechnology", "Applied Sciences"};

        AdapterSpinner adapterSubjects = new AdapterSpinner(this, branches, 2, null);
        spBranch.setAdapter(adapterSubjects);

        spBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spSelectedBranch = branches[i];

                if (Tools.isOnline(getApplicationContext())) {
                    checkVersion();
                } else {
                    getFaculty();
                    showSnackbar();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void checkVersion() {
        // If version conflict occurs, fetch latest faculty. else get faculty from SQLite.
        if (hmVersions != null) {
            if (hmVersions.containsKey("Faculty")) {
                int facultyLatestVersion = hmVersions.get("Faculty");
                if (facultyLatestVersion != sharedPreference.getFacultyVersion()) {
                    fetchFaculty(facultyLatestVersion);
                } else {
                    getFaculty();
                }
            } else {
                fetchFaculty(0);
            }
        } else {
            getFaculty();
        }
    }

    /**
     * This method fetches faculty from Firebase Server and store in SQLite.
     */
    private void fetchFaculty(final int facultyLatestVersion) {
        pdLoading.show();

        Call<JsonArray> call = Tools.getApi(getApplicationContext()).getFaculty();
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                if (response.body() != null){
                    JsonArray jsonArray = response.body();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        if (!jsonArray.get(i).toString().equals("null")) {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                            Faculty faculty = new Faculty();
                            faculty.setId(i + 1);
                            faculty.setName(jsonObject.get("name").getAsString());
                            faculty.setPosition(jsonObject.get("position").getAsString());
                            faculty.setBranch(jsonObject.get("branch").getAsString());
                            if (jsonObject.get("mail") != null) {
                                faculty.setMail(jsonObject.get("mail").getAsString());
                            }
                            if (jsonObject.get("contact") != null) {
                                faculty.setContact(jsonObject.get("contact").getAsString());
                            }
                            if (jsonObject.get("cvLink") != null) {
                                faculty.setCvLink(jsonObject.get("cvLink").getAsString());
                            }

                            int rows = db.updateFaculty(faculty);
                            if (rows == 0) {
                                db.insertFaculty(faculty);
                            }
                        }
                        getFaculty();
                        if (facultyLatestVersion != 0) {
                            sharedPreference.setFacultyVersion(facultyLatestVersion);
                        }
                    }
                } else {
                    Toast.makeText(ActivityFaculty.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                pdLoading.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                pdLoading.dismiss();
                Toast.makeText(ActivityFaculty.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method fetches faculty from SQLite.
     */
    private void getFaculty() {
        facultyList.clear();
        adapterFaculty.notifyDataSetChanged();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ArrayList<Faculty> list = new ArrayList<>(db.getBranchFaculty(Tools.getBranchAbbreviation(spSelectedBranch)));
                int count = 0;
                for (Faculty faculty : list) {
                    facultyList.add(faculty);
                    adapterFaculty.notifyItemInserted(count);
                    count++;
                }
            }
        }, 100);
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(ActivityFaculty.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading Faculty");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clMain), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
