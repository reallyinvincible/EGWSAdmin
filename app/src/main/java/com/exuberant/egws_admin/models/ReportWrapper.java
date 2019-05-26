package com.exuberant.egws_admin.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ReportWrapper {

    List<Report> reportList;
    User user;
    boolean enableDetails;

    public ReportWrapper() {
    }

    public ReportWrapper(User user, boolean enableDetails) {
        this.user = user;
        this.enableDetails = enableDetails;
    }

    public ReportWrapper(List<Report> reportList, boolean enableDetails) {
        this.reportList = reportList;
        this.enableDetails = enableDetails;
    }

    public List<Report> getReportList() {
        return reportList;
    }

    public void setReportList(List<Report> reportList) {
        this.reportList = reportList;
    }

    public boolean isEnableDetails() {
        return enableDetails;
    }

    public void setEnableDetails(boolean enableDetails) {
        this.enableDetails = enableDetails;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
