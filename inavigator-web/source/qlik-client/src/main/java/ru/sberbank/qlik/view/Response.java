package ru.sberbank.qlik.view;

import java.util.List;

public class Response {
    private String document;
    private List<ObjectData> data;
    private boolean error;
    private String errorMessage;

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public List<ObjectData> getData() {
        return data;
    }

    public void setData(List<ObjectData> data) {
        this.data = data;
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

    @Override
    public String toString() {
        return "Response{" +
                "document='" + document + '\'' +
                ", data=" + data +
                ", error=" + error +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
