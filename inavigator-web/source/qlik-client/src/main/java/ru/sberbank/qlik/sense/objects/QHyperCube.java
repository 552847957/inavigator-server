package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QHyperCube {
    public QSize qSize;
    public QDimensionInfo[] qDimensionInfo;
}
