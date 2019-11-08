package ru.sberbank.qlik.sense.methods;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class ExportDataResponse extends BaseResponse<ExportDataResponse.Result> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        public String qUrl;
        public int[] qWarnings;
    }
}
