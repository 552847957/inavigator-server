package ru.sberbank.qlik.sense.methods;

import java.util.HashMap;

public class GetObjectRequest extends BaseRequest<GetObjectResponse> {
    public GetObjectRequest(int handle, String objectId) {
        super("GetObject", GetObjectResponse.class);
        HashMap<Object, Object> params = new HashMap<Object, Object>();
        params.put("qId", objectId);
        setHandle(handle);
        setParams(params);
    }
}
