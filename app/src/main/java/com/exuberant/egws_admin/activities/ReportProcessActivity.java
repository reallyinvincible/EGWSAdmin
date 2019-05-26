package com.exuberant.egws_admin.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.exuberant.egws_admin.R;
import com.exuberant.egws_admin.adapters.ReportProcessAdapter;
import com.exuberant.egws_admin.models.Report;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shrikanthravi.collapsiblecalendarview.data.Day;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;

import net.ozaydin.serkan.easy_csv.EasyCsv;
import net.ozaydin.serkan.easy_csv.FileCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lib.kingja.switchbutton.SwitchMultiButton;

import static maes.tech.intentanim.CustomIntent.customType;

public class ReportProcessActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReportsReference;
    private ChildEventListener childEventListener;
    private List<Report> reportsList, reportsForExport;
    private MaterialButton exportButton, showAllReportsButton;
    private RecyclerView reportProcessRecyclerView;
    private CollapsibleCalendar collapsibleCalendar;
    private View parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_process);
        initialize();
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportData();
            }
        });
        showAllReportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllReports();
            }
        });
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                Day day = collapsibleCalendar.getSelectedDay();
                String date = day.getDay() + "-" + getMonth(day.getMonth()+1) + "-" + day.getYear();
                setReports(date);
            }

            @Override
            public void onItemClick(View v) {

            }

            @Override
            public void onDataUpdate() {

            }

            @Override
            public void onMonthChange() {

            }

            @Override
            public void onWeekChange(int position) {

            }
        });
    }

    void initialize(){
        mDatabase = FirebaseDatabase.getInstance();
        mReportsReference = mDatabase.getReference().child("reports");
        reportsList = new ArrayList<>();
        reportsForExport = new ArrayList<>();
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Report report = dataSnapshot.getValue(Report.class);
                reportsList.add(report);
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
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mReportsReference.addChildEventListener(childEventListener);
        exportButton = findViewById(R.id.btn_export);
        showAllReportsButton = findViewById(R.id.btn_all_reports);
        collapsibleCalendar = findViewById(R.id.collapsibleCalendar);
        reportProcessRecyclerView = findViewById(R.id.rv_user_report_process);
        parent = findViewById(R.id.activity_report_process);
        showAllReports();
    }

    void exportData() {
        final List<Report> reports = reportsForExport;
        EasyCsv easyCsv = new EasyCsv(ReportProcessActivity.this);
        String header = "Serial No^Report ID^User ID^User Mail^Latitude^Longitude^Building Name^Address^City^Pin^Mail^Phone^Alternate Phone^Type^Nameboard Image Link^Building Image Link^Date^Time`";
        List<String> headerList = new ArrayList<>();
        headerList.add(header);
        List<String> dataList = new ArrayList<>();
        Collections.reverse(reports);
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            String data = String.valueOf(reports.size() - i) + "^" + report.getReportId() + "^" + report.getAuthorId() + "^" +
                    report.getAuthorEmail() + "^" + String.valueOf(report.getLatitude()) + "^" + String.valueOf(report.getLongitude()) + "^" + '"' +
                    report.getBuildingName() + '"' + "^" + '"' + report.getBuildingAddress() + '"' + "^" + '"' + report.getBuildingCity() + '"' + "^" + report.getPinCode() + "^" +
                    report.getBuildingEmail() + "^" + report.getFirstPhoneNumber() + "^" + report.getSecondPhoneNumber() + "^" + report.getType() + "^" +
                    report.getNameBoardPhoto() + "^" + report.getBuildingPhoto() + "^" + report.getDate() + "^" + report.getTime() + "`";
            dataList.add(data);
        }
        easyCsv.setSeparatorColumn("^");
        easyCsv.setSeperatorLine("`");
        easyCsv.createCsvFile("reports", headerList, dataList, 601, new FileCallback() {
            @Override
            public void onSuccess(File receivedFile) {
                shareFile();
            }


            @Override
            public void onFail(String s) {
                Toast.makeText(ReportProcessActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void shareFile() {
        String filename = "/reports.csv";
        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), filename);
        Uri path = FileProvider.getUriForFile(ReportProcessActivity.this, "com.exuberant.egws_admin.fileprovider", filelocation);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        String[] to = {"egws2019@gmail.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "EWGS Live Report");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "This is your Report");
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    void setReports(String date){
        List<Report> filteredReport = new ArrayList<>();
        for (Report report : reportsList){
            if (report.getDate().equals(date))
                filteredReport.add(report);
        }
        if (filteredReport.size() > 0) {
            reportsForExport = filteredReport;
            ReportProcessAdapter adapter = new ReportProcessAdapter(filteredReport);
            reportProcessRecyclerView.setAdapter(adapter);
            showAllReportsButton.setVisibility(View.GONE);
        } else {
            reportsForExport = reportsList;
            Snackbar snackbar = Snackbar.make(parent, "No Reports Found", Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundResource(R.color.colorErrorSnackbar);
            snackbar.show();
            reportProcessRecyclerView.setAdapter(null);
            showAllReportsButton.setVisibility(View.VISIBLE);
        }
    }

    void showAllReports(){
        showAllReportsButton.setVisibility(View.GONE);
        ReportProcessAdapter adapter = new ReportProcessAdapter(reportsList);
        reportProcessRecyclerView.setAdapter(adapter);
    }

    String getMonth(int month){
        String monthString;
        switch (month) {
            case 1:  monthString = "Jan";     break;
            case 2:  monthString = "Feb";     break;
            case 3:  monthString = "Mar";     break;
            case 4:  monthString = "Apr";     break;
            case 5:  monthString = "May";     break;
            case 6:  monthString = "Jun";     break;
            case 7:  monthString = "Jul";     break;
            case 8:  monthString = "Aug";     break;
            case 9:  monthString = "Sep";     break;
            case 10: monthString = "Oct";     break;
            case 11: monthString = "Nov";     break;
            case 12: monthString = "Dec";     break;
            default: monthString = "Invalid month"; break;
        }
        return  monthString;
    }

}
