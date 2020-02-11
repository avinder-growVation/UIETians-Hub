package com.medha.avinder.uietianshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medha.avinder.uietianshub.adapters.AdapterCommittee;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.data.SharedPreference;
import com.medha.avinder.uietianshub.models.Committee;
import com.medha.avinder.uietianshub.utils.RecyclerTouchListener;
import com.medha.avinder.uietianshub.utils.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.medha.avinder.uietianshub.ActivityMain.hmVersions;

public class ActivityCommittees extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;

    private DatabaseHelper db;
    private SharedPreference sharedPreference;

    private ArrayList<Committee> committeeList;
    private AdapterCommittee adapterCommittee;

    private ProgressDialog pdLoading;

    private boolean isDetailsPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);
        sharedPreference = new SharedPreference(this);

        init();
    }

    private void init() {
        setContentView(R.layout.activity_recycler_view);
        ((TextView) findViewById(R.id.tvTitle)).setText("Committees");
        isDetailsPage = false;

        recyclerView = findViewById(R.id.recyclerView);
        pdLoading = getProgressDialog();

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setVisibility(View.VISIBLE);

        committeeList = new ArrayList<>();

        adapterCommittee = new AdapterCommittee(this, committeeList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterCommittee);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                showDetailsView(committeeList.get(position).getName());
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityCommittees.this, ActivityAddCommittee.class);
                startActivity(intent);
            }
        });

        if (Tools.isOnline(getApplicationContext())) {
            checkVersion();
        } else {
            showSnackbar();
            getCommittees(false);
        }

        findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void checkVersion() {
        // If version conflict occurs, fetch latest committees. else get committees from SQLite.
        if (hmVersions != null) {
            if (hmVersions.containsKey("Committees")) {
                int committeesLatestVersion = hmVersions.get("Committees");
                if (committeesLatestVersion != sharedPreference.getCommitteesVersion()) {
                    fetchCommittees(committeesLatestVersion);
                } else {
                    getCommittees(false);
                }
            } else {
                fetchCommittees(0);
            }
        } else {
            getCommittees(false);
        }
    }

    /**
     * This method fetches committees from Firebase Server and store in SQLite.
     */
    private void fetchCommittees(final int committeesLatestVersion) {
        pdLoading.show();

        Call<JsonArray> call = Tools.getApi(getApplicationContext()).getCommittees();
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                if (response.body() != null){
                    JsonArray jsonArray = response.body();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        if (!jsonArray.get(i).toString().equals("null")) {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                            Committee committee = new Committee();
                            committee.setId(i + 1);
                            committee.setName(jsonObject.get("name").getAsString());
                            committee.setDetails(jsonObject.get("details").getAsString());
                            committee.setContact(jsonObject.get("contact").getAsString());
                            committee.setCoverImage(jsonObject.get("coverImage").getAsString());
                            if (jsonObject.get("detailsImage") != null) {
                                committee.setDetailsImage(jsonObject.get("detailsImage").getAsString());
                            }
                            if (jsonObject.get("aboutTeam") != null) {
                                committee.setAboutTeam(jsonObject.get("aboutTeam").getAsString());
                            }
                            if (jsonObject.get("webPageLink") != null) {
                                committee.setWebPageLink(jsonObject.get("webPageLink").getAsString());
                            }
                            if (jsonObject.get("otherInfo") != null) {
                                committee.setOtherInfo(jsonObject.get("otherInfo").getAsString());
                            }

                            int rows = db.updateCommittee(committee);
                            if (rows == 0) {
                                db.insertCommittee(committee);
                            }
                        }
                    }
                    getCommittees(true);
                    if (committeesLatestVersion != 0) {
                        sharedPreference.setCommitteesVersion(committeesLatestVersion);
                    }
                } else {
                    Toast.makeText(ActivityCommittees.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                pdLoading.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                pdLoading.dismiss();
                Toast.makeText(ActivityCommittees.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method fetches committees from SQLite.
     */
    private void getCommittees(boolean update) {
        committeeList.addAll(db.getAllCommittees());
        adapterCommittee.notifyDataSetChanged();

        new SaveCoversInStorage().execute(update);
        new SaveDetailImagesInStorage().execute(update);
    }

    /**
     * This method shows details activity of committee.
     */
    private void showDetailsView(String committeeName) {
        setContentView(R.layout.activity_committees);
        isDetailsPage = true;

        ImageView ivCoverImage = findViewById(R.id.ivCoverImage);
        ImageView ivDetailsImage = findViewById(R.id.ivDetailsImage);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvDetails = findViewById(R.id.tvDetails);
        TextView tvContact = findViewById(R.id.tvContact);
        TextView tvAboutTeamTitle = findViewById(R.id.tvAboutTeamTitle);
        TextView tvAboutTeam = findViewById(R.id.tvAboutTeam);
        TextView tvWebPageLinkTitle = findViewById(R.id.tvWebPageLinkTitle);
        TextView tvWebPageLink = findViewById(R.id.tvWebPageLink);
        TextView tvOtherInfoTitle = findViewById(R.id.tvOtherInfoTitle);
        TextView tvOtherInfo = findViewById(R.id.tvOtherInfo);

        final Committee committee = db.getCommittee(committeeName);

        tvName.setText(committee.getName());
        tvDetails.setText(committee.getDetails());
        tvContact.setText(committee.getContact());

        File file = new File(getCacheDir(), "committee_" + committee.getId() + "_cover");
        if (file.exists()) {
            try {
                Glide.with(getApplicationContext()).load(BitmapFactory.decodeStream(new FileInputStream(file))).into(ivCoverImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Glide.with(getApplicationContext()).load(committee.getCoverImage()).into(ivCoverImage);
        }

        if (committee.getDetailsImage() != null) {
            File file2 = new File(Environment.getExternalStorageDirectory(), "committee_" + committee.getId() + "_details");
            if (file2.exists()) {
                try {
                    Glide.with(getApplicationContext()).load(BitmapFactory.decodeStream(new FileInputStream(file2))).into(ivDetailsImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                Glide.with(getApplicationContext()).load(committee.getDetailsImage()).into(ivDetailsImage);
            }
        } else {
            ConstraintLayout constraintLayout  = findViewById(R.id.constraintLayout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            int margin = getResources().getDimensionPixelOffset(R.dimen.margin_60_dp);
            constraintSet.connect(ivCoverImage.getId(), ConstraintSet.TOP, R.id.tvTitle, ConstraintSet.BOTTOM, margin);
            constraintSet.applyTo(constraintLayout);
        }

        if (committee.getAboutTeam() != null){
            tvAboutTeamTitle.setVisibility(View.VISIBLE);
            tvAboutTeam.setVisibility(View.VISIBLE);
            tvAboutTeam.setText(committee.getAboutTeam());
        } else {
            tvAboutTeamTitle.setVisibility(View.GONE);
            tvAboutTeam.setVisibility(View.GONE);
        }

        if (committee.getWebPageLink() != null){
            tvWebPageLinkTitle.setVisibility(View.VISIBLE);
            tvWebPageLink.setVisibility(View.VISIBLE);
            tvWebPageLink.setText(committee.getWebPageLink());
        } else {
            tvWebPageLinkTitle.setVisibility(View.GONE);
            tvWebPageLink.setVisibility(View.GONE);
        }

        if (committee.getOtherInfo() != null){
            tvOtherInfoTitle.setVisibility(View.VISIBLE);
            tvOtherInfo.setVisibility(View.VISIBLE);
            tvOtherInfo.setText(committee.getOtherInfo());
        } else {
            tvOtherInfoTitle.setVisibility(View.GONE);
            tvOtherInfo.setVisibility(View.GONE);
        }

        Linkify.addLinks(tvDetails, Linkify.ALL);
        Linkify.addLinks(tvWebPageLink, Linkify.ALL);
        Linkify.addLinks(tvAboutTeam, Linkify.ALL);
        Linkify.addLinks(tvOtherInfo, Linkify.ALL);
        Linkify.addLinks(tvContact, Linkify.ALL);

        final Intent intent = new Intent(ActivityCommittees.this, ActivityFullscreenImage.class);
        intent.putExtra("Item", "Committee");

        ivCoverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("Link", committee.getCoverImage());
                startActivity(intent);
            }
        });
        if (committee.getDetailsImage() != null) {
            ivDetailsImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent.putExtra("Link", committee.getDetailsImage());
                    startActivity(intent);
                }
            });
        }

        findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * If details page is open, onBackPressed() loads main page.
     * Else finishes the activity.
     */
    public void onBackPressed() {
        if (isDetailsPage){
            init();
        } else {
            super.onBackPressed();
        }
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(ActivityCommittees.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading Committees");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clMain), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    /**
     * This class stores cover images in storage in background and update SQLite.
     */
    private class SaveCoversInStorage extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... booleans) {
            for (final Committee committee : db.getAllCommittees()) {
                File file = new File(getCacheDir(), "committee_" + committee.getId() + "_cover");
                if (booleans[0]) {
                    saveImageFromUrlToStorage(committee.getCoverImage(), file);
                } else {
                    if (!file.exists()) {
                        saveImageFromUrlToStorage(committee.getCoverImage(), file);
                    }
                }
            }
            return null;
        }
    }

    /**
     * This class stores detail images in storage in background.
     */
    private class SaveDetailImagesInStorage extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... booleans) {
            for (final Committee committee : db.getAllCommittees()) {
                if (committee.getDetailsImage() != null) {
                    File file = new File(getCacheDir(), "committee_" + committee.getId() + "_details");
                    if (booleans[0]) {
                        saveImageFromUrlToStorage(committee.getDetailsImage(), file);
                    } else {
                        if (!file.exists()) {
                            saveImageFromUrlToStorage(committee.getDetailsImage(), file);
                        }
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