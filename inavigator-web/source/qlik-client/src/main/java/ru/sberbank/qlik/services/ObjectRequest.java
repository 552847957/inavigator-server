package ru.sberbank.qlik.services;

public class ObjectRequest {
    private String id;
    private Selection selection;

    public ObjectRequest(String id) {
        this.id = id;
    }

    public ObjectRequest(String id, Selection selection) {
        this.id = id;
        this.selection = selection;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Selection getSelection() {
        return selection;
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
    }
}
