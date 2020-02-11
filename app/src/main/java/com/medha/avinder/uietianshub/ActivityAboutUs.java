package com.medha.avinder.uietianshub;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.widget.TextView;

public class ActivityAboutUs extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        boolean aboutPrivacy = getIntent().getBooleanExtra("About Privacy", false);

        if (aboutPrivacy) {
            setContentView(R.layout.activity_about_us);
            Linkify.addLinks((TextView)findViewById(R.id.tvContactUs), Linkify.ALL);
        } else {
            setContentView(R.layout.activity_privacy_policy);
        }
    }
}
