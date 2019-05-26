package com.exuberant.egws_admin.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exuberant.egws_admin.R;
import com.exuberant.egws_admin.activities.ReportDetailActivity;
import com.exuberant.egws_admin.models.Report;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static maes.tech.intentanim.CustomIntent.customType;

public class UserReportAdapter extends RecyclerView.Adapter<UserReportAdapter.UserReportViewHolder> {

    List<String> reportList;
    DatabaseReference mReportReference;

    public UserReportAdapter(List<String> reportList, DatabaseReference mReportReference) {
        this.reportList = reportList;
        this.mReportReference = mReportReference;
    }

    @NonNull
    @Override
    public UserReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_user_reports, parent, false);
        return new UserReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserReportViewHolder holder, int position) {
        String reportId = reportList.get(position);
        mReportReference.child(reportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Report report = dataSnapshot.getValue(Report.class);
                holder.parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), ReportDetailActivity.class);
                        intent.putExtra("Report", report.getReportId());
                        v.getContext().startActivity(intent);
                        customType(v.getContext(), "bottom-to-up");
                    }
                });
                holder.nameTextView.setText(report.getBuildingName());
                holder.addressTextView.setText(report.getBuildingAddress());
                String time = report.getDate() + ", " + report.getTime();
                holder.timeTextView.setText(time);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    class UserReportViewHolder extends RecyclerView.ViewHolder {

        View parent;
        TextView nameTextView, addressTextView, timeTextView;

        public UserReportViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.cv_report_info_container);
            nameTextView = itemView.findViewById(R.id.tv_building_name);
            addressTextView = itemView.findViewById(R.id.tv_building_address);
            timeTextView = itemView.findViewById(R.id.tv_report_time);
        }
    }

}
