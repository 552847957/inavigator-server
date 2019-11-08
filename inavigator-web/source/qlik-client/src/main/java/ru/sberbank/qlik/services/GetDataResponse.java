package ru.sberbank.qlik.services;

import java.util.List;

/**
 * Данные объектов документа
 */
public class GetDataResponse {
    /**
     * Id документа
     */
    private String documentId;
    /**
     * Данные
     */
    private List<ObjectData> objectDatas;

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public List<ObjectData> getObjectDatas() {
        return objectDatas;
    }

    public void setObjectDatas(List<ObjectData> objectDatas) {
        this.objectDatas = objectDatas;
    }
}
