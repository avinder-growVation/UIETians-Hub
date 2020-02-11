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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.medha.avinder.uietianshub.adapters.AdapterSpinner;
import com.medha.avinder.uietianshub.adapters.AdapterSyllabus;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.models.Subject;
import com.medha.avinder.uietianshub.models.Syllabus;
import com.medha.avinder.uietianshub.utils.Tools;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySyllabus extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Spinner spBranch, spSem;
    private TextView btnView;

    private DatabaseHelper db;

    private ArrayList<Subject> subjectList = new ArrayList<>();
    private AdapterSyllabus adapterSyllabus;

    private String branch;
    private int sem;

    private boolean isAdvancedShowing = false;
    private String spSelectedBranch;
    private String spSelectedSem;

    private ProgressDialog pdLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllabus);

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
        initAdvancedOptions();
    }

    private void init() {
        findViewById(R.id.svConstraint).setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.recyclerViewSyllabus);
        recyclerView.setVisibility(View.VISIBLE);

        adapterSyllabus = new AdapterSyllabus(this, subjectList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterSyllabus);

        pdLoading = getProgressDialog();

        showSyllabus(db.getSyllabus(branch + sem));

        findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * This method shows syllabus by adding to the view.
     */
    private void showSyllabus(Syllabus syllabus) {
        subjectList.clear();
        adapterSyllabus.notifyDataSetChanged();

        String[] subjects = syllabus.getSubjects().split("~");
        String[] pages = syllabus.getPages().split("~");
        int count = 0;
        for (int i = 0; i < subjects.length; i++) {
            Subject subject = new Subject(syllabus.getBranchSem(), subjects[i], syllabus.getLink(), pages[i]);
            subjectList.add(subject);
            adapterSyllabus.notifyItemInserted(count);
            count++;
        }
    }

    private void initAdvancedOptions() {
        findViewById(R.id.clAdvanced).setVisibility(View.VISIBLE);

        spBranch = findViewById(R.id.spBranch);
        spSem = findViewById(R.id.spSem);
        btnView = findViewById(R.id.btnView);

        final ImageView ivDropDown = findViewById(R.id.ivDropDown);
        final RelativeLayout rlBranch = findViewById(R.id.rlBranch);
        final RelativeLayout rlSem = findViewById(R.id.rlSem);

        final String[] branches = {"Computer Science", "Electronics and Communication", "Information Technology", "Electrical and Electronics", "Mechanical", "Biotechnology"};
        AdapterSpinner adapterBranches = new AdapterSpinner(this, branches, 2, null);
        spBranch.setAdapter(adapterBranches);

        spBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spSelectedBranch = branches[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final String[] semesters = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};
        AdapterSpinner adapterSemester = new AdapterSpinner(this, semesters, 2, null);
        spSem.setAdapter(adapterSemester);

        spSem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spSelectedSem = semesters[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spSem.setSelection(sem - 1);
        spBranch.setSelection(Arrays.asList(branches).indexOf(Tools.getAbbreviationBranch(branch)));

        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Tools.isOnline(getApplicationContext())) {
                    pdLoading.show();

                    if (Tools.getBranchAbbreviation(spSelectedBranch).equals(branch) && Integer.parseInt(spSelectedSem.substring(0, 1)) == sem) {
                        pdLoading.dismiss();
                        showSyllabus(db.getSyllabus(branch + sem));
                    } else {
                        fetchSyllabus(Tools.getBranchAbbreviation(spSelectedBranch) + spSelectedSem.substring(0, 1));
                    }
                } else {
                    showSnackbar();
                }
            }
        });

        findViewById(R.id.llAdvance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAdvancedShowing) {
                    ivDropDown.setRotation(360f);
                    isAdvancedShowing = false;
                    rlBranch.setVisibility(View.GONE);
                    rlSem.setVisibility(View.GONE);
                    btnView.setVisibility(View.GONE);
                } else {
                    ivDropDown.setRotation(180f);
                    isAdvancedShowing = true;
                    rlBranch.setVisibility(View.VISIBLE);
                    rlSem.setVisibility(View.VISIBLE);
                    btnView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * This method fetches syllabus from Firebase Server.
     */
    private void fetchSyllabus(final String branchSem) {
        Call<Syllabus> call = Tools.getApi(getApplicationContext()).getSyllabus(branchSem);
        call.enqueue(new Callback<Syllabus>() {
            @Override
            public void onResponse(@NonNull Call<Syllabus> call, @NonNull Response<Syllabus> response) {
                if (response.body() != null) {
                    Syllabus syllabus = response.body();
                    syllabus.setBranchSem(branchSem);

                    showSyllabus(syllabus);
                } else {
                    Toast.makeText(ActivitySyllabus.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                pdLoading.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<Syllabus> call, @NonNull Throwable t) {
                pdLoading.dismiss();
                Toast.makeText(ActivitySyllabus.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(ActivitySyllabus.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Loading Syllabus");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private void showSnackbar(){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.clMain), "No internet connection", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
