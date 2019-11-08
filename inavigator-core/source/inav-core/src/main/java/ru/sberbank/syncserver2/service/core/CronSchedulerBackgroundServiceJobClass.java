package ru.sberbank.syncserver2.service.core;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CronSchedulerBackgroundServiceJobClass implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        CronSchedulerBackgroundService service = (CronSchedulerBackgroundService) dataMap.get("CronSchedulerBackgroundService");
        service.doRun(jobExecutionContext);
    }

}