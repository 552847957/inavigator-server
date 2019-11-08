package ru.sberbank.qlik.view;

import java.util.List;

public class QlikViewClientRequest {
    private String documentUri;
    private String user;
    private String password;
    private List<String> objectIds;
    private boolean quit;
    private boolean deleteCvs;

    public String getDocumentUri() {
        return documentUri;
    }

    public QlikViewClientRequest setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
        return this;
    }

    public String getUser() {
        return user;
    }

    public QlikViewClientRequest setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public QlikViewClientRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public List<String> getObjectIds() {
        return objectIds;
    }

    public QlikViewClientRequest setObjectIds(List<String> objectIds) {
        this.objectIds = objectIds;
        return this;
    }

    public boolean isQuit() {
        return quit;
    }

    public QlikViewClientRequest setQuit(boolean quit) {
        this.quit = quit;
        return this;
    }

    public boolean isDeleteCvs() {
        return deleteCvs;
    }

    public QlikViewClientRequest setDeleteCvs(boolean deleteCvs) {
        this.deleteCvs = deleteCvs;
        return this;
    }
}
