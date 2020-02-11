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
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import com.medha.avinder.uietianshub.adapters.AdapterQuestionPaper;
import com.medha.avinder.uietianshub.adapters.AdapterSpinner;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.models.QuestionPaper;
import com.medha.avinder.uietianshub.utils.FilePath;
import com.medha.avinder.uietianshub.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityQuestionPapers extends AppCompatActivity {
    private ConstraintLayout constraintLayout;
    private LinearLayout linearLayout;
    private RecyclerView recyclerView;
    private TextView btnMajor, btnMinor;
    private Spinner spSemester;
    private FloatingActionButton fabAdd, fabSort;

    public static PopupWindow popupWindow;
    public static boolean popWindowToggle = false;

    private DatabaseHelper db;

    private AdapterQuestionPaper adapterQuestionPaper;
    private ArrayList<QuestionPaper> papersList = new ArrayList<>();
    private ArrayList<QuestionPaper> allPapersList = new ArrayList<>();
    private String[] subjectsArray = null;

    private String branch;
    private int sem;

    private boolean isSubjectsListView = true;
    private boolean isMajor = true;

    private String spSelectedSemester;
    private String subjectTitle = null;

    private ProgressDialog pdLoading;

    // Add Paper fields
    private Spinner spSubject;
    private ImageView ivUpload;
    private TextView tvSubject, tvFilename, btnUpload;

    private String spSelectedSubject;
    private int majorMinor;
    private Uri uriPaper;

    private Dialog dialog;
    private ProgressDialog pdUploading;
    private StorageReference storageReference;

    private String masterToken;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllabus);

        ((TextView) findViewById(R.id.tvTitle)).setText("Question Papers");

        db = new DatabaseHelper(this);

        String branchSem = db.getProfile().getBranchSem();
        if (branchSem.substring(0,2).equals("It")) {
            branch = "It";
            sem = Integer.parseInt(branchSem.substring(2));
        } else {
            branch = branchSem.substring(0, 3);
            sem = Integer.parseInt(branchSem.substring(3));
        }

        init();
    }

    private void init() {
        fabAdd = findViewById(R.id.fabAdd);
        fabSort = findViewById(R.id.fabSort);

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
                    LayoutInflater layoutInflater = (LayoutInflater) ActivityQuestionPapers.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    if (layoutInflater != null) {
                        View customView = layoutInflater.inflate(R.layout.dialog_sort, null);
                        popupWindow = new PopupWindow(customView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        int margin = getResources().getDimensionPixelOffset(R.dimen.margin_100_dp);
                        popupWindow.showAtLocation(linearLayout, Gravity.BOTTOM | Gravity.END, margin, margin);
                        popWindowToggle = true;

                        customView.findViewById(R.id.tvLatestFirst).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                popupWindow.dismiss();
                                popWindowToggle = false;
                                Collections.sort(papersList, Collections.reverseOrder(new Comparator<QuestionPaper>() {
                                    @Override
                                    public int compare(QuestionPaper o1, QuestionPaper o2) {
                                        return o1.getTimestamp().compareTo(o2.getTimestamp());
                                    }
                                }));
                                adapterQuestionPaper.notifyDataSetChanged();
                            }
                        });

                        customView.findViewById(R.id.tvOldestFirst).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                popupWindow.dismiss();
                                popWindowToggle = false;
                                Collections.sort(papersList, new Comparator<QuestionPaper>() {
                                    @Override
                                    public int compare(QuestionPaper o1, QuestionPaper o2) {
                                        return o1.getTimestamp().compareTo(o2.getTimestamp());
                                    }
                                });
                                adapterQuestionPaper.notifyDataSetChanged();
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

        pdLoading = getProgressDialog();

        showSubjectsList(true);
    }

    /**
     * This method lists the subjects of particular branch_sem after getting them from SQLite.
     */
    private void showSubjectsList(boolean firstOpen) {
        findViewById(R.id.svConstraint).setVisibility(View.GONE);
        findViewById(R.id.svQuestionPapers).setVisibility(View.VISIBLE);
        linearLayout = findViewById(R.id.linearLayout);
        linearLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.rlSemester).setVisibility(View.VISIBLE);
        findViewById(R.id.llToggle).setVisibility(View.GONE);
        fabSort.setVisibility(View.GONE);

        findViewById(R.id.tvSubjectName).setVisibility(View.INVISIBLE);

        spSemester = findViewById(R.id.spSemester);

        isSubjectsListView = true;
        subjectTitle = null;

        final String[] semesters = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};

        AdapterSpinner adapterSubjects = new AdapterSpinner(this, semesters, 2, null);
        spSemester.setAdapter(adapterSubjects);

        if (firstOpen) {
            spSemester.setSelection(sem - 1);
        } else {
            spSemester.setSelection(Integer.parseInt(spSelectedSemester.substring(0,1)) - 1);
        }

        spSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spSelectedSemester = semesters[i];

                //Check if syllabus present in SQLite
                if (spSelectedSemester.substring(0, 1).equals(sem + "")) {
                    subjectsArray = db.getSubjects(branch + spSelectedSemester.substring(0, 1)).split("~");

                    linearLayout.removeAllViews();

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    //Add subjects to list
                    for (int index = 0; index < subjectsArray.length; index++) {
                        final TextView tvSubject = new TextView(getApplicationContext());
                        tvSubject.setLayoutParams(layoutParams);
                        tvSubject.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        tvSubject.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.montserrat_semi_bold));
                        tvSubject.setText(subjectsArray[index]);
                        tvSubject.setGravity(Gravity.CENTER);
                        tvSubject.setTextColor(getResources().getColor(R.color.colorPrimary));
                        int leftPadding = getResources().getDimensionPixelOffset(R.dimen.padding_15_dp);
                        int topPadding = getResources().getDimensionPixelOffset(R.dimen.padding_20_dp);
                        tvSubject.setPadding(leftPadding, topPadding, leftPadding, topPadding);
                        linearLayout.addView(tvSubject);

                        if (index != subjectsArray.length - 1) {
                            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.height_3_dp));
                            View v = new View(getApplicationContext());
                            v.setLayoutParams(layoutParams2);
                            v.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                            linearLayout.addView(v);
                        }

                        tvSubject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showQuestionPapers(tvSubject.getText().toString());
                            }
                        });
                    }
                } else {
                    if (Tools.isOnline(getApplicationContext())) {
                        fetchSubjects(branch + spSelectedSemester.substring(0, 1));
                    } else {
                        showSnackbar();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void showQuestionPapers(final String title) {
        papersList.clear();

        isSubjectsListView = false;
        subjectTitle = title;

        recyclerView = findViewById(R.id.recyclerView);
        btnMajor = findViewById(R.id.btnMajor);
        btnMinor = findViewById(R.id.btnMinor);

        findViewById(R.id.svQuestionPapers).setVisibility(View.GONE);
        findViewById(R.id.svConstraint).setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);                                                      // Hide the subjects list
        findViewById(R.id.rlSemester).setVisibility(View.GONE);                                     // Hide the semester select spinner
        recyclerView.setVisibility(View.VISIBLE);
        findViewById(R.id.llToggle).setVisibility(View.VISIBLE);
        fabSort.setVisibility(View.VISIBLE);

        TextView tvSubjectName = findViewById(R.id.tvSubjectName);
        tvSubjectName.setVisibility(View.VISIBLE);
        tvSubjectName.setText(title);

        constraintLayout = findViewById(R.id.constraintLayout);
        ConstraintSet set = new ConstraintSet();
        set.clone(constraintLayout);
        set.connect(recyclerView.getId(), ConstraintSet.TOP, R.id.llToggle, ConstraintSet.BOTTOM, getResources().getDimensionPixelOffset(R.dimen.padding_20_dp));
        set.applyTo(constraintLayout);

        adapterQuestionPaper = new AdapterQuestionPaper(this, papersList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterQuestionPaper);

        // Save particular sem's papers.
        if (Tools.isOnline(getApplicationContext())) {
            if (spSelectedSemester.substring(0,1).equals(sem + "")) {
                fetchQuestionPapersByTitle(title, true);
            } else {
                fetchQuestionPapersByTitle(title, false);
            }
        } else {
            showSnackbar();
            allPapersList.addAll(db.getQuestionPapers(title));
            addQuestionPapers(0);
        }

        isMajor = true;

        btnMajor.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        btnMajor.setTextColor(Color.WHITE);
        btnMinor.setBackground(null);
        btnMinor.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

        btnMajor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isMajor) {
                    btnMajor.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    btnMajor.setTextColor(Color.WHITE);
                    btnMinor.setBackground(null);
                    btnMinor.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    isMajor = true;
                    addQuestionPapers(0);
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                        popWindowToggle = false;
                    }
                }
            }
        });

        btnMinor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMajor) {
                    btnMinor.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    btnMinor.setTextColor(Color.WHITE);
                    btnMajor.setBackground(null);
                    btnMajor.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    isMajor = false;
                    addQuestionPapers(1);
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                        popWindowToggle = false;
                    }
                }
            }
        });
    }

    /**
     * This method adds question papers to list and notifies the adapter.
     */
    private void addQuestionPapers(int majorMinor) {
        pdLoading.show();
        papersList.clear();
        adapterQuestionPaper.notifyDataSetChanged();
        int count = 1;
        ArrayList<QuestionPaper> clone = (ArrayList<QuestionPaper>)allPapersList.clone();
        for (QuestionPaper questionPaper : (ArrayList<QuestionPaper>)clone.clone()) {
            if (questionPaper.getCredits().equals("Default") && questionPaper.getMajorMinor() == majorMinor) {
                papersList.add(questionPaper);
                clone.remove(questionPaper);
            }
        }

        Collections.sort(clone, Collections.reverseOrder(new Comparator<QuestionPaper>() {
            @Override
            public int compare(QuestionPaper o1, QuestionPaper o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        }));

        for (QuestionPaper questionPaper : clone) {
            if (!papersList.contains(questionPaper) && questionPaper.getMajorMinor() == majorMinor) {
                papersList.add(questionPaper);
                adapterQuestionPaper.notifyItemInserted(count);
                count++;
            }
        }
        pdLoading.dismiss();

        if (papersList.isEmpty()) {
            findViewById(R.id.tvEmpty).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tvEmpty).setVisibility(View.GONE);
        }
    }

    /**
     * This method fetches subject's question papers from Firebase Server.
     */
    private void fetchQuestionPapersByTitle(final String title, final boolean store) {
        allPapersList.clear();
        pdLoading.show();

        Call<JsonArray> call = Tools.getApi(getApplicationContext()).getQuestionPapers(title);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                if (response.body() != null){

                    JsonArray jsonArray = response.body();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        if (!jsonArray.get(i).toString().equals("null")) {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                            QuestionPaper questionPaper = new QuestionPaper();
                            questionPaper.setId(i + 1);
                            questionPaper.setTitle(title);
                            questionPaper.setCredits(jsonObject.get("credits").getAsString());
                            questionPaper.setLink(jsonObject.get("link").getAsString());
                            questionPaper.setTimestamp(jsonObject.get("timestamp").getAsString());
                            if (jsonObject.get("pdfImage") != null) {
                                questionPaper.setPdfImage(jsonObject.get("pdfImage").getAsInt());
                            }
                            questionPaper.setMajorMinor(jsonObject.get("majorMinor").getAsInt());

                            allPapersList.add(questionPaper);

                            if (store) {
                                int rows = db.updateQuestionPaper(questionPaper);
                                if (rows == 0) {
                                    db.insertQuestionPaper(questionPaper);
                                }
                            }
                        }

                        isMajor = true;
                        addQuestionPapers(0);
                    }
                } else {
                    findViewById(R.id.tvEmpty).setVisibility(View.VISIBLE);
                }
                pdLoading.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                pdLoading.dismiss();
                Toast.makeText(ActivityQuestionPapers.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method fetches subjects of branchSem from Firebase Server.
     */
    private void fetchSubjects(final String branchSem) {
        pdLoading.show();

        Call<String> call = Tools.getApi(getApplicationContext()).getSubjects(branchSem);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.body() != null){
                    subjectsArray = response.body().split("~");
                    linearLayout.removeAllViews();

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    //Add subjects to list
                    for (int index = 0; index < subjectsArray.length; index++) {
                        final TextView tvSubject = new TextView(getApplicationContext());
                        tvSubject.setLayoutParams(layoutParams);
                        tvSubject.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        tvSubject.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.montserrat_semi_bold));
                        tvSubject.setText(subjectsArray[index]);
                        tvSubject.setGravity(Gravity.CENTER);
                        tvSubject.setTextColor(getResources().getColor(R.color.colorPrimary));
                        int leftPadding = getResources().getDimensionPixelOffset(R.dimen.padding_15_dp);
                        int topPadding = getResources().getDimensionPixelOffset(R.dimen.padding_20_dp);
                        tvSubject.setPadding(leftPadding, topPadding, leftPadding, topPadding);
                        linearLayout.addView(tvSubject);

                        if (index != subjectsArray.length - 1) {
                            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.height_3_dp));
                            View v = new View(getApplicationContext());
                            v.setLayoutParams(layoutParams2);
                            v.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                            linearLayout.addView(v);
                        }

                        tvSubject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showQuestionPapers(tvSubject.getText().toString());
                            }
                        });
                    }
                } else {
                    findViewById(R.id.tvEmpty).setVisibility(View.VISIBLE);
                }
                pdLoading.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                pdLoading.dismiss();
                Toast.makeText(ActivityQuestionPapers.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onBackPressed() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popWindowToggle = false;
        } else {
            if (isSubjectsListView) {
                super.onBackPressed();
            } else {
                showSubjectsList(false);
                recyclerView.setVisibility(View.GONE);
            }

            findViewById(R.id.tvEmpty).setVisibility(View.GONE);
        }
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(ActivityQuestionPapers.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clMain), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void showDialogAdd() {
        dialog = new Dialog(ActivityQuestionPapers.this);
        dialog.setContentView(R.layout.dialog_add_paper);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface arg0) {
                uriPaper = null;
            }
        });

        TextView tvNote = dialog.findViewById(R.id.tvNote);
        spSubject = dialog.findViewById(R.id.spSubject);
        ivUpload = dialog.findViewById(R.id.ivUpload);
        btnUpload = dialog.findViewById(R.id.btnUpload);
        tvFilename = dialog.findViewById(R.id.tvFilename);
        tvSubject = dialog.findViewById(R.id.tvSubject);

        majorMinor = 0;
        pdUploading = getProgressDialogUploading();

        String first = "Note:";
        String next = " You will be notified once your submission is approved";
        tvNote.setText(first + next, TextView.BufferType.SPANNABLE);
        Spannable spannable = (Spannable)tvNote.getText();
        int start = first.length();
        int end = start + next.length();
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorBlack)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (isSubjectsListView) {
            spSubject.setVisibility(View.VISIBLE);                                                      // Show spinner for subject selection.
            tvSubject.setVisibility(View.INVISIBLE);

            final ArrayList<String> list = new ArrayList<>();
            list.add("Choose Subject");
            list.addAll(Arrays.asList(subjectsArray));

            AdapterSpinner adapterSubjects = new AdapterSpinner(this, subjectsArray, 1, list);
            spSubject.setAdapter(adapterSubjects);

            subjectTitle = "Choose Subject";
            spSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    subjectTitle = list.get(i);
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        } else {
            spSubject.setVisibility(View.INVISIBLE);                                                         // Hide spinner
            tvSubject.setVisibility(View.VISIBLE);                                                      // Show title

            AdapterSpinner adapterSubjects = new AdapterSpinner(this, subjectsArray, 0, null);
            spSubject.setAdapter(adapterSubjects);

            tvSubject.setText(subjectTitle);
        }

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
                    if (uriPaper == null) {
                        Toast.makeText(ActivityQuestionPapers.this, "Please select a file to upload", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (subjectTitle.equals("Choose Subject")) {
                        Toast.makeText(ActivityQuestionPapers.this, "Choose a subject first", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uploadFile();
                } else {
                    Toast.makeText(ActivityQuestionPapers.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.rbMajor) {
            majorMinor = 0;
        } else if (selectedId == R.id.rbMinor) {
            majorMinor = 1;
        }

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
        final String creditor = db.getProfile().getName();
        final String currentDate=  new SimpleDateFormat("dd.MM.yy 'at' h:mm a", Locale.getDefault()).format(new Date());
        final int pdfImage;

        String path = "Question Papers/" + subjectTitle + creditor + currentDate.replace(".", "_").replace(" 'at' ", "_").replace(":", "_").replace(" ", "_");
        storageReference = FirebaseStorage.getInstance().getReference();
        if (tvFilename.getText().toString().toLowerCase().endsWith("pdf")) {
            storageReference = storageReference.child(path + ".pdf");
            pdfImage = 0;
        } else {
            storageReference = storageReference.child(path + ".jpg");
            pdfImage = 1;
        }
        storageReference.putFile(uriPaper).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                pdUploading.setMessage("Please wait while we upload your paper...   " + (int)progress + "/100");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        sendToFirebase(uri.toString(), creditor, currentDate, pdfImage);
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

    private void sendToFirebase(String link, String creditor, String timestamp, int pdfImage) {
        QuestionPaper questionPaper = new QuestionPaper();
        if (isSubjectsListView) {
            questionPaper.setTitle(spSelectedSubject);
        } else {
            questionPaper.setTitle(subjectTitle);
        }
        questionPaper.setMajorMinor(majorMinor);
        questionPaper.setLink(link);
        questionPaper.setCredits(creditor);
        questionPaper.setTimestamp(timestamp);
        questionPaper.setPdfImage(pdfImage);

        Call<QuestionPaper> call = Tools.getApi(getApplicationContext()).insertQuestionPaper(questionPaper);
        call.enqueue(new Callback<QuestionPaper>() {
            @Override
            public void onResponse(@NonNull Call<QuestionPaper> call, @NonNull Response<QuestionPaper> response) {
                if (masterToken != null) {
                    Tools.sendDataNotification("Upload", "Paper", masterToken);
                }
                pdUploading.dismiss();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityQuestionPapers.this)
                        .setTitle("Success")
                        .setMessage("Your paper has been sent for approval. It will be added soon")
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
            public void onFailure(@NonNull Call<QuestionPaper> call, @NonNull Throwable t) {
                pdUploading.dismiss();
                Toast.makeText(ActivityQuestionPapers.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method is called for selecting image from camera or gallery.
     * It checks permissions and acts accordingly.
     */
    private void requestPermissions() {
        Dexter.withActivity(ActivityQuestionPapers.this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            final CharSequence[] items = {"Take Photo", "Choose file"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityQuestionPapers.this);
                            builder.setTitle("Add Paper");
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items[item].equals("Take Photo")) {
                                        cameraIntent();
                                    } else if (items[item].equals("Choose file")) {
                                        fileIntent();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityQuestionPapers.this);
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
                finish();
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void fileIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        String[] mimeTypes = {"image/*", "application/pdf"};
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 0);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 100:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final CharSequence[] items = {"Take Photo", "Choose file"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityQuestionPapers.this);
                        builder.setTitle("Add Paper");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (items[item].equals("Take Photo")) {
                                    cameraIntent();
                                } else if (items[item].equals("Choose file")) {
                                    fileIntent();
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            uriPaper = data.getData();

            if (uriPaper != null) {
                String filename = FilePath.getPath(getApplicationContext(), uriPaper);
                if (filename != null) {
                    if (filename.equals("File selected successfully")) {
                        Glide.with(getApplicationContext()).load(R.drawable.ic_file_selected).into(ivUpload);
                    } else {
                        filename = filename.substring(filename.lastIndexOf("/") + 1);
                        if (filename.toLowerCase().endsWith("pdf")) {
                            Glide.with(getApplicationContext()).load(R.drawable.ic_file_selected).into(ivUpload);
                        } else {
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
                    }
                    tvFilename.setVisibility(View.VISIBLE);
                    tvFilename.setText(filename);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
        Glide.with(getApplicationContext()).load(thumbnail).into(ivUpload);
        uriPaper = getImageUri(getApplicationContext(), thumbnail);
    }

    private ProgressDialog getProgressDialogUploading() {
        pdUploading = new ProgressDialog(ActivityQuestionPapers.this, R.style.MyAlertDialogStyle);
        pdUploading.setTitle("Uploading");
        pdUploading.setMessage("Please wait while we upload your paper");
        pdUploading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdUploading.setCancelable(false);
        pdUploading.setIndeterminate(true);
        return pdUploading;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.WEBP, 70, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
