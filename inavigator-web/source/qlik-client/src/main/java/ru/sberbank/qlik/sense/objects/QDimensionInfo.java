package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QDimensionInfo {
    public String qFallbackTitle;
    public int qApprMaxGlyphCount;
    public int qCardinal;
    public String qDimensionType;
    public String[] qGroupFieldDefs;
}
