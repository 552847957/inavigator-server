package ru.sberbank.qlik.sense.methods;

import java.util.HashMap;

public class ExportDataRequest extends BaseRequest<ExportDataResponse> {
    public ExportDataRequest(int handle) {
        super("ExportData", ExportDataResponse.class);
        setHandle(handle);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("qFileType", "OOXML");
        params.put("qPath", "/qHyperCubeDef");
        params.put("qExportState", "P");
        setParams(params);
    }
}
