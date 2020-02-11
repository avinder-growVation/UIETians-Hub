package com.medha.avinder.uietianshub;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jsibbold.zoomage.ZoomageView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.medha.avinder.uietianshub.data.SharedPreference;
import com.medha.avinder.uietianshub.utils.Tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ActivityPdfViewer extends AppCompatActivity {

    private TextView tvPageNumber, tvProgress;
    private ImageView ivLeftArrow, ivRightArrow, ivClose, ivDownload, ivImages;
    private ZoomageView ivFullscreen;
    private ProgressBar pbLoading;

    private SharedPreference sharedPreference;

    private String branchSem, link, pages, item, paperName, workshopName, facultyName;
    private boolean downloadable;

    private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";
    private ParcelFileDescriptor fileDescriptor;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page page;
    private int pageIndex;

    private ProgressDialog pdDownload;

    private DownloadFileToDevice downloadClass;
    private File file;

    //Connection related fields
    private int total;
    private int lengthOfFile;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        sharedPreference = new SharedPreference(this);

        branchSem = getIntent().getStringExtra("BranchSem");
        link = getIntent().getStringExtra("Link");
        pages = getIntent().getStringExtra("Pages");
        item = getIntent().getStringExtra("Item");
        paperName = getIntent().getStringExtra("Paper Name");
        workshopName = getIntent().getStringExtra("Workshop Name");
        downloadable = getIntent().getBooleanExtra("Downloadable", false);
        facultyName = getIntent().getStringExtra("Faculty Name");

        pageIndex = 0;
        if (savedInstanceState != null) {
            pageIndex = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
        }

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init() {
        setContentView(R.layout.activity_pdf_viewer);

        ivFullscreen = findViewById(R.id.ivFullscreen);
        ivLeftArrow = findViewById(R.id.ivLeftArrow);
        ivRightArrow = findViewById(R.id.ivRightArrow);
        ivClose = findViewById(R.id.ivClose);
        ivDownload = findViewById(R.id.ivDownload);
        ivImages = findViewById(R.id.ivImages);
        pbLoading = findViewById(R.id.pbLoading);
        tvPageNumber = findViewById(R.id.tvPageNumbers);
        tvProgress = findViewById(R.id.tvProgress);

        pbLoading.setVisibility(View.VISIBLE);
        tvProgress.setVisibility(View.VISIBLE);

        if (item.equals("Workshop")) {
            if (!downloadable) {
                ivDownload.setVisibility(View.GONE);
            }
            ivImages.setVisibility(View.VISIBLE);
        }

        ivLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pbLoading.setVisibility(View.VISIBLE);
                ivFullscreen.setVisibility(View.GONE);
                new ShowPage().execute(page.getIndex() - 1);
            }
        });
        ivRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pbLoading.setVisibility(View.VISIBLE);
                ivFullscreen.setVisibility(View.GONE);
                new ShowPage().execute(page.getIndex() + 1);
            }
        });
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
        ivImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityPdfViewer.this, ActivityWorkshopImages.class);
                intent.putExtra("Workshop", workshopName);
                startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onStart() {
        super.onStart();
        switch (item) {
            case "Syllabus":
                new GetSyllabus().execute();
                break;
            case "Paper":
                new GetPaper().execute();
                break;
            case "Workshop":
                new GetWorkshop().execute();
                break;
            case "Faculty":
                new GetFaculty().execute();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onStop() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (page != null) {
            outState.putInt(STATE_CURRENT_PAGE_INDEX, page.getIndex());
            if (item.equals("Syllabus")) {
                outState.putInt(STATE_CURRENT_PAGE_INDEX, page.getIndex() - getStartEndPages()[0] + 1);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class GetSyllabus extends AsyncTask<String, Void, Void> {
        private File file;
        @Override
        protected Void doInBackground(String... strings) {
            file = new File(getCacheDir(), "syllabus");
            if (file.exists() && sharedPreference.getCurrentSyllabusInCache() != null && sharedPreference.getCurrentSyllabusInCache().equals(branchSem.substring(0,3))) {
                initPdf(file);
            } else {
                if (hasPermissions()) {
                    file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Syllabus/" + branchSem.substring(0, 3) + ".pdf");
                    if (file.exists()) {
                        initPdf(file);
                    } else {
                        file = new File(getCacheDir(), "syllabus");
                        getInputStreamFromURL(file, true);
                    }
                } else {
                    file = new File(getCacheDir(), "syllabus");
                    getInputStreamFromURL(file, true);
                }
            }

            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class GetPaper extends AsyncTask<String, Void, Void> {
        private File file;
        @Override
        protected Void doInBackground(String... strings) {
            file = new File(getCacheDir(), "question_paper");
            if (file.exists() && sharedPreference.getCurrentPaperInCache() != null && sharedPreference.getCurrentPaperInCache().equals(paperName)) {
                initPdf(file);
            } else {
                if (hasPermissions()) {
                    file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Question Papers/" + paperName + ".pdf");
                    if (file.exists()) {
                        initPdf(file);
                    } else {
                        file = new File(getCacheDir(), "question_paper");
                        getInputStreamFromURL(file, true);
                    }
                } else {
                    file = new File(getCacheDir(), "question_paper");
                    getInputStreamFromURL(file, true);
                }
            }
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class GetWorkshop extends AsyncTask<String, Void, Void> {
        private File file;
        @Override
        protected Void doInBackground(String... strings) {
            file = new File(getCacheDir(), "workshop");
            if (file.exists() && sharedPreference.getCurrentWorkshopInCache() != null && sharedPreference.getCurrentWorkshopInCache().equals(workshopName)) {
                initPdf(file);
            } else {
                if (hasPermissions()) {
                    file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Workshops/" + workshopName + ".pdf");
                    if (file.exists()) {
                        initPdf(file);
                    } else {
                        file = new File(getCacheDir(), "workshop");
                        getInputStreamFromURL(file, true);
                    }
                } else {
                    file = new File(getCacheDir(), "workshop");
                    getInputStreamFromURL(file, true);
                }
            }
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class GetFaculty extends AsyncTask<String, Void, Void> {
        private File file;
        @Override
        protected Void doInBackground(String... strings) {
            file = new File(getCacheDir(), "faculty");
            if (file.exists() && sharedPreference.getCurrentFacultyInCache() != null && sharedPreference.getCurrentFacultyInCache().equals(facultyName)) {
                initPdf(file);
            } else {
                if (hasPermissions()) {
                    file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Faculty/" + facultyName + ".pdf");
                    if (file.exists()) {
                        initPdf(file);
                    } else {
                        file = new File(getCacheDir(), "faculty");
                        getInputStreamFromURL(file, true);
                    }
                } else {
                    file = new File(getCacheDir(), "faculty");
                    getInputStreamFromURL(file, true);
                }
            }
            return null;
        }
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
            if (item.equals("Syllabus")) {
                if (Tools.createDirIfNotExists("UIETians Hub/Syllabus")) {
                    file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Syllabus/" + branchSem.substring(0,3) + ".pdf");
                    getInputStreamFromURL(file, false);
                }
            } else if (item.equals("Paper")){
                if (Tools.createDirIfNotExists("UIETians Hub/Question Papers")) {
                    file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Question Papers/" + paperName + ".pdf");
                    getInputStreamFromURL(file, false);
                }
            } else if (item.equals("Workshop")){
                if (Tools.createDirIfNotExists("UIETians Hub/Workshops")) {
                    file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Workshops" + workshopName + ".pdf");
                    getInputStreamFromURL(file, false);
                }
            } else if (item.equals("Faculty")){
                if (Tools.createDirIfNotExists("UIETians Hub/Faculty")) {
                    file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Faculty" + facultyName + ".pdf");
                    getInputStreamFromURL(file, false);
                }
            }
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getInputStreamFromURL(File file, boolean view) {
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
                lengthOfFile = connection.getContentLength();

                total = 0;
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    total += length;
                    if (!view) {
                        pdDownload.setProgress(((total * 100) / lengthOfFile));
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvProgress.setText(((total * 100) / lengthOfFile) + " %");
                            }
                        });
                    }
                    outputStream.write(buffer, 0, length);
                }
                if (!view) {
                    pdDownload.dismiss();
                }
                outputStream.close();
                inputStream.close();

                if (view) {
                    initPdf(file);

                    switch (item) {
                        case "Syllabus":
                            sharedPreference.setCurrentSyllabusInCache(branchSem.substring(0, 3));
                            break;
                        case "Paper":
                            sharedPreference.setCurrentPaperInCache(paperName);
                            break;
                        case "Workshop":
                            sharedPreference.setCurrentWorkshopInCache(workshopName);
                            break;
                        case "Faculty":
                            sharedPreference.setCurrentFacultyInCache(facultyName);
                            break;
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ActivityPdfViewer.this, "File saved on device", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initPdf(final File file) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                    if (fileDescriptor != null) {
                        pbLoading.setVisibility(View.GONE);
                        ivClose.setVisibility(View.VISIBLE);
                        ivDownload.setVisibility(View.VISIBLE);
                        ivLeftArrow.setVisibility(View.VISIBLE);
                        ivRightArrow.setVisibility(View.VISIBLE);
                        tvPageNumber.setVisibility(View.VISIBLE);
                        tvProgress.setVisibility(View.GONE);
                        if (item.equals("Workshop")) {
                            ivImages.setVisibility(View.VISIBLE);
                        } else {
                            ivImages.setVisibility(View.GONE);
                        }
                        pdfRenderer = new PdfRenderer(fileDescriptor);
                        if (item.equals("Syllabus")) {
                            new ShowPage().execute(getStartEndPages()[0] - 1 + pageIndex);
                        } else {
                            new ShowPage().execute(pageIndex);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivClose.setVisibility(View.GONE);
                            ivDownload.setVisibility(View.GONE);
                            ivLeftArrow.setVisibility(View.GONE);
                            ivRightArrow.setVisibility(View.GONE);
                            ivImages.setVisibility(View.GONE);
                            tvPageNumber.setVisibility(View.GONE);
                            tvProgress.setVisibility(View.GONE);
                            Toast.makeText(ActivityPdfViewer.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivClose.setVisibility(View.GONE);
                            ivDownload.setVisibility(View.GONE);
                            ivLeftArrow.setVisibility(View.GONE);
                            ivRightArrow.setVisibility(View.GONE);
                            ivImages.setVisibility(View.GONE);
                            tvPageNumber.setVisibility(View.GONE);
                            tvProgress.setVisibility(View.GONE);
                            Toast.makeText(ActivityPdfViewer.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeRenderer() throws IOException {
        if (pdfRenderer != null) {
            pdfRenderer.close();
            fileDescriptor.close();
        }
    }

    private class ShowPage extends AsyncTask<Integer, Void, Void> {
        private Bitmap bitmap;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected Void doInBackground(Integer... integers) {
            page = pdfRenderer.openPage(integers[0]);
            bitmap = Bitmap.createBitmap(page.getWidth() * 3, page.getHeight() * 3, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ivFullscreen.setImageBitmap(bitmap);
            pbLoading.setVisibility(View.GONE);
            tvProgress.setVisibility(View.GONE);
            ivFullscreen.setVisibility(View.VISIBLE);
            if (item.equals("Syllabus")) {
                int[] ii = getStartEndPages();
                tvPageNumber.setText((page.getIndex() - ii[0] + 2) + "/" + (ii[1] - ii[0] + 1));
            } else {
                tvPageNumber.setText((page.getIndex() + 1) + "/" + pdfRenderer.getPageCount());
            }
            updateUi();
        }
    }

    /**
     * Updates the state of 2 control buttons in response to the current page index.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateUi() {
        int index = page.getIndex();
        if (index == getStartEndPages()[0] - 1) {
            ivLeftArrow.setEnabled(false);
            ivLeftArrow.setAlpha(0.2f);
        } else {
            ivLeftArrow.setEnabled(true);
            ivLeftArrow.setAlpha(1.0f);
        }
        if (index == getStartEndPages()[1] - 1) {
            ivRightArrow.setEnabled(false);
            ivRightArrow.setAlpha(0.2f);
        } else {
            ivRightArrow.setEnabled(true);
            ivRightArrow.setAlpha(1.0f);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int[] getStartEndPages() {
        if (item.equals("Syllabus")) {
            String[] strings = pages.split("-");
            return new int[] {Integer.parseInt(strings[0]), Integer.parseInt(strings[1])};
        } else {
            return new int[] {1, pdfRenderer.getPageCount()};
        }
    }

    public void requestPermissions() {
        Dexter.withActivity(ActivityPdfViewer.this)
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPdfViewer.this);
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

    private boolean hasPermissions() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        boolean hasAllPermissions = true;
        for(String permission : permissions) {
            if (ContextCompat.checkSelfPermission(ActivityPdfViewer.this, permission) != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false;
            }
        }
        return hasAllPermissions;
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog pdDownload = new ProgressDialog(ActivityPdfViewer.this, R.style.MyAlertDialogStyle);
        pdDownload.setMessage("Downloading...");
        pdDownload.setIndeterminate(false);
        pdDownload.setMax(100);
        pdDownload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pdDownload.setCancelable(false);
        pdDownload.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadClass.cancel(true);
                file.delete();
            }
        });
        return pdDownload;
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}