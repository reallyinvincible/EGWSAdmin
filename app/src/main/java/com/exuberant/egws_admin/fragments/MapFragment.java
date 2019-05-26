package com.exuberant.egws_admin.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.exuberant.egws_admin.R;
import com.exuberant.egws_admin.activities.ReportDetailActivity;
import com.exuberant.egws_admin.activities.ReportProcessActivity;
import com.exuberant.egws_admin.models.Report;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lib.kingja.switchbutton.SwitchMultiButton;

import static maes.tech.intentanim.CustomIntent.customType;

public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReportsReference;
    private ChildEventListener childEventListener;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private List<Report> reportsList;
    private static final float[] MARKER_COLORS = {BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_VIOLET, BitmapDescriptorFactory.HUE_YELLOW, BitmapDescriptorFactory.HUE_ROSE,
            BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_CYAN};
    List<String> usersList;

    //UI Elements
    private SwitchMultiButton reportType;
    private MaterialButton reportProcessButton;
    private TextView pinCountTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initialize(view);

        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Report report = dataSnapshot.getValue(Report.class);
                reportsList.add(report);
                if (!usersList.contains(report.getAuthorId())){
                    usersList.add(report.getAuthorId());
                }
                updateAllMarkers();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Report report = dataSnapshot.getValue(Report.class);
                int matched = 0;
                for (int i = 0; i < reportsList.size(); i++) {
                    if (reportsList.get(i).getReportId().equals(report.getReportId())) {
                        matched = i;
                        break;
                    }
                }
                reportsList.remove(matched);
                reportsList.add(matched, report);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Report report = dataSnapshot.getValue(Report.class);
                reportsList.remove(report);
                updateAllMarkers();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        reportType.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                updateAllMarkers();
            }
        });

        reportProcessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReportProcessActivity.class);
                startActivity(intent);
                customType(getContext(), "bottom-to-top");
            }
        });

        return view;
    }

    public void initialize(View view) {

        reportType = view.findViewById(R.id.swb_report_type);
        reportProcessButton = view.findViewById(R.id.btn_report_process);
        pinCountTextView = view.findViewById(R.id.tv_pin_count);

        reportsList = new ArrayList<>();
        usersList = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance();
        mReportsReference = mDatabase.getReference().child("reports");

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    try {
                        // Customise the styling of the base map using a JSON object defined
                        // in a raw resource file.
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                        getContext(), R.raw.style));
                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                Intent intent = new Intent(getActivity(), ReportDetailActivity.class);
                                intent.putExtra("Report", marker.getTag().toString());
                                startActivity(intent);
                                customType(getContext(), "bottom-to-up");
                            }
                        });

                        mReportsReference.addChildEventListener(childEventListener);

                        if (!success) {
                            Log.e(TAG, "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                    }
                }
            });
        }

    }

    void updateAllMarkers() {
        if (reportType.getSelectedTab() == 0) {
            updateMapData(reportsList);
        } else {
            updateMapData(filterTodayData());
        }
    }

    List<Report> filterTodayData() {
        List<Report> filteredReports = new ArrayList<>();
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDay = simpleDateFormat.format(date);
        for (Report report : reportsList) {
            if (report.getDate().equals(currentDay))
                filteredReports.add(report);
        }
        return filteredReports;
    }

    void updateMapData(List<Report> reports) {
        mMap.clear();
        pinCountTextView.setText(String.valueOf(reports.size()));
        for (Report report : reports) {
            addMarker(new LatLng(report.getLatitude(), report.getLongitude()), report.getBuildingName(), report.getBuildingAddress(), report.getReportId(), report.getAuthorId());
        }
    }

    private void addMarker(LatLng latLng, String title, String snippet, String tag, String userId) {
        int position = usersList.indexOf(userId) % 10;
        float color = MARKER_COLORS[position];
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(color)))
                .setTag(tag);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
}
