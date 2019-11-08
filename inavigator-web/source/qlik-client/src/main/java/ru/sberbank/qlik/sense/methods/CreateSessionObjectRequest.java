package ru.sberbank.qlik.sense.methods;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.sberbank.qlik.services.QlikApiUtils;

import java.io.IOException;
import java.util.ArrayList;

//{"method":"CreateSessionObject","handle":1,"params":[{"qListObjectDef":{"qDef":{"qFieldLabels":["Банки"],"qFieldDefs":["Банки"],"autoSort":true,"cId":"","qSortCriterias":[{"qSortByState":1,"qSortByAscii":1,"qSortByNumeric":1,"qSortByLoadOrder":1}]},"qLibraryId":"","qShowAlternatives":true,"qInitialDataFetch":[{"qTop":0,"qLeft":0,"qWidth":0,"qHeight":0}]},"showTitles":true,"title":"","subtitle":"","footnote":"","showDetails":false,"qInfo":{"qType":"listbox"},"visualization":"listbox"}],"delta":false,"jsonrpc":"2.0","id":322}
//{"method":"CreateSessionObject","handle":1,"params":[{"qListObjectDef":{"qDef":{"qFieldLabels":["name"],"qFieldDefs":["name"],"autoSort":true,"cId":"","qSortCriterias":[{"qSortByState":1,"qSortByAscii":1,"qSortByNumeric":1,"qSortByLoadOrder":1}]},"qLibraryId":"","qShowAlternatives":true,"qInitialDataFetch":[{"qTop":0,"qLeft":0,"qWidth":0,"qHeight":0}]},"showTitles":true,"title":"","subtitle":"","footnote":"","showDetails":false,"qInfo":{"qType":"listbox"},"visualization":"listbox"}],"delta":false,"jsonrpc":"2.0","id":46}
public class CreateSessionObjectRequest extends BaseRequest<CreateSessionObjectResponse> {
    public CreateSessionObjectRequest(int handle, String field) throws IOException {
        super("CreateSessionObject", CreateSessionObjectResponse.class);
        setHandle(handle);
        ArrayList<Object> params = new ArrayList<Object>();
        ObjectMapper objectMapper = QlikApiUtils.getObjectMapper();
        Object o = objectMapper.readValue("{" +
                "            \"qListObjectDef\":{" +
                "                \"qDef\":{" +
                "                    \"qFieldLabels\":[\""+ field + "\"],\"qFieldDefs\":[\""+ field + "\"],\"autoSort\":true, \"cId\":\"\", \"qSortCriterias\":[{" +
                "                        \"qSortByState\":1, \"qSortByAscii\":1, \"qSortByNumeric\":1, \"qSortByLoadOrder\":1" +
                "                    }]},\"qLibraryId\":\"\", \"qShowAlternatives\":true, \"qInitialDataFetch\":[{" +
                "                    \"qTop\":0, \"qLeft\":0, \"qWidth\":0, \"qHeight\":0" +
                "                }]},\"showTitles\":true, \"title\":\"\", \"subtitle\":\"\", \"footnote\":\"\", \"showDetails\":false, \"qInfo\":{" +
                "                \"qType\":\"listbox\"" +
                "            },\"visualization\":\"listbox\"" +
                "        }", Object.class);
        params.add(o);
        setParams(params);
    }
}
