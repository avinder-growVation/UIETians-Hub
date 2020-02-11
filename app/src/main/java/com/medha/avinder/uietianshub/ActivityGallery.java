package com.medha.avinder.uietianshub;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import com.medha.avinder.uietianshub.adapters.AdapterGallery;
import com.medha.avinder.uietianshub.models.Gallery;
import com.medha.avinder.uietianshub.utils.FilePath;
import com.medha.avinder.uietianshub.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityGallery extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd, fabSort;

    private PopupWindow popupWindow;
    public boolean popWindowToggle = false;

    private AdapterGallery adapterGallery;
    private ArrayList<String> monthList;
    public static ArrayList<Gallery> galleryList = new ArrayList<>();

    private ProgressDialog pdLoading;

    // Add Paper fields
    private ImageView ivUpload;
    private TextView  btnUpload, tvFilename;

    private Uri uriImage;
    private Dialog dialog;
    private ProgressDialog pdUploading;
    private StorageReference storageReference;

    private String masterToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        init();
    }

    private void init() {
        ((TextView) findViewById(R.id.tvTitle)).setText("Gallery");

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        fabSort = findViewById(R.id.fabSort);

        monthList = new ArrayList<>();

        adapterGallery = new AdapterGallery(this, monthList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterGallery);

        pdLoading = getProgressDialog();
        if (Tools.isOnline(this)) {
            fetchGallery();
        } else {
            showSnackbar();
        }

        fabSort.setVisibility(View.VISIBLE);
        fabAdd.setVisibility(View.VISIBLE);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popWindowToggle = false;
                }

                showDialogAdd();
            }
        });

        fabSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popWindowToggle) {
                    popupWindow.dismiss();
                    popWindowToggle = false;
                } else {
                    LayoutInflater layoutInflater = (LayoutInflater) ActivityGallery.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    if (layoutInflater != null) {
                        View customView = layoutInflater.inflate(R.layout.dialog_sort, null);
                        popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        int margin = getResources().getDimensionPixelOffset(R.dimen.margin_100_dp);
                        popupWindow.showAtLocation(findViewById(R.id.clMain), Gravity.BOTTOM | Gravity.END, margin, margin);
                        popWindowToggle = true;

                        customView.findViewById(R.id.tvLatestFirst).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                popupWindow.dismiss();
                                popWindowToggle = false;

                                sortList(true);
                            }
                        });

                        customView.findViewById(R.id.tvOldestFirst).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                popupWindow.dismiss();
                                popWindowToggle = false;

                                sortList(false);
                            }
                        });
                    }
                }
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
     * This method fetches gallery from Firebase Server.
     */
    private void fetchGallery() {
        pdLoading.show();

        Call<JsonArray> call = Tools.getApi(getApplicationContext()).getGallery();
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                if (response.body() != null){
                    galleryList = new ArrayList<>();

                    JsonArray jsonArray = response.body();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        if (!jsonArray.get(i).toString().equals("null")) {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                            Gallery gallery = new Gallery();
                            gallery.setId(i + 1);
                            gallery.setTimestamp(jsonObject.get("timestamp").getAsString());
                            gallery.setLink(jsonObject.get("link").getAsString());

                            galleryList.add(gallery);
                        }
                        sortList(true);
                    }
                } else {
                    Toast.makeText(ActivityGallery.this, "No images available yet. Would be uploaded soon", Toast.LENGTH_SHORT).show();
                }
                pdLoading.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                pdLoading.dismiss();
                if (t.getMessage().equals("Expected a com.google.gson.JsonArray but was com.google.gson.JsonNull")) {
                    Toast.makeText(ActivityGallery.this, "No images available yet. Would be uploaded soon", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityGallery.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sortList(boolean latestFirst) {
        if (!latestFirst) {
            Collections.sort(galleryList, Collections.reverseOrder(new Comparator<Gallery>() {
                @Override
                public int compare(Gallery o1, Gallery o2) {
                    return o1.getTimestamp().compareTo(o2.getTimestamp());
                }
            }));
        } else {
            Collections.sort(galleryList, new Comparator<Gallery>() {
                @Override
                public int compare(Gallery o1, Gallery o2) {
                    return o1.getTimestamp().compareTo(o2.getTimestamp());
                }
            });
        }

        monthList.clear();
        for (Gallery gallery : galleryList) {
            try {
                String month = Tools.getMonthFromDate(gallery.getTimestamp());
                if (!monthList.contains(month)) {
                    monthList.add(month);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        adapterGallery.notifyDataSetChanged();
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(ActivityGallery.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clMain), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void onBackPressed() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popWindowToggle = false;
        } else {
            super.onBackPressed();
        }
    }

    private void showDialogAdd() {
        dialog = new Dialog(ActivityGallery.this);
        dialog.setContentView(R.layout.dialog_add_paper);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface arg0) {
                uriImage = null;
            }
        });

        TextView tvNote = dialog.findViewById(R.id.tvNote);
        ivUpload = dialog.findViewById(R.id.ivUpload);
        btnUpload = dialog.findViewById(R.id.btnUpload);
        tvFilename = dialog.findViewById(R.id.tvFilename);
        ivUpload.setImageResource(R.drawable.ic_select_image);

        dialog.findViewById(R.id.spSubject).setVisibility(View.GONE);
        dialog.findViewById(R.id.tvSubject).setVisibility(View.GONE);
        dialog.findViewById(R.id.radioGroup).setVisibility(View.GONE);

        pdUploading = getProgressDialogUploading();

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

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Tools.isOnline(getApplicationContext())) {
                    if (uriImage == null) {
                        Toast.makeText(ActivityGallery.this, "Please select an image to upload", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uploadFile();
                } else {
                    Toast.makeText(ActivityGallery.this, "No internet connection", Toast.LENGTH_SHORT).show();
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

    private void uploadFile() {
        pdUploading.show();
        final String currentDate=  new SimpleDateFormat("dd.MM.yy 'at' h:mm a", Locale.getDefault()).format(new Date());

        storageReference = FirebaseStorage.getInstance().getReference().child("Gallery/" + currentDate.replace(".", "_").replace(" 'at' ", "_").replace(":", "_").replace(" ", "_") + ".jpg");
        storageReference.putFile(uriImage).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                pdUploading.setMessage("Please wait while we upload your image...   " + (int)progress + "/100");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        sendToFirebase(uri.toString(), currentDate);
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

    private void sendToFirebase(String link, String timestamp) {
        Gallery gallery = new Gallery(0, link, timestamp);

        Call<Gallery> call = Tools.getApi(getApplicationContext()).insertGallery(gallery);
        call.enqueue(new Callback<Gallery>() {
            @Override
            public void onResponse(@NonNull Call<Gallery> call, @NonNull Response<Gallery> response) {
                if (masterToken != null) {
                    Tools.sendDataNotification("Upload", "Gallery", masterToken);
                }
                pdUploading.dismiss();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityGallery.this)
                        .setTitle("Success")
                        .setMessage("Your photo has been sent for approval. It will be added soon")
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
            public void onFailure(@NonNull Call<Gallery> call, @NonNull Throwable t) {
                pdUploading.dismiss();
                Toast.makeText(ActivityGallery.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method is called for selecting image from camera or gallery.
     * It checks permissions and acts accordingly.
     */
    private void requestPermissions() {
        Dexter.withActivity(ActivityGallery.this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final CharSequence[] items = {"Take Photo", "Choose from Gallery"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityGallery.this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityGallery.this);
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
                        Glide.with(getApplicationContext()).load(uriImage).into(ivUpload);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityGallery.this);
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
        pdUploading = new ProgressDialog(ActivityGallery.this, R.style.MyAlertDialogStyle);
        pdUploading.setTitle("Uploading");
        pdUploading.setMessage("Please wait while we upload your image");
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
