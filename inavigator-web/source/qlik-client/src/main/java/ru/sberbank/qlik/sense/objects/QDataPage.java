package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QDataPage {
    public QMatrixItem[][] qMatrix;
    public NxRect qArea;
}
