package com.medha.avinder.uietianshub;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.data.SharedPreference;
import com.medha.avinder.uietianshub.models.Syllabus;
import com.medha.avinder.uietianshub.models.User;
import com.medha.avinder.uietianshub.utils.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityMyProfile extends AppCompatActivity {

    private EditText etName;
    private TextView tvEmail, tvBranch, tvSemester;
    private Button btnUpdate;

    private DatabaseHelper db;
    private SharedPreference sharedPreference;
    private User me;

    private String oldName, oldBranch, oldSemester;
    private String newName, newBranch, newSemester;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        db = new DatabaseHelper(this);
        sharedPreference = new SharedPreference(this);
        me = db.getProfile();

        init();
    }

    private void init() {
        etName = findViewById(R.id.etName);
        tvEmail = findViewById(R.id.tvEmail);
        tvBranch = findViewById(R.id.tvBranch);
        tvSemester = findViewById(R.id.tvSemester);
        btnUpdate = findViewById(R.id.btnUpdate);

        tvBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(ActivityMyProfile.this, tvBranch);
                popup.getMenuInflater().inflate(R.menu.menu_branches, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        newBranch = item.getTitle().toString();
                        tvBranch.setText(newBranch);
                        buttonStatus();
                        return true;
                    }
                });
                popup.show();
            }
        });

        tvSemester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(ActivityMyProfile.this, tvSemester);
                popup.getMenuInflater().inflate(R.menu.menu_semesters, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        newSemester = item.getTitle().toString();
                        tvSemester.setText(newSemester);
                        buttonStatus();
                        return true;
                    }
                });
                popup.show();
            }
        });

        progressDialog = new ProgressDialog(ActivityMyProfile.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Updating");
        progressDialog.setMessage("Please wait while we update your profile");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);

        String branchSem = me.getBranchSem();
        oldName = me.getName();
        oldSemester = branchSem.substring(branchSem.length() - 1);
        if (branchSem.substring(0,2).equals("It")) {
            oldBranch = "It";
        } else {
            oldBranch = branchSem.substring(0, 3);
        }

        etName.setText(oldName);
        tvEmail.setText(me.getoEmail());
        tvBranch.setText(Tools.getAbbreviationBranch(oldBranch));
        switch (oldSemester) {
            case "1":
                tvSemester.setText(oldSemester + "st");
                break;
            case "2":
                tvSemester.setText(oldSemester + "nd");
                break;
            case "3":
                tvSemester.setText(oldSemester + "rd");
                break;
            default:
                tvSemester.setText(oldSemester + "th");
                break;
        }

        etName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etName.setEnabled(true);
                etName.setCursorVisible(true);
            }
        });
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                newName = charSequence.toString();
                buttonStatus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Tools.isOnline(ActivityMyProfile.this)) {
                    progressDialog.show();
                    updateProfile();
                } else {
                    Toast.makeText(ActivityMyProfile.this, "No internet access", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProfile() {
        final String name, branch, semester;
        if (newName != null) {
            name = newName;
        } else {
            name = oldName;
        }
        if (newBranch != null) {
            branch = Tools.getBranchAbbreviation(newBranch);
        } else {
            branch = oldBranch;
        }
        if (newSemester != null) {
            semester = newSemester.substring(0, 1);
        } else {
            semester = oldSemester;
        }

        final User user = new User();
        user.setName(name);
        user.setBranchSem(branch + semester);
        user.setEmail(me.getEmail());
        user.setoEmail(me.getoEmail());

        Call<User> call = Tools.getApi(getApplicationContext()).insertUser(me.getEmail(), user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                db.updateProfile(name, branch + semester);
                getFCMToken();

                sharedPreference.setSyllabusVersion(0);

                fetchSyllabus(branch + semester);
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
            }
        });
    }

    private void buttonStatus() {
        String name, branch, semester;
        if (newName != null) {
            name = newName;
        } else {
            name = oldName;
        }
        if (newBranch != null) {
            branch = Tools.getBranchAbbreviation(newBranch);
        } else {
            branch = oldBranch;
        }
        if (newSemester != null) {
            semester = newSemester.substring(0, 1);
        } else {
            semester = oldSemester;
        }

        if (!oldName.equals(name) || !oldBranch.equals(branch) || !oldSemester.equals(semester)){
            btnUpdate.setEnabled(true);
            btnUpdate.setAlpha(1f);
        } else {
            btnUpdate.setEnabled(false);
            btnUpdate.setAlpha(0.5f);
        }
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
                            Log.w("FCM Id", "getInstanceId failed", task.getException());
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

    /**
     * This method fetches syllabus from Firebase Server and store in SQLite.
     */
    private void fetchSyllabus(final String branchSem) {
        Call<Syllabus> call = Tools.getApi(getApplicationContext()).getSyllabus(branchSem);
        call.enqueue(new Callback<Syllabus>() {
            @Override
            public void onResponse(@NonNull Call<Syllabus> call, @NonNull Response<Syllabus> response) {
                if (response.body() != null){
                    Syllabus syllabus = response.body();
                    syllabus.setBranchSem(branchSem);

                    int rows = db.updateSyllabus(syllabus);
                    if (rows == 0) {
                        db.insertSyllabus(syllabus);
                    }
                    sharedPreference.setSyllabusVersion(0);
                } else {
                    Toast.makeText(ActivityMyProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

                progressDialog.dismiss();
                AlertDialog.Builder alertbox = new AlertDialog.Builder(ActivityMyProfile.this);
                alertbox.setMessage("Your profile has been updated");
                alertbox.setTitle("Update Successful");
                alertbox.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                alertbox.create();
                alertbox.show();
                btnUpdate.setEnabled(false);
                btnUpdate.setAlpha(0.5f);
            }

            @Override
            public void onFailure(@NonNull Call<Syllabus> call, @NonNull Throwable t) {
            }
        });
    }
}
