package ru.sberbank.qlik.sense.methods;
//{method: "EndSelections", handle: 17, params: [true], delta: false, jsonrpc: "2.0", id: 83}

public class EndSelectionsRequest extends BaseRequest<BaseResponse>{
    public EndSelectionsRequest(int handle) {
        super("EndSelections", BaseResponse.class);
        setHandle(handle);
        setParams(new Object[]{true});
    }
}
