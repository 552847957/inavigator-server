package ru.sberbank.qlik.sense.methods;

import ru.sberbank.qlik.sense.objects.NxRect;

public class GetListObjectDataRequest extends BaseRequest<GetListObjectDataResponse> {
    public GetListObjectDataRequest(int handle, int left, int top, int width, int height) {
        super("GetListObjectData", GetListObjectDataResponse.class);
        setHandle(handle);
        setParams(new Object[]{"/qListObjectDef", new NxRect[]{new NxRect(left, top, width, height)}});
    }
}
