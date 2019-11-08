package ru.sberbank.syncserver2.service.sql;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.log4j.lf5.util.StreamUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.ResponseError;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.Dataset;
import ru.sberbank.syncserver2.service.sql.query.DatasetRow;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataPowerMockObject {

    protected HttpServer dataPowerServer;

    @Autowired
    @Qualifier("sqlPublicServiceAlphaTestBean")
    protected SQLPublicService sqlPublicServiceAlpha;


    public void before() throws Exception {
        HttpRequestHandler requestHandler = new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
                BasicHttpEntityEnclosingRequest request = (BasicHttpEntityEnclosingRequest) httpRequest;
                BasicHttpResponse response = (BasicHttpResponse) httpResponse;
                BasicHttpEntity requestEntity = (BasicHttpEntity) request.getEntity();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                InputStream is = requestEntity.getContent();
                StreamUtils.copy(is, byteArrayOutputStream);
                String payload = byteArrayOutputStream.toString("UTF8");

                OnlineRequest onlineRequest = null;
                InputStream payLoadInputStrem = new ByteArrayInputStream(payload.getBytes("UTF8"));
                try {
                    onlineRequest = (OnlineRequest) XMLHelper.readXML(payLoadInputStrem, OnlineRequest.class, ResponseError.class);
                } catch (JAXBException e) {
                    e.printStackTrace();
                }

                Assert.assertNotNull("Error deserialization OnlineRequest!", onlineRequest);
                if ("DATAPOWER".equals(onlineRequest.getAlphaDbHost())) {
                    DataResponse dataResponse = new DataResponse();
                    Dataset ds = new Dataset();
                    DatasetRow dsr = new DatasetRow();
                    dsr.addValue("Все просто здорово!");
                    ds.addRow(dsr);
                    dataResponse.setDataset(ds);
                    dataResponse.setResult(DataResponse.Result.OK);
                    String responsString = XMLHelper.writeXMLToString(dataResponse, false, DataResponse.class);
                    ByteArrayInputStream bais = new ByteArrayInputStream(responsString.getBytes("UTF8"));
                    BasicHttpEntity responseEntry = new BasicHttpEntity();
                    responseEntry.setContent(bais);
                    response.setEntity(responseEntry);
                    response.setStatusCode(200);
                    response.setStatusLine(new BasicStatusLine(HttpVersion.HTTP_1_0, 200, "Все хорошо!"));
                } else if (onlineRequest.getAlphaDbHost() != null && onlineRequest.getAlphaDbHost().startsWith("QLIK_SENSE")) {
                    //Assert.assertEquals(payload, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><request><stored-procedure>QLIK_SENSE_SERVICE_ONE_REQUEST_ONE</stored-procedure><arguments><argument><index>1</index><type>STRING</type><value>{id: 1, documentId: &quot;DOCID&quot;, objectId: [&quot;ONE&quot;,&quot;TWO&quot;,...,&quot;AT ST&quot;]} </value></argument></arguments><provider>DISPATCHER</provider><alpha-web-host>finik2-new</alpha-web-host><alpha-db-host>QLIK_SENSE_SERVICE_ONE</alpha-db-host></request>");

                    DataResponse dataResponse = sqlPublicServiceAlpha.request(onlineRequest);

/*
                    DataResponse dataResponse = new DataResponse();
                    Dataset ds = new Dataset();
                    DatasetRow dsr = new DatasetRow();
                    dsr.addValue("Все просто здорово!");
                    ds.addRow(dsr);
                    dataResponse.setDataset(ds);
                    dataResponse.setResult(DataResponse.Result.OK);
*/


                    String responsString = XMLHelper.writeXMLToString(dataResponse, false, DataResponse.class);
                    ByteArrayInputStream bais = new ByteArrayInputStream(responsString.getBytes("UTF8"));
                    BasicHttpEntity responseEntry = new BasicHttpEntity();
                    responseEntry.setContent(bais);
                    response.setEntity(responseEntry);
                    response.setStatusCode(200);
                    response.setStatusLine(new BasicStatusLine(HttpVersion.HTTP_1_0, 200, "Все хорошо!"));

                } else if (onlineRequest.getAlphaDbHost() != null && onlineRequest.getAlphaDbHost().startsWith("finik2-new")) {
                    DataResponse dataResponse = new DataResponse();
                    Dataset ds = new Dataset();
                    DatasetRow dsr = new DatasetRow();
                    dsr.addValue("file #1!");
                    ds.addRow(dsr);
                    dsr = new DatasetRow();
                    dsr.addValue("file #2!");
                    ds.addRow(dsr);
                    dataResponse.setDataset(ds);
                    dataResponse.setResult(DataResponse.Result.OK);
                    String responsString = XMLHelper.writeXMLToString(dataResponse, false, DataResponse.class);
                    ByteArrayInputStream bais = new ByteArrayInputStream(responsString.getBytes("UTF8"));
                    BasicHttpEntity responseEntry = new BasicHttpEntity();
                    responseEntry.setContent(bais);
                    response.setEntity(responseEntry);
                    response.setStatusCode(200);
                    response.setStatusLine(new BasicStatusLine(HttpVersion.HTTP_1_0, 200, "All right!"));
                } else if (onlineRequest.getAlphaDbHost() == null && "SYNCSERVER.DP_NOT_DEL_FILE_MOV_NOTIFICATION".equals(onlineRequest.getStoredProcedure())) {
                    response.setStatusCode(200);
                    response.setStatusLine(new BasicStatusLine(HttpVersion.HTTP_1_0, 200, "All right!"));
                } else if ("finik2-new".equals(onlineRequest.getAlphaWebHost()) && "mis-generator".equals(onlineRequest.getAlphaDbHost()) && "SYNCSERVER.GENERATOR_USER_GROUP_DRAFT".equals(onlineRequest.getStoredProcedure())) {
                    DataResponse dataResponse = new DataResponse();
                    Dataset ds = new Dataset();
                    DatasetRow dsr = new DatasetRow();
                    dsr.addValue("app #1");
                    dsr.addValue("email@mail.ru");
                    ds.addRow(dsr);
                    dsr = new DatasetRow();
                    dsr.addValue("app #2");
                    dsr.addValue("email2@mail.ru");
                    ds.addRow(dsr);
                    dataResponse.setDataset(ds);
                    dataResponse.setResult(DataResponse.Result.OK);
                    String responsString = XMLHelper.writeXMLToString(dataResponse, false, DataResponse.class);
                    ByteArrayInputStream bais = new ByteArrayInputStream(responsString.getBytes("UTF8"));
                    BasicHttpEntity responseEntry = new BasicHttpEntity();
                    responseEntry.setContent(bais);
                    response.setEntity(responseEntry);
                    response.setStatusCode(200);
                    response.setStatusLine(new BasicStatusLine(HttpVersion.HTTP_1_0, 200, "All right!"));
                } else if ("finik1-new".equals(onlineRequest.getAlphaWebHost()) && "mis-generator".equals(onlineRequest.getAlphaDbHost()) && "SYNCSERVER.SINGLE_FILES_REQUEST_STATUS".equals(onlineRequest.getStoredProcedure())) {
                    DataResponse dataResponse = new DataResponse();
                    Dataset ds = new Dataset();
                    DatasetRow dsr = new DatasetRow();
                    dsr.addValue("");
                    dsr.addValue("app #1");
                    dsr.addValue("10");
                    dsr.addValue("1234567890");
                    dsr.addValue("1234567890");
                    dsr.addValue("1");
                    ds.addRow(dsr);
                    dsr = new DatasetRow();
                    dsr.addValue("");
                    dsr.addValue("app #2");
                    dsr.addValue("20");
                    dsr.addValue("1234567890");
                    dsr.addValue("1234567890");
                    dsr.addValue("1");
                    ds.addRow(dsr);
                    dataResponse.setDataset(ds);
                    dataResponse.setResult(DataResponse.Result.OK);
                    String responsString = XMLHelper.writeXMLToString(dataResponse, false, DataResponse.class);
                    ByteArrayInputStream bais = new ByteArrayInputStream(responsString.getBytes("UTF8"));
                    BasicHttpEntity responseEntry = new BasicHttpEntity();
                    responseEntry.setContent(bais);
                    response.setEntity(responseEntry);
                    response.setStatusCode(200);
                    response.setStatusLine(new BasicStatusLine(HttpVersion.HTTP_1_0, 200, "All right!"));
                } else {
                    Assert.fail("Unknown OnlineRequest!");
                }
            }

        };
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(15000)
                .setTcpNoDelay(true)
                .build();
        dataPowerServer = ServerBootstrap.bootstrap()
                .setListenerPort(48655)
                .setSocketConfig(socketConfig)
                .registerHandler("*", requestHandler)
                .create();
        dataPowerServer.start();
    }

    public void after() throws ComponentException, IOException {
        dataPowerServer.stop();
    }


}
