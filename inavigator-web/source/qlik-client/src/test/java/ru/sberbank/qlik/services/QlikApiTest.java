package ru.sberbank.qlik.services;

import org.junit.Ignore;
import org.junit.Test;
import ru.sberbank.qlik.QlikTest;
import ru.sberbank.qlik.sense.methods.GetHyperCubePivotDataResponse;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class QlikApiTest implements QlikTest {
    @Ignore("Работают только при доступности сервера QlikSense")
    @Test
    public void getQlikSenseDataTest() throws Exception {
        QlikApi api = new QlikApi(SERVER, SERVER_PORT, API_CONTEXT,
                ROOT_CERTIFICATE,
                CLIENT_CERTIFICATE,
                CLIENT_KEY_PATH,
                CLIENT_KEY_PASSWORD,
                USER,
                ALPHA_DOMAIN);
        GetDataRequest request = new GetDataRequest();
        request.setDocumentId(DOCUMENT_ID);
        request.setObjectIds(Arrays.asList(
                BALANCE_TABLE_ID,
                BALANCE_BARCHAR_ID
                //new ObjectRequest("URaWq")
        ));

        GetDataResponse getDataResponse = api.getQlikSenseDataAsync(request).get();


        for (ObjectData objectData : getDataResponse.getObjectDatas()) {
            if(objectData.getType().equals("BARCHART")){
                System.out.println("Barchart " + objectData.getValues().size());
                for (Value value : objectData.getValues()) {
                    System.out.printf(value.getSValue() + ",");
                }
                System.out.println(";");
            } else {
                System.out.println();
                System.out.println("Table " + objectData.getValues().size() + "x" +objectData.getValues().get(0).getValues().size());
                                for (Value row : objectData.getValues()) {
                    for (Value cell : row.getValues()) {
                        System.out.print(cell.getSValue() + ",");
                    }
                    System.out.println(";");
                }
            }

            assertFalse("Метод выполнился без ошибок", objectData.getError());
        }
    }

    @Test
    public void convertPivotPageToDataTest() throws IOException {
        GetHyperCubePivotDataResponse getHyperCubePivotDataResponse = QlikApiUtils.getObjectMapper().readValue(new File(getClass().getClassLoader().getResource("GetHyperCubePivotDataResponseSample.json").getFile()), GetHyperCubePivotDataResponse.class);
        QlikApi.PivotModePageData pivotModePageData = QlikApi.convertPivotPageToData(getHyperCubePivotDataResponse.result.qDataPages);
        assertEquals(pivotModePageData.getLeft().size(), 1);
        assertEquals(pivotModePageData.getTop().size(), 44);
        assertEquals(pivotModePageData.getRows().size(), pivotModePageData.getLeft().size());
        assertEquals(pivotModePageData.getRows().get(0).getValues().size(), pivotModePageData.getTop().size());
        assertEquals(pivotModePageData.getRows().get(0).getValues().get(1).getSValue(), "48,86607");
    }
}
