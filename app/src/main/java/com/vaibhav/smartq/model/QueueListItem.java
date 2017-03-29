package com.vaibhav.smartq.model;

/**
 * Created by vaibhav on 11/6/2016.
 */

public class QueueListItem {
    private String qName;
    private String qDesc;
    private String qId;

    public QueueListItem(String qName,String qDesc, String qId) {
        this.qName = qName;
        this.qDesc = qDesc;
        this.qId = qId;
    }

    public String getqName() {
        return qName;
    }

    public void setqName(String qName) {
        this.qName = qName;
    }

    public String getqDesc() {
        return qDesc;
    }

    public void setqDesc(String qDesc) {
        this.qDesc = qDesc;
    }

    public String getqId() {
        return qId;
    }

    public void setqId(String qId) {
        this.qId = qId;
    }
}
