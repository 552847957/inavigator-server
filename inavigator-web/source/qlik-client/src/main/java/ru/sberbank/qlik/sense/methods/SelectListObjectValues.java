package ru.sberbank.qlik.sense.methods;

public class SelectListObjectValues extends BaseRequest<SelectListObjectValuesResponse> {
    public SelectListObjectValues(int handle, int elementIndex) {
        super("SelectListObjectValues", SelectListObjectValuesResponse.class);
        setHandle(handle);
        Object[] params = {"/qListObjectDef", new int[]{elementIndex}, true};
        setParams(params);
    }
}
