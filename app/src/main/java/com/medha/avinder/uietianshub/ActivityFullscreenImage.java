package com.medha.avinder.uietianshub;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.jsibbold.zoomage.ZoomageView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.medha.avinder.uietianshub.utils.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ActivityFullscreenImage extends AppCompatActivity {

    private ZoomageView ivFullscreen;
    private ImageView ivClose, ivDownload;
    private ProgressBar pbLoading;

    private String link, item, paperName, imageName;

    private ProgressDialog pdDownload;

    private DownloadFileToDevice downloadClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        link = getIntent().getStringExtra("Link");
        item = getIntent().getStringExtra("Item");
        paperName = getIntent().getStringExtra("Paper Name");
        imageName = getIntent().getStringExtra("Image Name");

        init();
    }

    private void init() {
        ivFullscreen = findViewById(R.id.ivFullscreen);
        ivClose = findViewById(R.id.ivClose);
        ivDownload = findViewById(R.id.ivDownload);

        ivFullscreen.setBackgroundColor(Color.BLACK);
        ivClose.setBackgroundResource(R.drawable.ic_close_white);
        ivDownload.setBackgroundResource(R.drawable.ic_download_white);
        pbLoading = findViewById(R.id.pbLoading);

        pbLoading.setVisibility(View.VISIBLE);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ivDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions();
            }
        });

        if (item.equals("Committee")) {
            ivDownload.setVisibility(View.GONE);
        }

        Glide.with(this).asBitmap().load(link).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                ivFullscreen.setImageBitmap(resource);
                pbLoading.setVisibility(View.GONE);
                ivClose.setVisibility(View.VISIBLE);
                ivDownload.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * This class checks if file exists in directory, it creates directory and open pdf Viewer app.
     */
    private class DownloadFileToDevice extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdDownload.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected Void doInBackground(String... strings) {
            switch (item) {
                case "Paper":
                    if (Tools.createDirIfNotExists("UIETians Hub/Question Papers")) {
                        File file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Question Papers/" + paperName + ".pdf");
                        getInputStreamFromURL(file);
                    }
                    break;
                case "Gallery":
                    if (Tools.createDirIfNotExists("UIETians Hub/Gallery")) {
                        File file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Gallery/" + imageName + ".jpg");
                        getInputStreamFromURL(file);
                    } else {
                        Log.e("AVi", "cant create folder");
                    }
                    break;
                case "Notice":
                    if (Tools.createDirIfNotExists("UIETians Hub/Notifications")) {
                        File file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Notifications/" + imageName + ".jpg");
                        getInputStreamFromURL(file);
                    }
                    break;
            }

            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getInputStreamFromURL(File file) {
        if (!Tools.isOnline(getApplicationContext())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showSnackbar();
                }
            });
        } else {
            try {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                int lengthOfFile = connection.getContentLength();

                long total = 0;
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    total += length;
                    pdDownload.setProgress((int) ((total * 100) / lengthOfFile));
                    outputStream.write(buffer, 0, length);
                }

                pdDownload.dismiss();
                outputStream.close();
                inputStream.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ActivityFullscreenImage.this, "File saved on device", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void requestPermissions() {
        Dexter.withActivity(ActivityFullscreenImage.this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            if (Tools.isOnline(getApplicationContext())) {
                                pdDownload = createProgressDialog();
                                downloadClass = new DownloadFileToDevice();
                                downloadClass.execute();
                            } else {
                                showSnackbar();
                            }
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityFullscreenImage.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("Allow the storage permission in settings to download.");
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (Tools.isOnline(getApplicationContext())) {
                            pdDownload = createProgressDialog();
                            downloadClass = new DownloadFileToDevice();
                            downloadClass.execute();
                        } else {
                            showSnackbar();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e("Avi", "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog pdDownload = new ProgressDialog(ActivityFullscreenImage.this, R.style.MyAlertDialogStyle);
        pdDownload.setMessage("Downloading...");
        pdDownload.setIndeterminate(false);
        pdDownload.setMax(100);
        pdDownload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pdDownload.setCancelable(false);
        pdDownload.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadClass.cancel(true);
            }
        });
        return pdDownload;
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
