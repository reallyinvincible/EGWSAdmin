package com.exuberant.egws_admin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.exuberant.egws_admin.R;
import com.exuberant.egws_admin.adapters.UserReportAdapter;
import com.exuberant.egws_admin.models.Report;
import com.exuberant.egws_admin.models.ReportWrapper;
import com.exuberant.egws_admin.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lib.kingja.switchbutton.SwitchMultiButton;

import static maes.tech.intentanim.CustomIntent.customType;

public class UserDetailActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserReference, mReportsReference;
    private ChildEventListener childEventListener;
    private List<Report> reportsList;

    //UI Elements
   private TextView totalReportCount, noReportsTextView;
   private RecyclerView reportsRecyclerView;
   private SwitchMultiButton reportType;
   private MaterialButton showOnMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        initialize();
        Intent intent = getIntent();
        final String userId = intent.getStringExtra("UserId");
        showTotalReports(userId);
        reportType.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                if (position == 0){
                    showTotalReports(userId);
                } else {
                    showTodaysReports(userId);
                }
            }
        });

    }

    void initialize(){
        showOnMap = findViewById(R.id.btn_show_map);
        mDatabase = FirebaseDatabase.getInstance();
        mUserReference = mDatabase.getReference().child("users");
        mReportsReference = mDatabase.getReference().child("reports");
        totalReportCount = findViewById(R.id.tv_total_reports);
        noReportsTextView = findViewById(R.id.tv_no_reports);
        reportsRecyclerView = findViewById(R.id.rv_user_reports);
        reportType = findViewById(R.id.swb_report_type);
    }

    private void showTotalReports(String userId) {
        mUserReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                updateMapButton(user);
                List<String> reports = user.getReports();
                if (reports == null || reports.size() == 0){
                    totalReportCount.setText("0");
                    noReportsTextView.setVisibility(View.VISIBLE);
                } else {
                    totalReportCount.setText(String.valueOf(reports.size()));
                    noReportsTextView.setVisibility(View.GONE);
                    Collections.reverse(reports);
                    UserReportAdapter adapter = new UserReportAdapter(reports, mReportsReference);
                    reportsRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showTodaysReports(final String userId){
        mReportsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> reports = new ArrayList<>();
                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                String currentDay = simpleDateFormat.format(date);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Report report = snapshot.getValue(Report.class);
                    if (report.getDate().equals(currentDay) && report.getAuthorId().equals(userId)){
                        reports.add(report.getReportId());
                    }
                }
                if (reports == null || reports.size() == 0){
                    totalReportCount.setText("0");
                    noReportsTextView.setVisibility(View.VISIBLE);
                    reportsRecyclerView.setAdapter(null);
                } else {
                    totalReportCount.setText(String.valueOf(reports.size()));
                    noReportsTextView.setVisibility(View.GONE);
                    Collections.reverse(reports);
                    UserReportAdapter adapter = new UserReportAdapter(reports, mReportsReference);
                    reportsRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void updateMapButton(User user){
        ReportWrapper wrapper = new ReportWrapper(user, true);
        Gson gson = new Gson();
        final String wrapperString = gson.toJson(wrapper);
        showOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetailActivity.this, MapsActivity.class);
                intent.putExtra("Wrapper", wrapperString);
                startActivity(intent);
                customType(UserDetailActivity.this, "bottom-to-top");
            }
        });
    }

}
