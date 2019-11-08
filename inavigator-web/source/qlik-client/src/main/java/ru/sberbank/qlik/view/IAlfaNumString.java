package ru.sberbank.qlik.view;

import com.jacob.com.Dispatch;

public class IAlfaNumString {
    public boolean IsNum;
    public String String;

    public static IAlfaNumString get(Dispatch dispatch) {
        IAlfaNumString value = new IAlfaNumString();
        value.IsNum = Dispatch.get(dispatch, "IsNum").getBoolean();
        value.String = Dispatch.get(dispatch, "String").getString();
        return value;
    }
}
