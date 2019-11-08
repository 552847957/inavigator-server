package ru.sberbank.qlik.sense.objects;

public class QInitialDataFetch {
    private int qLeft;
    private int qTop;
    private int qWidth;
    private int qHeight;

    public QInitialDataFetch() {
    }

    public QInitialDataFetch(int qLeft, int qTop, int qWidth, int qHeight) {
        this.qLeft = qLeft;
        this.qTop = qTop;
        this.qWidth = qWidth;
        this.qHeight = qHeight;
    }

    public int getqLeft() {
        return qLeft;
    }

    public void setqLeft(int qLeft) {
        this.qLeft = qLeft;
    }

    public int getqTop() {
        return qTop;
    }

    public void setqTop(int qTop) {
        this.qTop = qTop;
    }

    public int getqWidth() {
        return qWidth;
    }

    public void setqWidth(int qWidth) {
        this.qWidth = qWidth;
    }

    public int getqHeight() {
        return qHeight;
    }

    public void setqHeight(int qHeight) {
        this.qHeight = qHeight;
    }
}
