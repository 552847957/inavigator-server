package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NxInlineMeasureDef {
    public String qLabel;

    public NxInlineMeasureDef() {
    }

    public NxInlineMeasureDef(String qLabel) {
        this.qLabel = qLabel;
    }
}
