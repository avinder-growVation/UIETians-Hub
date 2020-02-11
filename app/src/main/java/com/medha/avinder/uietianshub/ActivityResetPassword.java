package com.medha.avinder.uietianshub;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.medha.avinder.uietianshub.utils.Tools;

public class ActivityResetPassword extends AppCompatActivity {

    private EditText etEmail;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etEmail = findViewById(R.id.email);
        Button btnReset = findViewById(R.id.btnResetPassword);
        Button btnBack = findViewById(R.id.btn_back);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(ActivityResetPassword.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Sending Email...");
        progressDialog.setMessage("Please wait while we send you the instructions");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Tools.isOnline(ActivityResetPassword.this)){
                    Toast.makeText(ActivityResetPassword.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    String email = etEmail.getText().toString().toLowerCase().trim();

                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progressDialog.show();
                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ActivityResetPassword.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ActivityResetPassword.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

}
