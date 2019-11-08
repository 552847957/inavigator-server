package ru.sberbank.qlik.sense.methods;

import ru.sberbank.qlik.sense.objects.NxPivotPage;

public class GetHyperCubePivotDataResponse extends BaseResponse<GetHyperCubePivotDataResponse.Result> {
    public static class Result {
        public NxPivotPage[] qDataPages;
    }
}
