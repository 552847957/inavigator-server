package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QHyperCubeDef {
    public QMode qMode;
    public NxDimension[] qDimensions;
    public NxMeasure[] qMeasures;
    public QInitialDataFetch[] qInitialDataFetch;
    public boolean qAlwaysFullyExpanded;
    public int qPseudoDimPos;
    public int qNoOfLeftDims;
    public boolean qShowTotalsAbove;

    public QMode getqMode() {
        return qMode;
    }

    public void setqMode(QMode qMode) {
        this.qMode = qMode;
    }

    public NxDimension[] getqDimensions() {
        return qDimensions;
    }

    public void setqDimensions(NxDimension[] qDimensions) {
        this.qDimensions = qDimensions;
    }

    public NxMeasure[] getqMeasures() {
        return qMeasures;
    }

    public void setqMeasures(NxMeasure[] qMeasures) {
        this.qMeasures = qMeasures;
    }

    public QInitialDataFetch[] getqInitialDataFetch() {
        return qInitialDataFetch;
    }

    public void setqInitialDataFetch(QInitialDataFetch[] qInitialDataFetch) {
        this.qInitialDataFetch = qInitialDataFetch;
    }

    public boolean isqAlwaysFullyExpanded() {
        return qAlwaysFullyExpanded;
    }

    public void setqAlwaysFullyExpanded(boolean qAlwaysFullyExpanded) {
        this.qAlwaysFullyExpanded = qAlwaysFullyExpanded;
    }

    public int getqPseudoDimPos() {
        return qPseudoDimPos;
    }

    public void setqPseudoDimPos(int qPseudoDimPos) {
        this.qPseudoDimPos = qPseudoDimPos;
    }

    public int getqNoOfLeftDims() {
        return qNoOfLeftDims;
    }

    public void setqNoOfLeftDims(int qNoOfLeftDims) {
        this.qNoOfLeftDims = qNoOfLeftDims;
    }

    public boolean isqShowTotalsAbove() {
        return qShowTotalsAbove;
    }

    public void setqShowTotalsAbove(boolean qShowTotalsAbove) {
        this.qShowTotalsAbove = qShowTotalsAbove;
    }
}
