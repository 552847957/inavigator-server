package ru.sberbank.qlik.view;


import com.jacob.com.Dispatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.sberbank.qlik.view.QlikViewComUtils.evaluate;
import static ru.sberbank.qlik.view.QlikViewComUtils.getStringPropertyValue;

public class TextObject extends SheetObject {
    public TextObject(SheetObject sheetObject) {
        super(sheetObject.getObjectDispatch());
    }

    public void fillObjectData(ObjectData objectData) {
        Dispatch objectDispatch = getObjectDispatch();
        ArrayList<List<String>> matrix = new ArrayList<List<String>>();
        ArrayList<String> row = new ArrayList<String>();
        objectData.setColumns(Collections.singletonList(""));
        Dispatch getProperties = Dispatch.call(objectDispatch, "GetProperties").getDispatch();
        Dispatch layout = Dispatch.get(getProperties, "Layout").getDispatch();
        String textString = getStringPropertyValue(layout, "Text");
        if (textString.startsWith("=")) {
            IFieldValue iFieldValue = evaluate(objectDispatch, textString);
            row.add(iFieldValue.isNumeric() ? iFieldValue.getNumber().toString() : iFieldValue.getText());
        } else {
            row.add(textString);
        }
        matrix.add(row);
        objectData.setMatrix(matrix);
    }
}
