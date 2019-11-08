package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QInfo {
    private String qId;
    private QType qType;

    public String getqId() {
        return qId;
    }

    public void setqId(String qId) {
        this.qId = qId;
    }

    public QType getqType() {
        return qType;
    }

    public void setqType(QType qType) {
        this.qType = qType;
    }
}
