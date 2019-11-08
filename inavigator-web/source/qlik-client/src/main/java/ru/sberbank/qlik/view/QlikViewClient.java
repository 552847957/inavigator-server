package ru.sberbank.qlik.view;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class QlikViewClient {
    private static final Logger log = LogManager.getLogger(QlikViewClient.class);

    public static Response getObjectsData(QlikViewClientRequest request) {
        Response response = new Response();
        String documentUri = request.getDocumentUri();
        response.setDocument(documentUri);
        long start = System.currentTimeMillis();

        try {
            Application application = new Application();

            // Проверяем запущен ли QlikView клиент,
            // если запущен то считаем чт озавис и пробум убить процесс
            if(application.checkQViewProcess()) {
                application.destroyQViewProcess();
            }

            application.connect();

            Document document = application.openDocument(documentUri, request.getUser(), request.getPassword());

            //printBookmarksForDebug(document);

            //printVariablesToDebug(document);

            List<String> objectIds = request.getObjectIds();
            response.setData(new ArrayList<ObjectData>(objectIds.size()));
            for (String objectId : objectIds) {
                ObjectData documentObjectData = document.getDocumentObjectData(objectId, request.isDeleteCvs());
                response.getData().add(documentObjectData);
            }

            if (request.isQuit()) {
                application.quit();
            }
        } catch (Exception e) {
            response.setError(true);
            response.setErrorMessage(e.getMessage());
        }
        long duration = System.currentTimeMillis() - start;
        log.debug("Execution time =" + duration);

        return response;
    }

    private static void printBookmarksForDebug(Document document) {
        ArrayList<String> bookmarksNames = document.getDocumentBookmarksNames();
        if (!bookmarksNames.isEmpty()) {
            log.debug("Bookmarks:");
            for (String bookmarkName : bookmarksNames) {
                log.debug(bookmarkName);
            }
        }
    }

    private static void printVariablesToDebug(Document document) {
        ArrayList<IVariableDescription> variableDescriptions = document.getDocumentVariableDescriptions();
        for (IVariableDescription variableDescription : variableDescriptions) {
            log.debug(variableDescription.Name + " = " + variableDescription.RawValue);
        }
    }
}

