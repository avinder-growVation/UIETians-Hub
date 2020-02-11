package com.medha.avinder.uietianshub;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.medha.avinder.uietianshub.adapters.AdapterNotification;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.models.Timeline;
import com.medha.avinder.uietianshub.utils.FilePath;
import com.medha.avinder.uietianshub.utils.RecyclerTouchListener;
import com.medha.avinder.uietianshub.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Avinder on 02-01-2018.
 */

public class ActivityNoticeBoard extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;

    private DatabaseHelper db;

    private AdapterNotification adapterNotification;
    private ArrayList<Timeline> notificationList = new ArrayList<>();

    private ProgressDialog pdLoading;

    private BroadcastReceiver broadcastReceiver;

    // Add Paper fields
    private ImageView ivUpload;
    private TextView btnSend, tvNote, tvFilename;
    private EditText etTitle, etDetails;

    private Uri uriImage;
    private Dialog dialog;
    private ProgressDialog pdUploading;
    private StorageReference storageReference;

    private String masterToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        db = new DatabaseHelper(this);

        init();
    }

    private void init() {
        ((TextView)findViewById(R.id.tvTitle)).setText("Notice Board");

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        fabAdd.setVisibility(View.VISIBLE);

        notificationList = new ArrayList<>();

        adapterNotification = new AdapterNotification(this, notificationList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapterNotification);

        pdLoading = getProgressDialog();
        if (Tools.isOnline(this)) {
            fetchNotifications();
        } else {
            showSnackbar();
        }
        getNotifications();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("dataNotification")){
                    String title = intent.getStringExtra("Title");
                    if (!title.equals("Location")) {
                        fetchNotifications();
                    }
                }
            }
        };

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(ActivityNoticeBoard.this,ActivityTimeline.class);
                intent.putExtra("Position", position);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogSend();
            }
        });

        findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * This method fetches notifications from Firebase Server and store in SQLite.
     */
    private void fetchNotifications() {
        pdLoading.show();

        Call<JsonArray> call = Tools.getApi(getApplicationContext()).getNotifications();
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                if (response.body() != null){
                    JsonArray jsonArray = response.body();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        if (!jsonArray.get(i).toString().equals("null")) {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                            Timeline timeline = new Timeline();
                            timeline.setId(i + 1);
                            timeline.setTitle(jsonObject.get("title").getAsString());
                            if (jsonObject.get("details") != null) {
                                timeline.setDetails(jsonObject.get("details").getAsString());
                            }
                            if (jsonObject.get("image") != null) {
                                timeline.setImage(jsonObject.get("image").getAsString());
                            }
                            timeline.setTimestamp(jsonObject.get("timestamp").getAsString());

                            int rows = db.updateNotification(timeline);
                            if (rows == 0) {
                                db.insertNotification(timeline);
                            }
                        }
                        getNotifications();
                    }
                } else {
                    Toast.makeText(ActivityNoticeBoard.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                pdLoading.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                pdLoading.dismiss();
                Toast.makeText(ActivityNoticeBoard.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method fetches notifications name from SQLite.
     */
    private void getNotifications() {
        notificationList.clear();
        adapterNotification.notifyDataSetChanged();

        ArrayList<Timeline> list = new ArrayList<>(db.getAllNotifications());
        int count = 0;
        for (Timeline timeline : list) {
            notificationList.add(timeline);
            adapterNotification.notifyItemInserted(count);
            count++;
        }
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(ActivityNoticeBoard.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading Notifications");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clMain), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("dataNotification"));
        Tools.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    private void showDialogSend() {
        dialog = new Dialog(ActivityNoticeBoard.this);
        dialog.setContentView(R.layout.dialog_add_notification);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface arg0) {
                uriImage = null;
            }
        });

        tvNote = dialog.findViewById(R.id.tvNote);
        ivUpload = dialog.findViewById(R.id.ivUpload);
        btnSend = dialog.findViewById(R.id.btnSend);
        tvFilename = dialog.findViewById(R.id.tvFilename);
        etDetails = dialog.findViewById(R.id.etDetails);
        etTitle = dialog.findViewById(R.id.etTitle);
        pdUploading = getProgressDialogUploading();
        ivUpload.setImageResource(R.drawable.ic_select_image);

        String first = "Note:";
        String next = " You will be notified once your notification is approved";
        tvNote.setText(first + next, TextView.BufferType.SPANNABLE);
        Spannable spannable = (Spannable)tvNote.getText();
        int start = first.length();
        int end = start + next.length();
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorBlack)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etTitle.getText().toString().trim().length() == 0) {
                    Toast.makeText(ActivityNoticeBoard.this, "Add a title for this notification", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Tools.isOnline(getApplicationContext())) {
                    pdUploading.show();
                    final String currentDate =  new SimpleDateFormat("dd MMM yyyy 'at' h:mm a", Locale.getDefault()).format(new Date());
                    if (uriImage != null) {
                        uploadFile(currentDate);
                    } else {
                        sendToFirebase(null);
                    }
                } else {
                    Toast.makeText(ActivityNoticeBoard.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (dialog.getWindow() != null) {
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = getResources().getDisplayMetrics().widthPixels / 10 * 9;
            dialog.show();
            dialog.getWindow().setAttributes(layoutParams);
        }

        fetchMasterToken();
    }

    private void uploadFile(String currentDate) {
        storageReference = FirebaseStorage.getInstance().getReference().child("Notifications/" + currentDate.replace(".", "_").replace(" 'at' ", "_").replace(":", "_").replace(" ", "_") + ".jpg");
        storageReference.putFile(uriImage).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                pdUploading.setMessage("Please wait while we upload your notice...   " + (int)progress + "/100");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        sendToFirebase(uri.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                pdUploading.dismiss();
                Toast.makeText(getApplicationContext(), "We were unable to upload your paper", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendToFirebase(String link) {
        Timeline timeline = new Timeline();
        timeline.setTitle(etTitle.getText().toString());
        timeline.setTimestamp(new SimpleDateFormat("dd MMM yyyy 'at' h:mm a", Locale.getDefault()).format(new Date()));
        timeline.setSender(db.getProfile().getEmail());
        if (etDetails.getText().toString().trim().length() != 0) {
            timeline.setDetails(etDetails.getText().toString().trim());
        }
        timeline.setImage(link);

        Call<Timeline> call = Tools.getApi(getApplicationContext()).insertNotification(timeline);
        call.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(@NonNull Call<Timeline> call, @NonNull Response<Timeline> response) {
                if (masterToken != null) {
                    Tools.sendDataNotification("Upload", "Notification", masterToken);
                }
                pdUploading.dismiss();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityNoticeBoard.this)
                        .setTitle("Success")
                        .setMessage("Your notification has been sent for approval. It will be added soon")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }

            @Override
            public void onFailure(@NonNull Call<Timeline> call, @NonNull Throwable t) {
                pdUploading.dismiss();
                Toast.makeText(ActivityNoticeBoard.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method is called for selecting image from camera or gallery.
     * It checks permissions and acts accordingly.
     */
    private void requestPermissions() {
        Dexter.withActivity(ActivityNoticeBoard.this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final CharSequence[] items = {"Take Photo", "Choose from Gallery"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityNoticeBoard.this);
                            builder.setTitle("Add Paper");
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items[item].equals("Take Photo")) {
                                        cameraIntent();
                                    } else if (items[item].equals("Choose from Gallery")) {
                                        galleryIntent();
                                    }
                                }
                            });
                            builder.show();
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityNoticeBoard.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permissions to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 0);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            uriImage = data.getData();

            if (uriImage != null) {
                String filename = FilePath.getPath(getApplicationContext(), uriImage);
                if (filename != null) {
                    if (filename.equals("File selected successfully")) {
                        Glide.with(getApplicationContext()).load(R.drawable.ic_selected_image).into(ivUpload);
                    } else {
                        filename = filename.substring(filename.lastIndexOf("/") + 1);

                        String[] imageExtensions = new String[]{"jpg", "png", "gif", "jpeg", "webp"};
                        for (String extension : imageExtensions) {
                            if (filename.toLowerCase().endsWith(extension)) {
                                try {
                                    Bitmap bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                                    Glide.with(getApplicationContext()).load(bm).into(ivUpload);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    tvFilename.setVisibility(View.VISIBLE);
                    tvFilename.setText(filename);
                }
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        Glide.with(getApplicationContext()).load(thumbnail).into(ivUpload);
        uriImage = getImageUri(getApplicationContext(), thumbnail);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.WEBP, 70, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 100:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e("Avi", "User agreed to make required location settings changes.");
                        final CharSequence[] items = {"Take Photo", "Choose from Gallery"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityNoticeBoard.this);
                        builder.setTitle("Add Paper");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (items[item].equals("Take Photo")) {
                                    cameraIntent();
                                } else if (items[item].equals("Choose from Gallery")) {
                                    galleryIntent();
                                }
                            }
                        });
                        builder.show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e("Avi", "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0)
                onSelectFromGalleryResult(data);
            else if (requestCode == 1)
                onCaptureImageResult(data);
        }
    }

    private ProgressDialog getProgressDialogUploading() {
        pdUploading = new ProgressDialog(ActivityNoticeBoard.this, R.style.MyAlertDialogStyle);
        pdUploading.setTitle("Uploading");
        pdUploading.setMessage("Please wait while we upload your notice");
        pdUploading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdUploading.setCancelable(false);
        pdUploading.setIndeterminate(true);
        return pdUploading;
    }

    private void fetchMasterToken() {
        Call<String> call = Tools.getApi(getApplicationContext()).getMasterToken();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                masterToken = response.body();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            }
        });
    }
}
