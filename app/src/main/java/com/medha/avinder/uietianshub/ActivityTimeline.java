package com.medha.avinder.uietianshub;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.medha.avinder.uietianshub.adapters.AdapterTimeline;
import com.medha.avinder.uietianshub.data.DatabaseHelper;
import com.medha.avinder.uietianshub.models.Timeline;

import java.util.ArrayList;

public class ActivityTimeline extends AppCompatActivity {
    private RecyclerView recyclerView;

    private DatabaseHelper db;

    private int position;

    private ArrayList<Timeline> timelineList;
    private AdapterTimeline adapterTimeline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        db = new DatabaseHelper(this);

        position = getIntent().getIntExtra("Position", 0);

        init();
    }

    private void init() {
        ((TextView)findViewById(R.id.tvTitle)).setText("Notice Board");

        recyclerView = findViewById(R.id.recyclerView);

        timelineList = new ArrayList<>();

        adapterTimeline = new AdapterTimeline(this, timelineList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapterTimeline);

        getNotifications();

        findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * This method fetches notifications name from SQLite.
     */
    private void getNotifications() {
        ArrayList<Timeline> list = new ArrayList<>(db.getAllNotifications());
        int count = 0;
        for (Timeline timeline : list) {
            timelineList.add(timeline);
            adapterTimeline.notifyItemInserted(count);
            count++;
        }

        if (position != 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    recyclerView.smoothScrollToPosition(position);
                }
            }, 700);
        }
    }
}
