package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QMatrixItem {
    public String qText;
    public Double qNum;
    public int qElemNumber;
    public String qState;
}
