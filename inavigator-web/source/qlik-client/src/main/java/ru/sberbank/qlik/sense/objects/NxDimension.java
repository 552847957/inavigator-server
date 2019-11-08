package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NxDimension {
    public NxInlineDimensionDef qDef;

    public NxDimension() {
    }

    public NxDimension(NxInlineDimensionDef qDef) {
        this.qDef = qDef;
    }
}
