package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QLayout {
    public QInfo qInfo;
    public QHyperCube qHyperCube;
    public Object qSelectionInfo;
    public QListObject qListObject;
}
