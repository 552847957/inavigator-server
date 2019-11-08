package ru.sberbank.qlik.view;

import com.jacob.com.Dispatch;

public class IVariableDescription {
    public boolean IsConfig;
    public boolean IsReserved;
    public String Name;
    public String RawValue;
    public IAlfaNumString ShownValue;
    public IAlfaNumString Content;

    public static IVariableDescription get(Dispatch dispatch) {
        IVariableDescription value = new IVariableDescription();
        //value.IsConfig = Dispatch.get(dispatch, "IsConfig ").getBoolean();
        //value.IsReserved = Dispatch.get(dispatch, "IsReserved  ").getBoolean();
        value.Name = Dispatch.get(dispatch, "Name").getString();
        value.RawValue = Dispatch.get(dispatch, "RawValue").getString();
        value.ShownValue = IAlfaNumString.get(Dispatch.get(dispatch, "ShownValue").getDispatch());
        return value;
    }
}
