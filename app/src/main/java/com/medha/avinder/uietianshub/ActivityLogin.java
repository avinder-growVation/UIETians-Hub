package com.medha.avinder.uietianshub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.data.SharedPreference;
import com.medha.avinder.uietianshub.models.User;
import com.medha.avinder.uietianshub.utils.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLogin extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private DatabaseHelper db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = new DatabaseHelper(this);

        if (auth.getCurrentUser() != null && db.getProfileCount() != 0) {
            startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
            finish();
        }

        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.etEmail);
        inputPassword = findViewById(R.id.etPassword);
        Button btnSignup = findViewById(R.id.btnSignUp);
        Button btnLogin = findViewById(R.id.btnSignIn);
        Button btnReset = findViewById(R.id.btnResetPassword);

        progressDialog = new ProgressDialog(ActivityLogin.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Authenticating");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityLogin.this, ActivitySignUp.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityLogin.this, ActivityResetPassword.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            String email;
            @Override
            public void onClick(View v) {
                if (!Tools.isOnline(ActivityLogin.this)){
                    Toast.makeText(ActivityLogin.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    String getemail = inputEmail.getText().toString().toLowerCase().trim();
                    final String password = inputPassword.getText().toString();

                    if (TextUtils.isEmpty(getemail)) {
                        inputEmail.setError("Enter email address");
                        return;
                    } else {
                        if (!inputEmail.getText().toString().trim().matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")) {
                            inputEmail.setError("Invalid Email Address");
                            return;
                        } else {
                            String[] stringArray = getemail.split("@");
                            email = stringArray[0].replace(".", "").concat("@").concat(stringArray[1]);
                        }
                    }

                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(getApplicationContext(), "Enter password", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progressDialog.show();
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(ActivityLogin.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        if (String.valueOf(task.getException()).toLowerCase().contains("there is no user record corresponding to this identifier")) {
                                            Toast.makeText(ActivityLogin.this, "No user found", Toast.LENGTH_SHORT).show();
                                        } else if (String.valueOf(task.getException()).toLowerCase().contains("the password is invalid")) {
                                            Toast.makeText(ActivityLogin.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        final String email_db = email.trim().replace(".", "").replace("@", "");
                                        getUser(email_db);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void getUser(String string){
        final Handler handler = new Handler();
        Call<User> call = Tools.getApi(getApplicationContext()).getUser(string);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                final User user = response.body();
                int rows = db.updateFullProfile(user);
                if (rows == 0) {
                    db.insertProfile(user);
                }
                new SharedPreference(ActivityLogin.this).setSyllabusVersion(0);
                progressDialog.dismiss();
                handler.removeCallbacksAndMessages(null);
                Intent intent = new Intent(ActivityLogin.this, ActivityMain.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ActivityLogin.this, "Somethings wrong!!! Please try again", Toast.LENGTH_SHORT).show();
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(ActivityLogin.this, "Somethings wrong!!! Please try again", Toast.LENGTH_SHORT).show();
            }
        },20000);
    }
}

