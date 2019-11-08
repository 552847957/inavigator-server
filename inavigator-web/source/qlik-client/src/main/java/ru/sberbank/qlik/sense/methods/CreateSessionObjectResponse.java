package ru.sberbank.qlik.sense.methods;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.sberbank.qlik.sense.objects.QType;

public class CreateSessionObjectResponse extends BaseResponse<CreateSessionObjectResponse.Result>{

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        public QReturn qReturn;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QReturn {
        public QType qType;
        public int qHandle;
        public String qGenericType;
        public String qGenericId;
    }
}

