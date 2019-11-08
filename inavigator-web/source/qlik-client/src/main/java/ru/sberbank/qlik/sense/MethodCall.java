package ru.sberbank.qlik.sense;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.sberbank.qlik.sense.methods.BaseRequest;

import javax.websocket.Session;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MethodCall<R, T extends BaseRequest<R>> implements Callable<T> {
    private static final Logger log = LogManager.getLogger(MethodCall.class);
    public static final int METHOD_CALL_TIMEOUT = 10;
    public static final TimeUnit METHOD_CALL_TIME_UNIT = TimeUnit.SECONDS;
    private long startTime;
    private long endTime;
    private CountDownLatch latch;
    private final T request;
    private final Session session;

    public MethodCall(T request, Session session) {
        this.request = request;
        this.session = session;
    }

    @Override
    public T call() throws Exception {
        String json = QlikSenseClient.getObjectMapper().writeValueAsString(request);
        log.debug(request.getId() + " --> " + json);
        startTime = System.currentTimeMillis();
        session.getBasicRemote().sendText(json);
        if (request.isAsync()) {
            latch = new CountDownLatch(1);
            latch.await(METHOD_CALL_TIMEOUT, METHOD_CALL_TIME_UNIT);
            if (latch.getCount() != 0) {
                log.error("Timeout" + METHOD_CALL_TIMEOUT + " " + METHOD_CALL_TIME_UNIT + " out for request " + request.getMethod() + request.getId());
                throw new Exception("Method call timeout");
            }
        }
        return request;
    }

    public T getRequest() {
        return request;
    }

    public void setResponse(R result) {
        request.setResponse(result);
        if (latch != null) {
            latch.countDown();
        }
        endTime = System.currentTimeMillis();
    }

    public long getDuration() {
        return endTime - startTime;
    }
}
