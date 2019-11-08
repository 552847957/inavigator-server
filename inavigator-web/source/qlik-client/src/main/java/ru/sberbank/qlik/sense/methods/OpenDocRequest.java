package ru.sberbank.qlik.sense.methods;

public class OpenDocRequest extends BaseRequest<OpenDocResponse> {
    public OpenDocRequest(String docId) {
        super("OpenDoc", OpenDocResponse.class);
        setParams(new String[]{docId});
    }
}
