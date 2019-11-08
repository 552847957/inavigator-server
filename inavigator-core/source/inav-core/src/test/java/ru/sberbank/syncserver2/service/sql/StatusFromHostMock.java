package ru.sberbank.syncserver2.service.sql;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import ru.sberbank.syncserver2.service.core.ComponentException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class StatusFromHostMock extends DataPowerMockObject {

    protected HttpServer httpServer;

    public void before() throws Exception {
        super.before();
        HttpRequestHandler requestHandler = new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {

                BasicHttpResponse response = (BasicHttpResponse) httpResponse;
                response.setStatusCode(400);
                response.setStatusLine(new BasicStatusLine(HttpVersion.HTTP_1_0, 400, "Bad Request"));

                BasicHttpRequest request = (BasicHttpRequest) httpRequest;
                if ("/monitor-sigma/gui/status.maintenance".equals(request.getRequestLine().getUri())) {

                    String responsString = "[true,\"some text\",\"mail1@sberbank.ru;mail2@sbberbank.ru;\",1234567890]";
                    ByteArrayInputStream bais = new ByteArrayInputStream(responsString.getBytes("UTF8"));
                    BasicHttpEntity responseEntry = new BasicHttpEntity();
                    responseEntry.setContent(bais);
                    response.setEntity(responseEntry);
                    response.setStatusCode(200);
                    response.setStatusLine(new BasicStatusLine(HttpVersion.HTTP_1_0, 200, "Все хорошо!"));
                }
            }

        };
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(15000)
                .setTcpNoDelay(true)
                .build();
        httpServer = ServerBootstrap.bootstrap()
                .setListenerPort(48656)
                .setSocketConfig(socketConfig)
                .registerHandler("*", requestHandler)
                .create();
        httpServer.start();
    }

    public void after() throws ComponentException, IOException {
        super.after();
        httpServer.stop();
    }
}
