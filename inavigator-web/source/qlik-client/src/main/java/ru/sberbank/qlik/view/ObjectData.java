package ru.sberbank.qlik.view;

import java.util.List;

public class ObjectData {
    private String id;
    private List<String> columns;
    private List<List<String>> matrix;
    private boolean error;
    private String errorMessage;
    private String title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<List<String>> getMatrix() {
        return matrix;
    }

    public void setMatrix(List<List<String>> matrix) {
        this.matrix = matrix;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isError() {
        return error;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "ObjectData{" +
                "id='" + id + '\'' +
                ", columns=" + columns +
                ", matrix=" + matrix +
                ", error=" + error +
                ", errorMessage='" + errorMessage + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
