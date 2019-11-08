package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NxInlineDimensionDef {
    public String[] qFieldLabels;
    public String[] qFieldDefs;
    public int qActiveField;

    public NxInlineDimensionDef() {
    }

    public NxInlineDimensionDef(String[] qFieldLabels, String[] qFieldDefs, int qActiveField) {
        this.qFieldLabels = qFieldLabels;
        this.qFieldDefs = qFieldDefs;
        this.qActiveField = qActiveField;
    }
}
