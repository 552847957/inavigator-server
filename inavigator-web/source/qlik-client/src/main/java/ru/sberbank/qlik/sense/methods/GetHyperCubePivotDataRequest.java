package ru.sberbank.qlik.sense.methods;

import ru.sberbank.qlik.sense.objects.NxRect;

import java.util.HashMap;

public class GetHyperCubePivotDataRequest extends BaseRequest<GetHyperCubePivotDataResponse> {
    public GetHyperCubePivotDataRequest(int handle, int left, int top, int width, int height) {
        super("GetHyperCubePivotData", GetHyperCubePivotDataResponse.class);
        setHandle(handle);
        setOutKey(null);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("qPath", "/qHyperCubeDef");
        params.put("qPages", new Object[]{new NxRect(left, top, width, height)});
        setParams(params);
    }
}
