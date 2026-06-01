package com.scm.erp.fi;

import java.util.ArrayList;
import java.util.List;

public class JournalEntryRecord {
    private String jeNo;
    private String bizKey;
    private String status = "POSTED";
    private String waybillNo;
    private final List<JournalLineRecord> lines = new ArrayList<>();

    public String getJeNo() {
        return jeNo;
    }

    public void setJeNo(String jeNo) {
        this.jeNo = jeNo;
    }

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWaybillNo() {
        return waybillNo;
    }

    public void setWaybillNo(String waybillNo) {
        this.waybillNo = waybillNo;
    }

    public List<JournalLineRecord> lines() {
        return lines;
    }
}
