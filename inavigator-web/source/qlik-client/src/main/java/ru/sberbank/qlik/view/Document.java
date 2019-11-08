package ru.sberbank.qlik.view;

import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;

public class Document {
    private static final Logger log = LogManager.getLogger(Document.class);
    private final Dispatch document;

    public Document(Dispatch document) {
        this.document = document;
    }

    /**
     * Экспортируем Layout документа в файл
     */
    public File exportDocumentLayout(String path, String password1, String password2) {
        File file = new File(path);
        Dispatch.call(this.document, "ExportLayoutFile", file.getAbsolutePath(), password1, password2);
        if(file.exists()) {
            log.debug("Document layout exported to " + file.getAbsolutePath());
        } else {
            log.error("Can`t export Document layout");
        }
        return file;
    }

    public String getName() {
        return Dispatch.call(document, "Name").toString();
    }

    public ArrayList<String> getDocumentBookmarksNames() {
        ArrayList<String> strings = new ArrayList<String>();
        SafeArray getDocBookmarkNames = Dispatch.call(document, "GetDocBookmarkNames").toSafeArray();
        int lBound = getDocBookmarkNames.getLBound();
        int uBound = getDocBookmarkNames.getUBound();
        for (int j = lBound; j <= uBound; j++) {
            strings.add(getDocBookmarkNames.getString(j));
        }
        return strings;
    }

    public ArrayList<IVariableDescription> getDocumentVariableDescriptions() {
        Dispatch dispatchOfValiableDescriptions = Dispatch.call(document, "GetVariableDescriptions").getDispatch();
        int count = Dispatch.get(dispatchOfValiableDescriptions, "Count").getInt();
        ArrayList<IVariableDescription> variableDescriptions = new ArrayList<IVariableDescription>(count);
        for (int v = 0; v < count; v++) {
            Dispatch iVariableDescription = Dispatch.call(dispatchOfValiableDescriptions, "Item", v).getDispatch();
            IVariableDescription e = IVariableDescription.get(iVariableDescription);
            variableDescriptions.add(e);
        }

        for (IVariableDescription variableDescription : variableDescriptions) {
            Dispatch variableDispatch = Dispatch.call(document, "Variables", variableDescription.Name).getDispatch();
            Dispatch getContent = Dispatch.call(variableDispatch, "GetContent").getDispatch();
            IAlfaNumString iAlfaNumString = IAlfaNumString.get(getContent);
            variableDescription.Content = iAlfaNumString;
        }
        return variableDescriptions;
    }

    public ObjectData getDocumentObjectData(String objectId, boolean deleteCvs) {
        log.debug("Get data for object: " + objectId);
        ObjectData objectData;
        try {
            SheetObject sheetObject = getSheetObject(document, objectId);
            //
            //Dispatch objectDispatch = getObjectFromSheets(document, objectId);
            objectData = sheetObject.getObjectData(deleteCvs);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            objectData = new ObjectData();
            objectData.setError(true);
            objectData.setErrorMessage(e.getMessage());
        }

        objectData.setId(objectId);
        return objectData;
    }

    private static SheetObject getSheetObject(Dispatch document, String objectId) {
        Dispatch getSheetObject = Dispatch.call(document, "GetSheetObject", objectId).getDispatch();
        SheetObject sheetObject = new SheetObject(getSheetObject);
        String objectId1 = sheetObject.getObjectId();
        log.debug("Object ID=" + objectId1 + " type=" + sheetObject.getObjectType());
        return sheetObject;
    }


    private SheetObject getObjectFromSheets(String objectId) {
        int sheetsCount = getSheetsCount();
        for (int s = 0; s < sheetsCount; s++) {
            Sheet sheet = getSheet(s);
            SheetObject so = sheet.getObjectById(objectId);
            if(so != null) return so;
        }
        return null;
    }

    private Sheet getSheet(int s) {
        Dispatch sheetDispatch = Dispatch.call(document, "GetSheet", s).getDispatch();
        //QlikViewComUtils.activate(sheetDispatch);
        return new Sheet(sheetDispatch);
    }

    private int getSheetsCount() {
        log.debug("Get Sheets count");
        int sheetsCount = Dispatch.call(document, "NoOfSheets").getInt();
        log.debug("Sheets count = " + sheetsCount);
        return sheetsCount;
    }
}
