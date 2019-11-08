package ru.sberbank.syncserver2.mybatis.domain;

import ru.sberbank.syncserver2.util.FormatHelper;

import java.io.Serializable;

public class QlikViewObjectId implements Serializable {

    private String documentUri;
    private String objectId;

    public QlikViewObjectId() {
    }

    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        return FormatHelper.stringConcatenator("QlikViewObjectId{",
                "documentUri='", documentUri, '\'',
                ", objectId='", objectId + '\'',
                '}');
    }
}
