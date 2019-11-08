package ru.sberbank.qlik.services;

import java.util.List;

public class Selection {
    private List<Integer> rows;
    private List<Integer> cols;

    public Selection(List<Integer> rows, List<Integer> cols) {
        this.rows = rows;
        this.cols = cols;
    }

    public List<Integer> getRows() {
        return rows;
    }

    public void setRows(List<Integer> rows) {
        this.rows = rows;
    }

    public List<Integer> getCols() {
        return cols;
    }

    public void setCols(List<Integer> cols) {
        this.cols = cols;
    }
}
