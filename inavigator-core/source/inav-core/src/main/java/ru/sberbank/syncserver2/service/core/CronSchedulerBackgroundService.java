package ru.sberbank.syncserver2.service.core;

import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger;
import ru.sberbank.syncserver2.util.FormatHelper;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public abstract class CronSchedulerBackgroundService extends BackgroundService {

    private Logger logger = Logger.getLogger(CronSchedulerBackgroundService.class);
    private static volatile Scheduler scheduler = null;
    private String cronExpression;
    private volatile boolean started = false;
    private boolean reentrant = false;
    private JobDetail jobDetail;
    private CronTrigger trigger;
    private AtomicBoolean reenter = new AtomicBoolean(false);
    protected ClusterManager clusterManager;
    private boolean clustered = true;

    public CronSchedulerBackgroundService() {
        super();
    }

    private static void createScheduler() throws SchedulerException {
        if (scheduler == null) {
            synchronized (Scheduler.class) {
                if (scheduler == null) {
                    SchedulerFactory sf = new StdSchedulerFactory();
                    scheduler = sf.getScheduler();
                }
            }
        }
    }

    public static Scheduler getSched() throws SchedulerException {
        createScheduler();
        return scheduler;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isReentrant() {
        return reentrant;
    }

    public void setReentrant(boolean reentrant) {
        this.reentrant = reentrant;
    }

    public void setReentrant(String reentrant) {
        this.reentrant = Boolean.parseBoolean(reentrant);
    }

    public String getJobIdentity() {
        return serviceBeanCode;
    }

    public boolean isClustered() {
        return clustered;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    public void setClustered(String clustered) {
        this.clustered = Boolean.parseBoolean(clustered);
    }

    public void addClustered(String clustered) {
        this.clustered = Boolean.parseBoolean(clustered);
    }

    @Override
    protected void doStart() {
        //log(LogEventType.SERV_START, "starting service");
        try {
            createScheduler();
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("CronSchedulerBackgroundService", this);
            jobDetail = newJob(CronSchedulerBackgroundServiceJobClass.class)
                    .withIdentity(getJobIdentity(), "CronSchedulerBackgroundService")
                    .setJobData(dataMap)
                    .build();

            TriggerBuilder<Trigger> triggerBuilder = newTrigger();

            trigger =
                    triggerBuilder.withIdentity("trigger " + cronExpression, "CronSchedulerBackgroundService")
                            .withSchedule(cronSchedule(cronExpression))
                            .forJob(getJobIdentity(), "CronSchedulerBackgroundService")
                            .build();

            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
            clusterManager = getClusterManager();
            started = true;
            //log(LogEventType.SERV_START, "started service");
        } catch (SchedulerException e) {
            log(LogEventType.ERROR, "Error starting service");
            logger.error(FormatHelper.stringConcatenator("CronSchedulerBackgroundService: Error start service '", e.getMessage(), "'", e));
        }
    }

    protected ClusterManager getClusterManager() {
        return (ClusterManager) ServiceManager.getInstance().findFirstServiceByClassCode(ClusterManager.class);
    }

    protected boolean isHostActive() {
        return (clusterManager == null || clusterManager.isActive());
    }

    public void doRun(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            if (started) {
                if (clustered) {
                    if (isHostActive()) {
                         doRunGeneral(jobExecutionContext);
                    } else {
                        logger.warn(FormatHelper.stringConcatenator("CronSchedulerBackgroundService: The cluster host is not active! Skip run job."));
                    }
                } else {
                    doRunGeneral(jobExecutionContext);
                }
            }
        } catch (Throwable exception) {
            try {
                doOnThrowAnyException(exception);
                notifyCsps(exception.getMessage());
            } catch (Throwable insideException) {
                logger.error(FormatHelper.stringConcatenator("Error in error handler in CronSchedulerBackgroundService.doRun"), insideException);
            }
            throw new JobExecutionException(exception, false);
        }
    }

    private void doRunGeneral(JobExecutionContext jobExecutionContext) throws Exception {
        boolean r = reenter.get();
        if (!r) {
            if (reenter.compareAndSet(false, true)) {
                try {
                    doRunInternal();
                } finally {
                    reenter.compareAndSet(true, false);
                }
            } else {
                doRunReentrant(jobExecutionContext);
            }
        } else {
            doRunReentrant(jobExecutionContext);
        }
    }

    private void doRunReentrant(JobExecutionContext jobExecutionContext) throws Exception {
        if (reentrant && jobExecutionContext.getJobDetail().equals(jobDetail) && jobExecutionContext.getTrigger().equals(trigger)) {
            doRunInternal();
        } else {
            log(LogEventType.DEBUG, "CronSchedulerBackgroundService.JobClass.execute: reentrant failed!");
            logger.error(FormatHelper.stringConcatenator("CronSchedulerBackgroundService.JobClass.execute: reentrant failed! reentrant = ", reentrant, ", jobDetail = ", jobDetail, ", trigger = ", trigger));
        }
    }

    private void doRunInternal() throws Exception {
        try {
            doBeforeRun();
            doRun();
        } finally {
            doAfterRun();
        }
    }

    public abstract void doBeforeRun() throws Exception;

    public abstract void doAfterRun() throws Exception;

    public abstract void doRun() throws Exception;

    public abstract void doOnThrowAnyException(Throwable exception);

    @Override
    protected void waitUntilStopped() {
        if (started) {
            log(LogEventType.SERV_STOP, "stoping service");
            try {
                scheduler.shutdown(true);
                scheduler = null;
                jobDetail = null;
                trigger = null;
                started = false;
                System.gc();
            } catch (SchedulerException e) {
                log(LogEventType.ERROR, "Error stoping service");
                logger.error(FormatHelper.stringConcatenator("CronSchedulerBackgroundService: Error stoping service '", e.getMessage(), "'"), e);
            }
        }
    }

    @Override
    protected void doStop() {
        waitUntilStopped();
        log(LogEventType.SERV_STOP, "stoped service");
    }

    protected void log(LogEventType eventType, String msg) {
        String[] tags = new String[]{serviceBeanCode};
        logServiceMessageWithTags(eventType, tags, msg);
        logger.info(msg);
    }

    protected void logErrorH(LogEventType eventType, String msg, Throwable th) {
        logError(eventType, msg, th);
        logger.info(msg, th);
    }

    protected void notifyCsps(String strMsg) {
        try {
            DatabaseNotificationLogger databaseNotificationLogger = null;
            ServiceManager serviceManager = getServiceContainer().getServiceManager();
            databaseNotificationLogger = (DatabaseNotificationLogger) serviceManager.findFirstServiceByClassCode(DatabaseNotificationLogger.class);
            logger.debug(FormatHelper.stringConcatenator("CronSchedulerBackgroundService 1: notifyCsps '", strMsg, "'"));
            databaseNotificationLogger.addError(strMsg);
            logger.debug(FormatHelper.stringConcatenator("CronSchedulerBackgroundService 2: notifyCsps '", strMsg, "'"));
        } catch (Exception exc) {
            logError(LogEventType.ERROR, "Ошибка отправки нотификационного сообщения!", exc);
        }
    }

}
