package ru.sberbank.syncserver2.service.common;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.log.LogEventType;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Created by sbt-kozhinsky-lb on 26.02.14.
 */
public class PingService extends AbstractService implements PublicService {
    @Override
    public void request(HttpServletRequest request, HttpServletResponse response) {
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            StringBuffer sb = new StringBuffer("PONG");
            out.print(sb.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void doStop() {
    	logServiceMessage(LogEventType.SERV_STOP, "stopping service");
        logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

    @Override
    protected void waitUntilStopped() {
    }

    @Override
    protected void doStart() {
        logServiceMessage(LogEventType.SERV_START, "starting service");
        logServiceMessage(LogEventType.SERV_START, "started service");
    }
}
