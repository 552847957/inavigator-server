package ru.sberbank.qlik.sense.methods;

public class GetEffectivePropertiesRequest extends BaseRequest<GetEffectivePropertiesResponse> {
    public GetEffectivePropertiesRequest(int handle) {
        super("GetEffectiveProperties", GetEffectivePropertiesResponse.class);
        setHandle(handle);
        setParams(new String[]{});
    }
}
