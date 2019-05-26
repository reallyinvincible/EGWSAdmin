package com.exuberant.egws_admin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.exuberant.egws_admin.R;
import com.exuberant.egws_admin.models.Report;
import com.exuberant.egws_admin.models.ReportWrapper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static maes.tech.intentanim.CustomIntent.customType;

public class ReportDetailActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReportsReference;

    //UI Elements
    private TextView buildingNameTextView, buildingAddressTextView, buildingCityTextView, buildingPincodeTextView,
    buildingTypeTextView, buildingEmailTextView, buildingPhoneTextView, buildingAlternatePhoneTextView, reportTimeTextView, noImageTextView;
    private ImageView nameboardImageView, buildingImageView;
    private MaterialButton showOnMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        initialize();

        Intent intent = getIntent();
        String reportId = intent.getStringExtra("Report");
        mReportsReference.child(reportId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Report report = dataSnapshot.getValue(Report.class);
                setMapButton(report);
                buildingNameTextView.setText(report.getBuildingName());
                buildingAddressTextView.setText(report.getBuildingAddress());
                buildingCityTextView.setText(report.getBuildingCity());
                buildingPincodeTextView.setText(report.getPinCode());
                buildingTypeTextView.setText(report.getType());
                String time = report.getDate() + ", " + report.getTime();
                reportTimeTextView.setText(time);
                if (report.getBuildingEmail().length() > 0){
                    buildingEmailTextView.setText(report.getBuildingEmail());
                } else {
                    buildingEmailTextView.setText("Not Available");
                }
                if (report.getFirstPhoneNumber().length() > 0){
                    buildingPhoneTextView.setText(report.getFirstPhoneNumber());
                } else {
                    buildingPhoneTextView.setText("Not Available");
                }
                if (report.getSecondPhoneNumber().length() > 0){
                    buildingAlternatePhoneTextView.setText(report.getSecondPhoneNumber());
                } else {
                    buildingAlternatePhoneTextView.setText("Not Available");
                }
                try {
                    String nameboardUrl = report.getNameBoardPhoto();
                    if (nameboardUrl.equals("") || nameboardUrl.length() < 2){
                        nameboardImageView.setVisibility(View.GONE);
                    } else {
                        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(ReportDetailActivity.this);
                        circularProgressDrawable.setColorSchemeColors(getColor(R.color.colorAccent));
                        circularProgressDrawable.setStrokeWidth(10f);
                        circularProgressDrawable.setArrowEnabled(true);
                        circularProgressDrawable.setCenterRadius(100f);
                        circularProgressDrawable.start();
                        Glide.with(ReportDetailActivity.this)
                                .load(nameboardUrl)
                                .placeholder(circularProgressDrawable)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(nameboardImageView);
                        nameboardImageView.setVisibility(View.VISIBLE);
                        noImageTextView.setVisibility(View.GONE);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    String buildingUrl = report.getBuildingPhoto();
                    if (buildingUrl.equals("") || buildingUrl.length() < 2) {
                        buildingImageView.setVisibility(View.GONE);
                    } else {
                        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(ReportDetailActivity.this);
                        circularProgressDrawable.setColorSchemeColors(getColor(R.color.colorAccent));
                        circularProgressDrawable.setStrokeWidth(10f);
                        circularProgressDrawable.setArrowEnabled(true);
                        circularProgressDrawable.setCenterRadius(100f);
                        circularProgressDrawable.start();
                        Glide.with(ReportDetailActivity.this)
                                .load(buildingUrl)
                                .placeholder(circularProgressDrawable)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(buildingImageView);
                        buildingImageView.setVisibility(View.VISIBLE);
                        noImageTextView.setVisibility(View.GONE);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void initialize(){
        mDatabase = FirebaseDatabase.getInstance();
        mReportsReference = mDatabase.getReference().child("reports");
        buildingNameTextView = findViewById(R.id.tv_building_name);
        buildingAddressTextView = findViewById(R.id.tv_building_address);
        buildingCityTextView = findViewById(R.id.tv_building_city);
        buildingPincodeTextView = findViewById(R.id.tv_building_pincode);
        buildingTypeTextView = findViewById(R.id.tv_building_type);
        buildingEmailTextView = findViewById(R.id.tv_building_email);
        buildingPhoneTextView = findViewById(R.id.tv_building_phone);
        buildingAlternatePhoneTextView = findViewById(R.id.tv_building_alternate_phone);
        reportTimeTextView = findViewById(R.id.tv_report_time);
        noImageTextView = findViewById(R.id.tv_no_images);
        nameboardImageView = findViewById(R.id.iv_nameboard_picture);
        buildingImageView = findViewById(R.id.iv_building_picture);
        showOnMap = findViewById(R.id.btn_show_map);
    }

    void setMapButton(Report report){
        List<Report> reportList = new ArrayList<>();
        reportList.add(report);
        ReportWrapper wrapper = new ReportWrapper(reportList, false);
        Gson gson = new Gson();
        final String wrapperString = gson.toJson(wrapper);
        showOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportDetailActivity.this, MapsActivity.class);
                intent.putExtra("Wrapper", wrapperString);
                startActivity(intent);
                customType(ReportDetailActivity.this, "bottom-to-top");
            }
        });
        }
}
