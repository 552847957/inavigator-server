package ru.sberbank.qlik.services;

import java.util.List;

/**
 * Запрос для получения данных по id документа и списку id объектов
 */
public class GetDataRequest {

    /**
     * Тип сервера отчетов {@link QlikApi#SERVER_TYPE_QLIK_VIEW} или {@link QlikApi#SERVER_TYPE_QLIK_SENSE}
     */
    private int serverType = QlikApi.SERVER_TYPE_QLIK_SENSE;
    /**
     * ID документа
     */
    private String documentId;
    /**
     * ID объектов документа
     */
    //private List<ObjectRequest> objects;
    private List<String> objectIds;

    public GetDataRequest(String documentId, List<String> objectIds) {
        this.documentId = documentId;
        this.objectIds = objectIds;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public List<String> getObjectIds() {
        return objectIds;
    }

    public void setObjectIds(List<String> objectIds) {
        this.objectIds = objectIds;
    }

    public GetDataRequest() {}

    public int getServerType() {
        return serverType;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }
}
