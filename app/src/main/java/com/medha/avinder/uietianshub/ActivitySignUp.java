package com.medha.avinder.uietianshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.medha.avinder.uietianshub.adapters.AdapterSpinner;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.data.SharedPreference;
import com.medha.avinder.uietianshub.models.User;
import com.medha.avinder.uietianshub.utils.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySignUp extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputName;
    private FirebaseAuth auth;
    private DatabaseHelper db;
    private String name, semester, branch, email;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = new DatabaseHelper(this);
        auth = FirebaseAuth.getInstance();

        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        inputEmail = findViewById(R.id.etEmail);
        inputPassword = findViewById(R.id.etPassword);
        inputName = findViewById(R.id.etName);
        Button btnResetPassword = findViewById(R.id.btnResetPassword);
        Spinner spBranch = findViewById(R.id.spBranch);
        Spinner spSemester = findViewById(R.id.spSemester);

        progressDialog = new ProgressDialog(ActivitySignUp.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("Please wait while we create your account");
        progressDialog.setCancelable(false);

        final String[] semesters = {"Select Semester", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};
        AdapterSpinner spinnerAdapter = new AdapterSpinner(getApplicationContext(), semesters, 0, null);
        spSemester.setAdapter(spinnerAdapter);

        final String[] branches = {"Select Branch", "Computer Science", "Electronics and Communication", "Information Technology", "Electrical and Electronics", "Mechanical", "Biotechnology"};
        AdapterSpinner spinnerAdapter2 = new AdapterSpinner(getApplicationContext(), branches, 0, null);
        spBranch.setAdapter(spinnerAdapter2);

        spSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                semester = semesters[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                branch = branches[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivitySignUp.this, ActivityResetPassword.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Tools.isOnline(ActivitySignUp.this)){
                    Toast.makeText(ActivitySignUp.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    final String getemail = inputEmail.getText().toString().toLowerCase().trim();
                    final String password = inputPassword.getText().toString().trim();
                    name = inputName.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) {
                        inputName.setError("This field cannot be empty");
                        return;
                    }
                    if (branch.equals("Select Branch")) {
                        Toast.makeText(ActivitySignUp.this, "Please select your branch", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (semester.equals("Select Semester")) {
                        Toast.makeText(ActivitySignUp.this, "Please select your semester", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(getemail)) {
                        inputEmail.setError("This field cannot be empty");
                        return;
                    } else {
                        if (!getemail.matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+.[a-z]+")) {
                            inputEmail.setError("Invalid Email Address");
                            return;
                        } else {
                            String[] stringArray = getemail.split("@");
                            email = stringArray[0].replace(".", "").concat("@").concat(stringArray[1]);
                        }
                    }

                    if (TextUtils.isEmpty(password)) {
                        inputPassword.setError("This field cannot be empty");
                        return;
                    }
                    if (password.length() < 6) {
                        Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progressDialog.show();
                    final String email_db = email.trim().replace(".", "").replace("@", "");

                    String branchSem = Tools.getBranchAbbreviation(branch) + semester.substring(0,1);

                    final User user = new User();
                    user.setName(name);
                    user.setBranchSem(branchSem);
                    user.setEmail(email_db);
                    user.setoEmail(email);
                    user.setEmail(email_db);

                    insertUser(email_db, user, password);
                }
            }
        });
    }

    private void insertUser(String string, final User user, final String password){
        final Handler handler = new Handler();
        Call<User> call = Tools.getApi(getApplicationContext()).insertUser(string, user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                final Handler handler = new Handler();
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(ActivitySignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            progressDialog.dismiss();
                            if (String.valueOf(task.getException()).toLowerCase().contains("the email address is already in use by another account")) {
                                Toast.makeText(ActivitySignUp.this, "The email address is already in use by another account.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            int rows = db.updateFullProfile(user);
                            if (rows == 0) {
                                db.insertProfile(user);
                            }
                            new SharedPreference(ActivitySignUp.this).setSyllabusVersion(0);
                            progressDialog.dismiss();
                            startActivity(new Intent(ActivitySignUp.this, ActivityMain.class));
                            handler.removeCallbacksAndMessages(null);
                            finish();
                        }
                    }
                });
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            handler.removeCallbacksAndMessages(null);
                            Toast.makeText(ActivitySignUp.this, "Somethings wrong!!! Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 10000);
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                handler.removeCallbacksAndMessages(null);
                Toast.makeText(ActivitySignUp.this, "Somethings wrong!!! Please try again", Toast.LENGTH_SHORT).show();
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Toast.makeText(ActivitySignUp.this, "Somethings wrong!!! Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        },20000);
    }
}