package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NxMeasure {
    public NxInlineMeasureDef qDef;

    public NxMeasure() {
    }

    public NxMeasure(NxInlineMeasureDef qDef) {
        this.qDef = qDef;
    }
}

