package ru.sberbank.qlik.sense.methods;

//{"method":"ResetMadeSelections","handle":8,"params":[],"delta":false,"jsonrpc":"2.0","id":274}
public class ResetMadeSelectionsRequest extends BaseRequest<BaseResponse> {
    public ResetMadeSelectionsRequest(int handle) {
        super("ResetMadeSelections", BaseResponse.class);
        setHandle(handle);
        setParams(new String[]{});
    }
}
