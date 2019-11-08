package ru.sberbank.qlik.view;

import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;

import java.util.ArrayList;

public class Sheet {
    private final Dispatch dispatch;

    public Sheet(Dispatch dispatch) {
        this.dispatch = dispatch;
    }

    public ArrayList<SheetObject> getObjects() {
        ArrayList<SheetObject> objects = new ArrayList<SheetObject>();
        SafeArray sheetObjects = Dispatch.call(dispatch, "GetSheetObjects").toSafeArray();
        int lBound = sheetObjects.getLBound();
        int uBound = sheetObjects.getUBound();
        for (int i = lBound; i <= uBound; i++) {
            Dispatch objectDispatch = sheetObjects.getVariant(i).getDispatch();
            SheetObject sheetObject = new SheetObject(objectDispatch);
            objects.add(sheetObject);
        }
        return objects;
    }

    public SheetObject getObjectById(String objectId) {
        for (SheetObject sheetObject : getObjects()) {
            if(objectId.equals(sheetObject.getObjectId())) {
                return sheetObject;
            }
        }
        return null;
    }
}
