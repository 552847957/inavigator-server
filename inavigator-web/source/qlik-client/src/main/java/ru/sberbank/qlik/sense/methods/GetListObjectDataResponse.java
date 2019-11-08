package ru.sberbank.qlik.sense.methods;

import ru.sberbank.qlik.sense.objects.QDataPage;

public class GetListObjectDataResponse extends BaseResponse<GetListObjectDataResponse.Response> {
    public static class Response {
        public QDataPage[] qDataPages;
    }
}
