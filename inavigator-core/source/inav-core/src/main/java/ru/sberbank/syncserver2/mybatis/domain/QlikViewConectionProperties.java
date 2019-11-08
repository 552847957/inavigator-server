package ru.sberbank.syncserver2.mybatis.domain;

import ru.sberbank.syncserver2.util.FormatHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QlikViewConectionProperties implements Serializable {

    private String documentUri;
    private String dashBoardId;
    private String user;
    private String password;
    private List<QlikViewObjectId> objectIds;

    public QlikViewConectionProperties() {
    }

    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }

    public String getDashBoardId() {
        return dashBoardId;
    }

    public void setDashBoardId(String dashBoardId) {
        this.dashBoardId = dashBoardId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<QlikViewObjectId> getObjectIds() {
        return objectIds;
    }

    public List<String> getOnlyObjectIds() {
        ArrayList<String> result = new ArrayList<String>();
        for (QlikViewObjectId o : objectIds) {
            result.add(o.getObjectId());
        }
        return result;
    }

    public void setObjectIds(List<QlikViewObjectId> objectIds) {
        this.objectIds = objectIds;
    }

    @Override
    public String toString() {
        return FormatHelper.stringConcatenator("QlikViewConectionProperties{",
                "documentUri='", documentUri, '\'',
                ", dashBoardId='", dashBoardId, '\'',
                ", user='", user, '\'',
                ", password='", password, '\'',
                ", objectIds=", objectIds,
                '}');
    }
}
