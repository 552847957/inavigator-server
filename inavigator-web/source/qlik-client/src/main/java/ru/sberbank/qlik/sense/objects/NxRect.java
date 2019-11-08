package ru.sberbank.qlik.sense.objects;

public class NxRect {
    public int qLeft;
    public int qTop;
    public int qWidth;
    public int qHeight;

    public NxRect() {
    }

    public NxRect(int qLeft, int qTop, int qWidth, int qHeight) {
        this.qLeft = qLeft;
        this.qTop = qTop;
        this.qWidth = qWidth;
        this.qHeight = qHeight;
    }
}
