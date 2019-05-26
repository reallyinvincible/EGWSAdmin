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

import java.util.List;

import static maes.tech.intentanim.CustomIntent.customType;

public class ReportProcessAdapter extends RecyclerView.Adapter<ReportProcessAdapter.ReportProcessViewHolder> {

    List<Report> reportList;

    public ReportProcessAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportProcessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_process_report, parent, false);
        return new ReportProcessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportProcessViewHolder holder, int position) {
        final Report report = reportList.get(position);
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
        holder.emailTextView.setText(report.getAuthorEmail());
        holder.serialNumberTextView.setText(String.valueOf(reportList.size() - position));
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    class ReportProcessViewHolder extends RecyclerView.ViewHolder{

        View parent;
        TextView nameTextView, addressTextView, timeTextView, emailTextView, serialNumberTextView;

        public ReportProcessViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.cv_report_info_container);
            nameTextView = itemView.findViewById(R.id.tv_building_name);
            addressTextView = itemView.findViewById(R.id.tv_building_address);
            timeTextView = itemView.findViewById(R.id.tv_report_time);
            emailTextView =  itemView.findViewById(R.id.tv_user_mail);
            serialNumberTextView = itemView.findViewById(R.id.tv_order_number);
        }
    }

}
