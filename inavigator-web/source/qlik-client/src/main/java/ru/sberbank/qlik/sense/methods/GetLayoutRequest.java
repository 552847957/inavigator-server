package ru.sberbank.qlik.sense.methods;

import java.util.HashMap;

public class GetLayoutRequest extends BaseRequest<GetLayoutResponse> {
    public GetLayoutRequest(int handle) {
        super("GetLayout", GetLayoutResponse.class);
        setHandle(handle);
        setParams(new HashMap<String, Object>());
    }
}
