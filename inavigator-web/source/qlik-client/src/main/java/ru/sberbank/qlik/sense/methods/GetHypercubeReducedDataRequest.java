package ru.sberbank.qlik.sense.methods;

import ru.sberbank.qlik.sense.objects.NxRect;

//{"method":"GetHyperCubeReducedData","handle":9,"params":["/qHyperCubeDef",[{"qTop":0,"qLeft":0,"qWidth":2,"qHeight":2000}],-1,"D1"],"delta":true,"jsonrpc":"2.0","id":241}
public class GetHypercubeReducedDataRequest extends BaseRequest<GetHyperCubeDataResponse>{

    public GetHypercubeReducedDataRequest(int handle, int left,int top, int width, int height) {
        super("GetHyperCubeReducedData", GetHyperCubeDataResponse.class);
        setHandle(handle);
        setParams(new Object[]{"/qHyperCubeDef", new NxRect[]{new NxRect(left, top, width, height)}, -1, "D1"});
    }
}
