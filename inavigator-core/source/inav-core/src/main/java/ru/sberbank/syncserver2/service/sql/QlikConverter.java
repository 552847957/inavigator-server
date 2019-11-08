package ru.sberbank.syncserver2.service.sql;

import ru.sberbank.qlik.services.GetDataResponse;
import ru.sberbank.qlik.services.ObjectData;
import ru.sberbank.qlik.services.Value;
import ru.sberbank.syncserver2.service.sql.query.*;

import java.util.ArrayList;
import java.util.List;

public class QlikConverter {
    public static List<DataResponse> convertToDataResponse(GetDataResponse response) {
        ArrayList<DataResponse> result = new ArrayList<DataResponse>();
        List<ObjectData> objectDatas = response.getObjectDatas();
        if(objectDatas != null && !objectDatas.isEmpty()) {
            for (ObjectData objectData : objectDatas) {
                DataResponse dataResponse = new DataResponse();
                if(objectData.isError()) {
                    dataResponse.setResult(DataResponse.Result.FAIL);
                    dataResponse.setError(objectData.getErrorMessage());
                } else {
                    List<Value> values = objectData.getValues();
                    dataResponse.setResult(DataResponse.Result.OK);
                    DatasetMetaData metadata = new DatasetMetaData();
                    List<String> topValues = objectData.getTop().getValues();
                    if (topValues != null) {
                        for (String s : topValues) {
                            DatasetFieldMetaData field = new DatasetFieldMetaData();
                            field.setName(s);
                            field.setType(FieldType.STRING);
                            metadata.addField(field);
                        }
                    }
                    Dataset dataset = new Dataset();
                    if (values != null && !values.isEmpty()) {
                        metadata.setRowCount(values.size());
                        for (Value rowValue : values) {
                            DatasetRow row = new DatasetRow();
                            for (Value colValue : rowValue.getValues()) {
                                row.addValue(colValue.getSValue());
                            }
                            dataset.addRow(row);
                        }
                    } else {
                        metadata.setRowCount(0);
                    }

                    dataResponse.setMetadata(metadata);
                    dataResponse.setDataset(dataset);
                }
                result.add(dataResponse);
            }
        }
        return result;
    }
}
