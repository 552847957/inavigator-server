package ru.sberbank.syncserver2.service.core;

import ru.sberbank.syncserver2.service.monitor.NotificationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sbt-kozhinsky-lb on 10.02.15.
 */
public abstract class MonitoredPublicService extends SingleThreadBackgroundService {
    private AtomicLong lastRequestTime = new AtomicLong(System.currentTimeMillis());
    private long maxPeriodWithoutRequests = 60*10; //ten minutes without requests
    private boolean notifyOnFailure;

    public MonitoredPublicService() {
        super(60);
    }

    public AtomicLong getLastRequestTime() {
        return lastRequestTime;
    }

    public synchronized long getMaxPeriodWithoutRequests() {
        return maxPeriodWithoutRequests;
    }

    public synchronized void setMaxPeriodWithoutRequests(long maxPeriodWithoutRequests) {
        this.maxPeriodWithoutRequests = maxPeriodWithoutRequests;
    }

    public boolean isNotifyOnFailure() {
        return notifyOnFailure;
    }

    public void setNotifyOnFailure(boolean notifyOnFailure) {
        this.notifyOnFailure = notifyOnFailure;
    }

    public boolean isStateValid(){
        long minRequestTime;
        synchronized (this){
            minRequestTime = System.currentTimeMillis() - maxPeriodWithoutRequests *1000;
        }
        return lastRequestTime.get() > minRequestTime;
    }

    public void request(HttpServletRequest request, HttpServletResponse response) {
        lastRequestTime.set(System.currentTimeMillis());
        doRequest(request,response);
    }

    protected abstract void doRequest(HttpServletRequest request, HttpServletResponse response);

    @Override
    public void doInit() {
    }

    @Override
    public void doRun() {
        if(!isStateValid() && notifyOnFailure){
            String text = "No requests received by service "+getServiceBeanCode()+" during "+ maxPeriodWithoutRequests +" seconds ";
            NotificationService service = (NotificationService) ServiceManager.getInstance().findFirstServiceByClassCode(NotificationService.class);
            service.notify(text);
        }
    }
}
