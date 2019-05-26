package com.exuberant.egws_admin.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.exuberant.egws_admin.R;
import com.exuberant.egws_admin.models.Report;
import com.exuberant.egws_admin.models.ReportWrapper;
import com.exuberant.egws_admin.models.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static maes.tech.intentanim.CustomIntent.customType;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReportsReference;
    private List<Report> reportsList;
    private ChildEventListener childEventListener;
    private static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initialize();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    void initialize(){
        mDatabase = FirebaseDatabase.getInstance();
        mReportsReference = mDatabase.getReference().child("reports");
        reportsList = new ArrayList<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style));

            Intent intent = getIntent();
            String wrapperString = intent.getStringExtra("Wrapper");
            Gson gson = new Gson();
            ReportWrapper wrapper = gson.fromJson(wrapperString, ReportWrapper.class);

            if (wrapper.isEnableDetails()) {
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Intent intent = new Intent(MapsActivity.this, ReportDetailActivity.class);
                        intent.putExtra("Report", marker.getTag().toString());
                        startActivity(intent);
                        customType(MapsActivity.this, "bottom-to-up");
                        finishAfterTransition();
                    }
                });
                User user = wrapper.getUser();
                setChildEventListener(user);
            } else {
                List<Report> reportList = wrapper.getReportList();
                updateMapData(reportList);
            }


            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    void updateMapData(List<Report> reports){
        mMap.clear();
        if (reports != null)
        for (Report report : reports){
            addMarker(new LatLng(report.getLatitude(), report.getLongitude()), report.getBuildingName(), report.getBuildingAddress(), report.getReportId());
        }
    }

    private void addMarker(LatLng latLng, String title, String snippet, String tag){
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet))
                .setTag(tag);
        moveCamera(latLng);
    }

    private void moveCamera(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
    }

    void setChildEventListener(final User user){
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Report report = dataSnapshot.getValue(Report.class);
                List<String> reportIds = user.getReports();
                if (reportIds.contains(report.getReportId())) {
                    reportsList.add(report);
                }
                updateMapData(reportsList);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Report report = dataSnapshot.getValue(Report.class);
                List<String> reportIds = user.getReports();
                if (reportIds.contains(report.getReportId())) {
                    int matched = 0;
                    for (int i = 0; i < reportsList.size(); i++) {
                        if (reportsList.get(i).getReportId().equals(report.getReportId())) {
                            matched = i;
                            break;
                        }
                    }
                    reportsList.remove(matched);
                    reportsList.add(matched, report);
                    updateMapData(reportsList);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Report report = dataSnapshot.getValue(Report.class);
                List<String> reportIds = user.getReports();
                if (reportIds.contains(report.getReportId())) {
                    reportsList.remove(report);
                }
                updateMapData(reportsList);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mReportsReference.addChildEventListener(childEventListener);
    }
}
