package ru.sberbank.syncserver2.service.sql;

import org.apache.commons.codec.binary.Base64;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.Dataset;
import ru.sberbank.syncserver2.service.sql.query.DatasetRow;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

import java.io.Serializable;
import java.lang.reflect.Method;

public class OnlineRequestQlikSenseExecutorAnswer implements Answer, Serializable {

    public static String returnJSON = "{id: 1, documentId: \"DDDDDDD4444\", results: [{objectId:\"HHHHHHH55555\", data: {fld1:1, fld2:2, fld3:${serviceName}}}]}";

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Method m = invocation.getMethod();
        if (m.getName().equalsIgnoreCase("query")) {
            OnlineRequest onlineRequest = (OnlineRequest) invocation.getArguments()[0];
            String serviceName = onlineRequest.getService();
            DataResponse response = new DataResponse();
            Dataset ds = new Dataset();
            DatasetRow dsr = new DatasetRow();
            String resultStr = returnJSON.replace("${serviceName}", serviceName);
            dsr.addValue(Base64.encodeBase64String(resultStr.getBytes("UTF8")));
            ds.addRow(dsr);
            response.setDataset(ds);
            response.setResult(DataResponse.Result.OK);
            return response;
        }
        return null;
    }
}
