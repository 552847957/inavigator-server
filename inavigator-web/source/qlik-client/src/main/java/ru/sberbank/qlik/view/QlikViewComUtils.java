package ru.sberbank.qlik.view;

import com.jacob.com.Dispatch;

import java.util.Arrays;
import java.util.List;

public class QlikViewComUtils {
    public static void activate(Dispatch objectDispatch) {
        Dispatch.call(objectDispatch, "Activate");
    }

    public static String getStringPropertyValue(Dispatch dispatch, String property) {
        Dispatch windowTitle = Dispatch.get(dispatch, property).getDispatch();
        return Dispatch.get(windowTitle, "v").getString();
    }

    /**
     * Вичисляем значение формулы
     * @param objectDispatch
     * @param textString
     * @return
     */
    public static IFieldValue evaluate(Dispatch objectDispatch, String textString) {
        Dispatch evaluate = Dispatch.call(objectDispatch, "Evaluate", textString).getDispatch();
        boolean aDefault = Dispatch.get(evaluate, "Default").getBoolean();
        Dispatch value = Dispatch.get(evaluate, "Value").getDispatch();
        IFieldValue iFieldValue = new IFieldValue();
        iFieldValue.setNumeric(Dispatch.get(value, "IsNumeric").getBoolean());
        if (iFieldValue.isNumeric()) {
            iFieldValue.setNumber(Dispatch.get(value, "Number").getDouble());
        } else {
            iFieldValue.setText(Dispatch.get(value, "Text").getString());
        }
        return iFieldValue;
    }

    // TODO Понять как вычислить значения в заголовке таблицы
    public static List<String> evaluates(Dispatch dispatch, String[] strings) {
        return Arrays.asList(strings);
//        ArrayList<String> evaluatedStrings = new ArrayList<String>(strings.length);
//        for (int i = 0; i < strings.length; i++) {
//            String si = strings[i];
//            if(si.startsWith("\uFEFF")) {
//                si = si.substring(1);
//            }
//            if(si.startsWith("=")) {
//                IFieldValue evaluatedField = evaluate(dispatch, si);
//                String evaluate = evaluatedField.isNumeric() ? evaluatedField.getNumber().toString() : evaluatedField.getText();
//                evaluatedStrings.set(i, evaluate);
//            } else {
//                evaluatedStrings.set(i, si);
//            }
//        }
//        return evaluatedStrings;
    }
}
