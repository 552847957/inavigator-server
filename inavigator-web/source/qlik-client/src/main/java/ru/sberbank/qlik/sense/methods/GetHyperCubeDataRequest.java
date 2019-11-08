package ru.sberbank.qlik.sense.methods;

import ru.sberbank.qlik.sense.objects.NxRect;

import java.util.HashMap;

public class GetHyperCubeDataRequest extends BaseRequest<GetHyperCubeDataResponse> {
    public GetHyperCubeDataRequest(int handle, int left, int top, int width, int height) {
        super("GetHyperCubeData", GetHyperCubeDataResponse.class);
        setHandle(handle);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("qPath", "/qHyperCubeDef");
        params.put("qPages", new Object[]{new NxRect(left, top, width, height)});
        setParams(params);
    }
}
