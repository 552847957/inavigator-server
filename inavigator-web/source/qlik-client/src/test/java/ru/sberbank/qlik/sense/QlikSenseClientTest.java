package ru.sberbank.qlik.sense;


import org.apache.log4j.Logger;
import org.junit.*;
import ru.sberbank.qlik.QlikTest;
import ru.sberbank.qlik.sense.methods.*;
import ru.sberbank.qlik.sense.objects.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

@Ignore("Работают только при доступности сервера Qlik")
public class QlikSenseClientTest implements QlikTest {
    private static final Logger log = org.apache.log4j.LogManager.getLogger(QlikSenseClientTest.class);
    private QlikSenseClient qlikSenseClient;

    @Before
    public void connect() throws URISyntaxException {
        this.qlikSenseClient = new QlikSenseClient(SERVER,
                SERVER_PORT,
                API_CONTEXT,
                ROOT_CERTIFICATE,
                CLIENT_CERTIFICATE,
                CLIENT_KEY_PATH,
                CLIENT_KEY_PASSWORD,
                USER,
                ALPHA_DOMAIN);
        this.qlikSenseClient.connect();
        if (!qlikSenseClient.isConnected()) {
            Assert.fail("Не удалось подключиться к Qlik Sense");
        }
    }

    @After
    public void disconnect() throws IOException {
        this.qlikSenseClient.disconnect();
    }

    @Test
    public void testConnectionToQlikServer() {
        Assert.assertTrue("Соединение установлено", qlikSenseClient.isConnected());
    }

    @Test
    public void testOpenDoc() throws Exception {
        OpenDocRequest request = qlikSenseClient.call(new OpenDocRequest(DOCUMENT_ID));
        OpenDocResponse response = request.getResponse();
        if (response.getError() == null) {
            String qGenericId = response.getResult().qReturn.qGenericId;
            QType qType = response.getResult().qReturn.qType;
            Assert.assertEquals("Получен ID документа", qGenericId, DOCUMENT_ID);
            Assert.assertEquals("Тип документа верный", qType, QType.DOC);
        } else {
            Assert.fail();
        }
    }

    @Test
    public void testGetObjectMethod() throws Exception {
        OpenDocResponse response = qlikSenseClient.call(new OpenDocRequest(DOCUMENT_ID)).getResponse();
        Integer documentHandle = response.getResult().qReturn.qHandle;
        GetObjectResponse getObjectResponse = qlikSenseClient.call(new GetObjectRequest(documentHandle, BALANCE_TABLE_ID)).getResponse();
        String tableId = getObjectResponse.getResult().qReturn.qGenericId;
        Assert.assertEquals(BALANCE_TABLE_ID, tableId);
    }

    @Test
    public void testGetInfoMethod() throws Exception {
        OpenDocResponse response = qlikSenseClient.call(new OpenDocRequest(DOCUMENT_ID)).getResponse();
        Integer documentHandle = response.getResult().qReturn.qHandle;
        GetObjectResponse getObjectResponse = qlikSenseClient.call(new GetObjectRequest(documentHandle, BALANCE_TABLE_ID)).getResponse();
        Integer objectHandle = getObjectResponse.getResult().qReturn.qHandle;
        GetEffectivePropertiesResponse.QProp qProp = qlikSenseClient.call(new GetEffectivePropertiesRequest(objectHandle)).getResponse().getResult().qProp;
        QType qType = qProp.qInfo.getqType();
        Assert.assertEquals(qType, QType.PIVOT_TABLE);
    }

    @Test
    public void testApplyPatchMethod() throws Exception {
        OpenDocRequest request = qlikSenseClient.call(new OpenDocRequest(DOCUMENT_ID));
        OpenDocResponse response = request.getResponse();
        Integer documentHandle = response.getResult().qReturn.qHandle;
        GetObjectRequest getObjectRequest = qlikSenseClient.call(new GetObjectRequest(documentHandle, BALANCE_TABLE_ID));
        GetObjectResponse getObjectResponse = getObjectRequest.getResponse();
        GetObjectResponse.QReturn qReturn = getObjectResponse.getResult().qReturn;
        Integer objectHandle = qReturn.qHandle;
        ApplyPatchesRequest applyPatchesRequest = fixHypercubeLayout(objectHandle);
        Object error = applyPatchesRequest.getResponse().getError();
        Assert.assertNull(error);
    }

    @Test
    public void testGetLayoutMethodMethod() throws Exception {
        OpenDocRequest request = qlikSenseClient.call(new OpenDocRequest(DOCUMENT_ID));
        OpenDocResponse response = request.getResponse();
        Integer documentHandle = response.getResult().qReturn.qHandle;
        GetObjectRequest getObjectRequest = qlikSenseClient.call(new GetObjectRequest(documentHandle, BALANCE_TABLE_ID));
        GetObjectResponse getObjectResponse = getObjectRequest.getResponse();
        GetObjectResponse.QReturn qReturn = getObjectResponse.getResult().qReturn;
        Integer objectHandle = qReturn.qHandle;
        fixHypercubeLayout(objectHandle);
        GetLayoutRequest getLayoutRequest = qlikSenseClient.call(new GetLayoutRequest(objectHandle));
        QLayout qLayout = getLayoutRequest.getResponse().result.qLayout;
    }

    @Test
    public void testGetHypercubeDataMethod() throws Exception {
        OpenDocRequest request = qlikSenseClient.call(new OpenDocRequest(DOCUMENT_ID));
        OpenDocResponse response = request.getResponse();
        Integer documentHandle = response.getResult().qReturn.qHandle;
        GetObjectRequest getObjectRequest = qlikSenseClient.call(new GetObjectRequest(documentHandle, BALANCE_TABLE_ID));
        GetObjectResponse getObjectResponse = getObjectRequest.getResponse();
        GetObjectResponse.QReturn qReturn = getObjectResponse.getResult().qReturn;
        Integer objectHandle = qReturn.qHandle;
        fixHypercubeLayout(objectHandle);
        GetLayoutRequest getLayoutRequest = qlikSenseClient.call(new GetLayoutRequest(objectHandle));
        QLayout qLayout = getLayoutRequest.getResponse().result.qLayout;
        QSize qSize = qLayout.qHyperCube.qSize;
        int width = qSize.qcx;
        int height = qSize.qcy;
        int totalCells = width * height;

        int rowOnPage = (int) Math.floor(200 / width);
        int pagesCount = (int) Math.ceil(totalCells / (rowOnPage * width * 1.0));

        QMatrixItem[][] allData = new QMatrixItem[height][];
        for (int pageIndex = 0; pageIndex < pagesCount; pageIndex++) {
            try {
                QDataPage[] qDataPages = qlikSenseClient.call(new GetHyperCubeDataRequest(objectHandle, 0, pageIndex * rowOnPage, width, rowOnPage)).getResponse().result.qDataPages;
                QMatrixItem[][] pageData = qDataPages[0].qMatrix;
                System.arraycopy(pageData, 0, allData, rowOnPage * pageIndex, pageData.length);
            } catch (Exception e) {
                log.error(e);
            }
        }

        int size = allData.length;
        Assert.assertEquals(height, size);
    }

    private ApplyPatchesRequest fixHypercubeLayout(Integer objectHandle) throws Exception {
        HashMap<String, Object> changes = new HashMap<String, Object>();
        changes.put("/qHyperCubeDef/qInitialDataFetch/0", "{\"qLeft\": 0,\"qTop\": 0, \"qWidth\": 0,\"qHeight\": 0}");
        return qlikSenseClient.call(new ApplyPatchesRequest(objectHandle, changes));
    }
}
