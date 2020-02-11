package com.medha.avinder.uietianshub;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.data.SharedPreference;
import com.medha.avinder.uietianshub.models.Syllabus;
import com.medha.avinder.uietianshub.puWifi.ActivityPuWifi;
import com.medha.avinder.uietianshub.utils.Tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private ImageButton ibHamburger, ibWifi, ibNotification;
    private TextView tvBadge;
    private ImageView ivBanner;

    private DatabaseHelper db;
    private SharedPreference sharedPreference;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private BroadcastReceiver broadcastReceiver;

    private String branch;
    private int sem;

    public static HashMap<String, Integer> hmVersions;
    private DrawerLayout drawer;

    //Location related fields
    private String mLastUpdateTime;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private int count;

    private boolean isSyllabusLoading = false;
    private ProgressDialog pdSyllabusLoad;

    private boolean doubleBackToExitPressedOnce = false;
    private ArrayList<Syllabus> papersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        sharedPreference = new SharedPreference(this);

        String branchSem = db.getProfile().getBranchSem();
        if (branchSem.substring(0,2).equals("It")) {
            branch = "It";
            sem = Integer.parseInt(branchSem.substring(2));
        } else {
            branch = branchSem.substring(0, 3);
            sem = Integer.parseInt(branchSem.substring(3));
        }

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        init();
    }

    private void init() {
        ibNotification = findViewById(R.id.ibNotification);
        ibHamburger = findViewById(R.id.ibHamburger);
        ibWifi = findViewById(R.id.ibWifi);
        tvBadge = findViewById(R.id.tvBadge);
        ivBanner = findViewById(R.id.ivBanner);

        FirebaseMessaging.getInstance().subscribeToTopic("all");
        auth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(ActivityMain.this, ActivityLogin.class));
                    finish();
                }
            }
        };

        if (db.getProfileCount() == 0) {
            startActivity(new Intent(ActivityMain.this, ActivityLogin.class));
            finish();
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("dataNotification")){
                    String title = intent.getStringExtra("Title");
                    if (title.equals("Location")) {
                        initLocationSend();
                    } else {
                        tvBadge.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        pdSyllabusLoad = getProgressDialog();

        hmVersions = new HashMap<>();
        fetchContentVersions();
        //TODO
        getBannerImage();

        // Set on click listeners
        findViewById(R.id.ivSyllabus).setOnClickListener(this);
        findViewById(R.id.ivQuestionPapers).setOnClickListener(this);
        findViewById(R.id.ivGallery).setOnClickListener(this);
        findViewById(R.id.ivCommittees).setOnClickListener(this);
        findViewById(R.id.ivFaculty).setOnClickListener(this);
        findViewById(R.id.ivWorkshops).setOnClickListener(this);
        findViewById(R.id.ivResults).setOnClickListener(this);
        ibNotification.setOnClickListener(this);
        ibHamburger.setOnClickListener(this);
        ibWifi.setOnClickListener(this);

        if (Tools.isOnline(this)) {
            checkAppVersion();
        } else {
            showSnackbar();
        }
        getFCMToken();

        sharedPreference.setRunCount(sharedPreference.getRunCount() + 1);
        if (sharedPreference.getRunCount() == 4) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initLocationSend();
                }
            }, 3000);
        }

        //TODO
        ivBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.medha.avinder.uietianshub")));
            }
        });
    }

    /**
     * This method fetches contents' versions from Firebase Server.
     * If syllabus version conflict occurs, fetch latest syllabus.
     */
    private void fetchContentVersions() {
        Call<Map<String, String>> call = Tools.getApi(getApplicationContext()).getContentVersions();
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (response.body() != null) {
                    String[] titles = response.body().get("titles").split("~");
                    String[] versions = response.body().get("versions").split("~");
                    for (int j = 0; j < titles.length; j++) {
                        hmVersions.put(titles[j], Integer.parseInt(versions[j]));
                    }
                    if (hmVersions.containsKey("Syllabus")) {
                        int syllabusLatestVersion = hmVersions.get("Syllabus");
                        if (syllabusLatestVersion != sharedPreference.getSyllabusVersion()) {
                            fetchSyllabus(syllabusLatestVersion);
                        }
                    } else {
                        fetchSyllabus(0);
                    }
                } else {
                    fetchSyllabus(0);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
            }
        });
    }

    /**
     * This method fetches syllabus from Firebase Server and store in SQLite.
     */
    private void fetchSyllabus(final int syllabusLatestVersion) {
        isSyllabusLoading = true;

        Call<Syllabus> call = Tools.getApi(getApplicationContext()).getSyllabus(branch + sem);
        call.enqueue(new Callback<Syllabus>() {
            @Override
            public void onResponse(@NonNull Call<Syllabus> call, @NonNull Response<Syllabus> response) {
                if (response.body() != null){
                    Syllabus syllabus = response.body();
                    syllabus.setBranchSem(branch + sem);

                    int rows = db.updateSyllabus(syllabus);
                    if (rows == 0) {
                        db.insertSyllabus(syllabus);
                    }
                    if (syllabusLatestVersion != 0) {
                        sharedPreference.setSyllabusVersion(syllabusLatestVersion);
                    }

                    isSyllabusLoading = false;
                    if (pdSyllabusLoad.isShowing()) {
                        pdSyllabusLoad.dismiss();
                        startActivity(new Intent(ActivityMain.this, ActivitySyllabus.class));
                    }
                } else {
                    Log.d("ActivityMain","fetchSyllabus, body = null");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Syllabus> call, @NonNull Throwable t) {
            }
        });
    }

    private void getBannerImage() {
        Call<String> call = Tools.getApi(getApplicationContext()).getBannerImage();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.body() != null) {
                    Glide.with(getApplicationContext()).load(response.body()).into(ivBanner);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_profile) {
            startActivity(new Intent(ActivityMain.this, ActivityMyProfile.class));
        } else if (id == R.id.nav_about_us) {
            Intent intent = new Intent(ActivityMain.this, ActivityAboutUs.class);
            intent.putExtra("About Privacy", true);
            startActivity(intent);
        } else if (id == R.id.nav_rate_us) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.medha.avinder.uietianshub")));
        } else if (id == R.id.nav_other_apps) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=6781234941494828520")));
        } else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "https://play.google.com/store/apps/details?id=com.medha.avinder.uietianshub";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "UIETians Hub");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else if (id == R.id.nav_leave_performa) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/open?id=0B0NowMBDpqkqUndWNHFCU21yd3M")));
        } else if (id == R.id.nav_sign_out) {
            auth.signOut();
        } else if (id == R.id.nav_privacy_policy) {
            startActivity(new Intent(ActivityMain.this, ActivityAboutUs.class));
        } else if (id == R.id.nav_exit) {
            finish();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == ibHamburger.getId()) {
            drawer.openDrawer(GravityCompat.START);
        } else if (view.getId() == ibWifi.getId()) {
            startActivity(new Intent(ActivityMain.this, ActivityPuWifi.class));
        } else if (view.getId() == ibNotification.getId()) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            tvBadge.setVisibility(View.GONE);
            startActivity(new Intent(ActivityMain.this, ActivityNoticeBoard.class));
        } else if (view.getId() == R.id.ivSyllabus) {
            if (isSyllabusLoading) {
                pdSyllabusLoad.show();
            } else {
                startActivity(new Intent(ActivityMain.this, ActivitySyllabus.class));
            }
        } else if (view.getId() == R.id.ivQuestionPapers) {
            startActivity(new Intent(ActivityMain.this, ActivityQuestionPapers.class));
        } else if (view.getId() == R.id.ivWorkshops) {
            Intent intent = new Intent(ActivityMain.this, ActivityWorkshops.class);
            startActivity(intent);
        } else if (view.getId() == R.id.ivGallery) {
            startActivity(new Intent(ActivityMain.this, ActivityGallery.class));
        } else if (view.getId() == R.id.ivFaculty) {
            startActivity(new Intent(ActivityMain.this, ActivityFaculty.class));
        } else if (view.getId() == R.id.ivCommittees) {
            startActivity(new Intent(ActivityMain.this, ActivityCommittees.class));
        } else if (view.getId() == R.id.ivResults) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://eakadamik.in/")));
        }
    }

    private void checkAppVersion(){
        Call<Long> call = Tools.getApi(getApplicationContext()).getAppVersion();
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                if (response.body() != null) {
                    long latestVersion = response.body();
                    if (Tools.getAppVersion(getApplicationContext()) < latestVersion) {
                        AlertDialog dialog = new AlertDialog.Builder(ActivityMain.this)
                                .setTitle("New version available")
                                .setMessage("Please update your app to stay up to date.")
                                .setCancelable(false)
                                .setPositiveButton("Update Now",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.medha.avinder.uietianshub")));
                                            }
                                        }).setNegativeButton("Update Later",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).create();
                        dialog.show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
            }
        });
    }

    /**
     * Initiate location sending
     */
    private void initLocationSend() {
        count = 0;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = new SimpleDateFormat("dd.MM.yyyy 'at' h:mm:ss a", Locale.getDefault()).format(new Date());

                sendLocationToServer();
            }
        };

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        startLocationSending();
    }

    public void startLocationSending() {
        Dexter.withActivity(ActivityMain.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(ActivityMain.this)
                                    .setTitle("Permissions required")
                                    .setMessage("Please allow the camera, storage and location permissions in settings.")
                                    .setCancelable(false)
                                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            openSettings();
                                        }
                                    });
                            alertDialog.show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(ActivityMain.this, 100);
                                } catch (IntentSender.SendIntentException sie) {
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        }
                    }
                });
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e("Avi", "User agreed to make required location settings changes.");
                        startLocationSending();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e("Avi", "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    private void sendLocationToServer() {
        if (mCurrentLocation != null) {
            Map<String, String> map = new HashMap<>();
            map.put("location", mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
            map.put("updateTime", mLastUpdateTime);
            setLocation(map);
            if (count == 0){
                count++;
            } else {
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
        }
    }

    private void setLocation(Map<String, String> map){
        Call<Boolean> call = Tools.getApi(getApplicationContext()).setLocation(db.getProfile().getEmail(), map);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
            }
        });
    }

    /**
     * This methods gets the FCM token and sends it to server.
     */
    private void getFCMToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        if (task.getResult() != null) {
                            String token = task.getResult().getToken();
                            Call<Boolean> call = Tools.getApi(getApplicationContext()).setToken(db.getProfile().getEmail(), token);
                            call.enqueue(new Callback<Boolean>() {
                                @Override
                                public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                                }

                                @Override
                                public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                                }
                            });
                        }
                    }
                });
    }

    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("dataNotification"));
        Tools.clearNotifications(getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.constraintLayout), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(ActivityMain.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading Syllabus");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }
}
