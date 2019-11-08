package ru.sberbank.syncserver2.service.log;

/**
 * Created by sbt-kozhinsky-lb on 18.06.14.
 */
public class SynchronousDbLogService extends DbLogService {

    public SynchronousDbLogService() {
        super(5);
    }

    /**
     * Make logging synchronous
     * @param logMsgs
     */
    @Override
    public void log(LogMsg... logMsgs) {
        super.log(logMsgs);
        super.doRun();
    }

    /**
     * Stop executing <code>run</code> in SingleThreadBackgroundThread
     */
    @Override
    protected Runnable createDoRunTask() {
        return null;
    }
}
