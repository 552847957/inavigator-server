package ru.sberbank.qlik.services;

import org.apache.log4j.Logger;
import ru.sberbank.qlik.sense.QlikSenseClient;
import ru.sberbank.qlik.sense.methods.*;
import ru.sberbank.qlik.sense.objects.*;
import ru.sberbank.qlik.view.QlikViewClient;
import ru.sberbank.qlik.view.QlikViewClientRequest;
import ru.sberbank.qlik.view.Response;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QlikApi {
    public static final int SERVER_TYPE_QLIK_VIEW = 0;
    public static final int SERVER_TYPE_QLIK_SENSE = 1;
    private static final Logger log = org.apache.log4j.LogManager.getLogger(QlikApi.class);

    private static final int MAX_CELL_ON_PAGE = 100;
    private final String serverHost;
    private final File rootCertificate;
    private final File clientCertificate;
    private final File clientKeyPath;
    private final String clientKeyPassword;
    private final String user;
    private final String domain;
    private final int serverPort;
    private final String serverContext;
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    /**
     * API для подключению к Qlik Engine API
     *
     * @param serverHost        Хост сервера
     * @param serverPort        Порт
     * @param serverContext     Контекст
     * @param rootCertificate   Путь к файлу корневого сертификата Qlik
     * @param clientCertificate Путь к файлу клиентского сертификата
     * @param clientKeyPath     Путь к файлу клиентского ключа
     * @param clientKeyPassword Пароль к файлу клиентского ключа
     * @param user              Имя пользователя
     * @param domain            Домен пользователя
     */
    public QlikApi(String serverHost, Integer serverPort, String serverContext,
                   File rootCertificate, File clientCertificate, File clientKeyPath,
                   String clientKeyPassword, String user, String domain) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.serverContext = serverContext;
        this.rootCertificate = rootCertificate;
        this.clientCertificate = clientCertificate;
        this.clientKeyPath = clientKeyPath;
        this.clientKeyPassword = clientKeyPassword == null ? "" : clientKeyPassword;
        this.user = user;
        this.domain = domain;
    }

    /**
     * Получаем данные списка объектов для указанного документа
     *
     * @param request Запрос данных по списку объектов для документа
     * @return Данные о объектах документа
     */
    public Future<GetDataResponse> getQlikSenseDataAsync(final GetDataRequest request) {
        return executor.submit(new GetDataResponseCallable(request, serverHost, serverPort, serverContext, rootCertificate, clientCertificate,
                clientKeyPath, clientKeyPassword, user, domain));
    }

    public static class GetDataResponseCallable implements Callable<GetDataResponse> {
        private String serverHost;
        private int serverPort;
        private String serverContext;
        private File rootCertificate;
        private File clientCertificate;
        private File clientKeyPath;
        private String clientKeyPassword;
        private String qlikUser;
        private String qlikDomain;
        private GetDataRequest request;

        public GetDataResponseCallable(GetDataRequest request, String serverHost, int serverPort, String apiContext, File rootCertificate, File clientCertificatePath, File clientKeyPath, String clientKeyPassword, String qlikUser, String qlikDomain) {
            this.serverHost = serverHost;
            this.serverPort = serverPort;
            this.serverContext = apiContext;
            this.rootCertificate = rootCertificate;
            this.clientCertificate = clientCertificatePath;
            this.clientKeyPath = clientKeyPath;
            this.clientKeyPassword = clientKeyPassword;
            this.qlikUser = qlikUser;
            this.qlikDomain = qlikDomain;
            this.request = request;
        }

        @Override
        public GetDataResponse call() throws Exception {
            QlikSenseClient qlikSenseClient = new QlikSenseClient(serverHost, serverPort, serverContext, rootCertificate, clientCertificate, clientKeyPath, clientKeyPassword, qlikUser, qlikDomain);
            try {
                qlikSenseClient.connect();
                return getQlikSenseData(request, qlikSenseClient);
            } finally {
                qlikSenseClient.disconnect();
            }
        }
    }

    /**
     * Получаем данные списка объектов для указанного документа для QlikSense
     *
     * @param request Запрос данных по списку объектов для документа
     * @return Данные о объектах документа
     */
    public static Future<GetDataResponse> getQlikSenseDataAsync(final GetDataRequest request, final QlikSenseClient qlikSenseClient) {
        return executor.submit(new Callable<GetDataResponse>() {
            @Override
            public GetDataResponse call() throws Exception {
                return getQlikSenseData(request, qlikSenseClient);
            }
        });
    }

    /**
     * Получаем данные списка объектов для указанного документа для QlikView
     *
     * @param request Запрос данных по списку объектов для документа
     * @return Данные о объектах документа
     */
    public static Future<GetDataResponse> getQlikViewDataAsync(final GetDataRequest request, final String login, final String password) {
        return singleThreadExecutor.submit(new Callable<GetDataResponse>() {
            @Override
            public GetDataResponse call() throws Exception {
                return getQlikViewData(request, login, password);
            }
        });
    }

    private static GetDataResponse getQlikViewData(GetDataRequest request, String login, String password) throws Exception {
        GetDataResponse getDataResponse = new GetDataResponse();
        ArrayList<String> ids = new ArrayList<String>();
        for (String objectRequest : request.getObjectIds()) {
            ids.add(objectRequest);
        }

        ArrayList<ObjectData> objectDatas = new ArrayList<ObjectData>();
        QlikViewClientRequest clientRequest = new QlikViewClientRequest()
                .setDocumentUri(request.getDocumentId())
                .setUser(login)
                .setPassword(password)
                .setObjectIds(ids)
                .setDeleteCvs(true)
                .setQuit(true);
        Response objectsData = QlikViewClient.getObjectsData(clientRequest);
        for (ru.sberbank.qlik.view.ObjectData objectData : objectsData.getData()) {
            ObjectData od = new ObjectData();
            Dimension topDimension = new Dimension();
            topDimension.setLabel(objectData.getTitle());
            topDimension.setValues(objectData.getColumns());
            od.setTop(topDimension);
            ArrayList<Value> rows = new ArrayList<Value>();
            for (List<String> r : objectData.getMatrix()) {
                Value row = new Value();
                ArrayList<Value> cells = new ArrayList<Value>();
                for (String c : r) {
                    Value cell = new Value();
                    cell.setSValue(c);
                    cells.add(cell);
                }
                row.setValues(cells);
                rows.add(row);
            }
            od.setValues(rows);
            objectDatas.add(od);
        }
        getDataResponse.setObjectDatas(objectDatas);
        return getDataResponse;
    }

    private static GetDataResponse getQlikSenseData(GetDataRequest request, QlikSenseClient qlikSenseClient) throws Exception {
        String documentId = request.getDocumentId();
        OpenDocResponse response = qlikSenseClient.call(new OpenDocRequest(documentId)).getResponse();
        Integer documentHandle = response.getResult().qReturn.qHandle;
        log.info("Document " + documentId + " opened " + documentHandle);
        GetDataResponse getDataResponse = new GetDataResponse();
        getDataResponse.setDocumentId(documentId);

        List<String> objectRequests = request.getObjectIds();
        List<ObjectData> data = new ArrayList<ObjectData>(objectRequests.size());
        for (String objectRequest : objectRequests) {
            ObjectData objectData = getObjectData(qlikSenseClient, documentHandle, objectRequest);
            data.add(objectData);
        }

        getDataResponse.setObjectDatas(data);
        return getDataResponse;
    }


    private static ObjectData getObjectData(QlikSenseClient qlikSenseClient, Integer documentHandle, String objectRequest) {
        ObjectData objectData = new ObjectData();
        //String objectId = objectRequest.getId();
        String objectId = objectRequest;
        objectData.setId(objectId);
        Integer objectHandle = null;
        try {
            // Выбираем объект
            GetObjectRequest getObjectRequest = qlikSenseClient.call(new GetObjectRequest(documentHandle, objectId));
            objectHandle = getObjectRequest.getResponse().getResult().qReturn.qHandle;
            log.info("Object " + objectId + " selected " + objectHandle);

            Selection selection = null;//objectRequest.getSelection();
            // Не удается выполнить getLayout для куба, который подгруажет данные для первой страницы.
            // Поэтому сбрасываем предзагрузку.
            clearHypercubeInitialFetch(qlikSenseClient, objectHandle);

            //clear selection
            //qlikSenseClient.call(new ResetMadeSelectionsRequest(objectHandle)).getResponse();
            if(selection != null) {
                List<Integer> rows = selection.getRows();
                List<Integer> cols = selection.getCols();

                if((rows != null && !rows.isEmpty()) || (cols != null && !cols.isEmpty())) {
                    qlikSenseClient.call(new BeginSelectionsRequest(objectHandle, QType.PIVOT_TABLE)).getResponse();
                    qlikSenseClient.call(new SelectPivotCellsRequest(objectHandle, rows, cols)).getResponse();
                }
            }

            QHyperCubeDef qHyperCubeDef = qlikSenseClient.call(new GetEffectivePropertiesRequest(objectHandle)).getResponse().result.qProp.qHyperCubeDef;
            String hypercubeMode = qHyperCubeDef.qMode.name();
            int leftDimensionsCount = qHyperCubeDef.qNoOfLeftDims;
            log.info("Hypercube mode: " + hypercubeMode + " left: " + leftDimensionsCount);

            QLayout layout = qlikSenseClient.call(new GetLayoutRequest(objectHandle)).getResponse().result.qLayout;
            objectData.setType(layout.qInfo.getqType().toString());

            QHyperCube hyperCube = layout.qHyperCube;
            QDimensionInfo[] dimensionInfos = hyperCube.qDimensionInfo;
            for (int i = 0; i < dimensionInfos.length; i++) {
                QDimensionInfo qDimensionInfo = dimensionInfos[i];
                Dimension dimension = new Dimension();
                dimension.setLabel(qDimensionInfo.qFallbackTitle);
                if (i >= leftDimensionsCount) {
                    objectData.setTop(dimension);
                } else {
                    objectData.setLeft(dimension);
                }
            }
            QSize qubeSize = layout.qHyperCube.qSize;
            int cols = qubeSize.qcx;
            int rows = qubeSize.qcy;
            int cells = cols * rows;

            int rowsOnPage = (int) Math.max(Math.floor((double) MAX_CELL_ON_PAGE / (double) cols), Math.max(leftDimensionsCount, 2));
            int pagesCount = (int) Math.ceil(cells / (rowsOnPage * (float) cols));

            for (int pageIndex = 0; pageIndex < pagesCount; pageIndex++) {
                log.debug(objectId + " p:" + pageIndex + " s:" + (pageIndex * rowsOnPage) + "w:" + cols + "h:" + rowsOnPage);
                if ("DATA_MODE_STRAIGHT".equals(hypercubeMode)) {
                    StraightModePageData dataForStraightMode = getDataForStraightMode(qlikSenseClient, objectHandle, cols, rowsOnPage, pageIndex);
                    objectData.getTop().getValues().addAll(dataForStraightMode.getTop());
                    objectData.getValues().addAll(dataForStraightMode.getRows());

                } else if ("DATA_MODE_PIVOT".equals(hypercubeMode)) {
                    PivotModePageData d = getDataForPivotMode(qlikSenseClient, objectHandle, cols, rowsOnPage, pageIndex);
                    objectData.getValues().addAll(d.getRows());
                    objectData.getLeft().getValues().addAll(d.getLeft());
                    objectData.getTop().setValues(d.getTop());

                } else {
                    throw new Exception("Undefined table mode");
                }
            }
        } catch (Exception e) {
            log.error(e);
            objectData.setError(true);
        } finally {
            if(objectHandle != null) {
                try {
                    qlikSenseClient.call(new ResetMadeSelectionsRequest(objectHandle));
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        return objectData;
    }

    private static PivotModePageData getDataForPivotMode(QlikSenseClient qlikSenseClient, Integer objectHandle, int cols, int rowsOnPage, int pageIndex) throws Exception {
        GetHyperCubePivotDataRequest method = qlikSenseClient.call(new GetHyperCubePivotDataRequest(objectHandle, 0, pageIndex * rowsOnPage, cols, rowsOnPage));
        return convertPivotPageToData(method.getResponse().result.qDataPages);
    }

    static PivotModePageData convertPivotPageToData(NxPivotPage[] pages) {
        List<Value> rows = new ArrayList<Value>();
        List<String> topLabels = new ArrayList<String>();
        List<String> leftLabels = new ArrayList<String>();
        for (NxPivotPage page : pages) {
            if(topLabels.isEmpty()) {
                topLabels = getTopLabelsForPage(page);
            }

            leftLabels.addAll(getLeftColumnLabelsForPage(page));

            for (NxValuePoint[] qDatum : page.qData) {
                Value row = new Value();
                row.setValues(getValuesForRow(qDatum));
                rows.add(row);
            }
        }

        return PivotModePageData.builder()
                .left(leftLabels)
                .top(topLabels)
                .rows(rows)
                .build();
    }

    private static List<String> getTopLabelsForPage(NxPivotPage page) {
        List<String> labels = new ArrayList<String>();
        NxPivotDimensionCell[] qTop = page.qTop;
        for (NxPivotDimensionCell cell : qTop) {
            labels.add(cell.qText);
        }
        return labels;
    }

    private static List<String> getLeftColumnLabelsForPage(NxPivotPage qDataPage) {
        List<String> leftLabels = new ArrayList<String>();
        NxPivotDimensionCell[] qLeft = qDataPage.qLeft;
        for (NxPivotDimensionCell cell : qLeft) {
            leftLabels.add(cell.qText);
        }
        return leftLabels;
    }

    private static List<Value> getValuesForRow(NxValuePoint[] qDatum) {
        List<Value> rowValues = new ArrayList<Value>();
        for (NxValuePoint d : qDatum) {
            Value cellValue = new Value();
            cellValue.setSValue(d.qText);
            cellValue.setNValue(d.qNum);
            rowValues.add(cellValue);
        }
        return rowValues;
    }

    private static StraightModePageData getDataForStraightMode(QlikSenseClient qlikSenseClient, Integer objectHandle, int cols, int rowOnPage, int pageIndex) throws Exception {
        ArrayList<String> top = new ArrayList<String>();
        ArrayList<Value> values = new ArrayList<Value>();
        GetHyperCubeDataRequest method1 = qlikSenseClient.call(new GetHyperCubeDataRequest(objectHandle, 0, pageIndex * rowOnPage, cols, rowOnPage));
        QDataPage[] qDataPages1 = method1.getResponse().result.qDataPages;
        for (QDataPage qDataPage : qDataPages1) {
            QMatrixItem[][] qMatrix1 = qDataPage.qMatrix;
            for (QMatrixItem[] qMatrix : qMatrix1) {
                String label = qMatrix[0].qText;
                top.add(label);
                Value value = new Value();
                value.setSValue(qMatrix[1].qText);
                value.setNValue(qMatrix[1].qNum);
                values.add(value);
            }
        }
        return StraightModePageData.builder().rows(values).top(top).build();
    }

    private static void clearHypercubeInitialFetch(QlikSenseClient qlikSenseClient, Integer objectHandle) throws Exception {
        HashMap<String, Object> changes = new HashMap<String, Object>();
        changes.put("/qHyperCubeDef/qInitialDataFetch/0", "{\"qLeft\": 0,\"qTop\": 0, \"qWidth\": 0,\"qHeight\": 0}");
        qlikSenseClient.call(new ApplyPatchesRequest(objectHandle, changes));
        log.info("Initial fetch cleared " + objectHandle);
    }

    static class PivotModePageData {
        private List<String> top;
        private List<String> left;
        private List<Value> rows;

        PivotModePageData(List<String> top, List<String> left, List<Value> rows) {
            this.top = top;
            this.left = left;
            this.rows = rows;
        }

        public static PivotModePageDataBuilder builder() {
            return new PivotModePageDataBuilder();
        }

        public List<String> getTop() {
            return this.top;
        }

        public List<String> getLeft() {
            return this.left;
        }

        public List<Value> getRows() {
            return this.rows;
        }

        public void setTop(List<String> top) {
            this.top = top;
        }

        public void setLeft(List<String> left) {
            this.left = left;
        }

        public void setRows(List<Value> rows) {
            this.rows = rows;
        }

        public static class PivotModePageDataBuilder {
            private List<String> top;
            private List<String> left;
            private List<Value> rows;

            PivotModePageDataBuilder() {
            }

            public PivotModePageData.PivotModePageDataBuilder top(List<String> top) {
                this.top = top;
                return this;
            }

            public PivotModePageData.PivotModePageDataBuilder left(List<String> left) {
                this.left = left;
                return this;
            }

            public PivotModePageData.PivotModePageDataBuilder rows(List<Value> rows) {
                this.rows = rows;
                return this;
            }

            public PivotModePageData build() {
                return new PivotModePageData(top, left, rows);
            }
        }
    }

    private static class StraightModePageData {
        private List<String> top;
        private List<Value> rows;

        StraightModePageData(List<String> top, List<Value> rows) {
            this.top = top;
            this.rows = rows;
        }

        public static StraightModePageDataBuilder builder() {
            return new StraightModePageDataBuilder();
        }

        public List<String> getTop() {
            return this.top;
        }

        public List<Value> getRows() {
            return this.rows;
        }

        public void setTop(List<String> top) {
            this.top = top;
        }

        public void setRows(List<Value> rows) {
            this.rows = rows;
        }

        public static class StraightModePageDataBuilder {
            private List<String> top;
            private List<Value> rows;

            StraightModePageDataBuilder() {
            }

            public StraightModePageData.StraightModePageDataBuilder top(List<String> top) {
                this.top = top;
                return this;
            }

            public StraightModePageData.StraightModePageDataBuilder rows(List<Value> rows) {
                this.rows = rows;
                return this;
            }

            public StraightModePageData build() {
                return new StraightModePageData(top, rows);
            }
        }
    }
}
