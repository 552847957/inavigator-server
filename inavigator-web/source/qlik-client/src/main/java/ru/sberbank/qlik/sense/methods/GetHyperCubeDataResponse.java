package ru.sberbank.qlik.sense.methods;

import ru.sberbank.qlik.sense.objects.QDataPage;

public class GetHyperCubeDataResponse extends BaseResponse<GetHyperCubeDataResponse.Result> {
    public static class Result {
        public QDataPage[] qDataPages;
    }
}
