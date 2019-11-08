package ru.sberbank.qlik.sense.methods;

import ru.sberbank.qlik.sense.objects.QLayout;

public class GetLayoutResponse extends BaseResponse<GetLayoutResponse.Result> {
    public static class Result {
        public QLayout qLayout;
    }
}
