package ru.sberbank.qlik.sense.methods;

import ru.sberbank.qlik.sense.objects.QType;

public class BeginSelectionsRequest extends BaseRequest<BaseResponse> {
    public BeginSelectionsRequest(int handle, QType type) {
        super("BeginSelections", BaseResponse.class);
        setHandle(handle);
        switch (type) {
            case LISTBOX:
                setParams(new Object[]{new String[]{"/qListObjectDef"}});
                break;
            default:
                setParams(new Object[]{new String[]{"/qHyperCubeDef"}});
                break;
        }

    }
}
