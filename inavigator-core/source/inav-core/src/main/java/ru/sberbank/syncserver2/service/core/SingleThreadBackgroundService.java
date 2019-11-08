package ru.sberbank.syncserver2.service.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


import ru.sberbank.syncserver2.service.log.LogEventType;

/**
 * Created by sbt-kozhinsky-lb on 21.02.14.
 */
public abstract class SingleThreadBackgroundService extends BackgroundService {
	private static final ThreadGroup threadGroup = new ThreadGroup("SingleThreadBackgroundServices");
    private ExecutorService executor = null;
    private int waitSeconds = 60;

    private AtomicBoolean isInternalTaskRunning  = new AtomicBoolean(false);
    private AtomicBoolean shouldInternalTaskStop = new AtomicBoolean(false);
    private AtomicLong    lastDoRunStart         = new AtomicLong(0);

    protected SingleThreadBackgroundService(int waitSeconds) {
        this.waitSeconds = waitSeconds;
    }

    @Override
    protected void doStart() {
        shouldInternalTaskStop.set(false);
    	ThreadFactory threadFactory = new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread th = new Thread(getThreadgroup(), r, getServiceBeanCode());
				th.setDaemon(true);
				th.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        logger.error("Exception in thread " + t, e);
                        logError(LogEventType.ERROR, "uncaught Exception", e);
                    }
                });

				return th;
			}
		};
		logServiceMessage(LogEventType.SERV_START, "starting service");
        executor = Executors.newSingleThreadExecutor(threadFactory);
        executor.execute(new Runnable() {
            @Override
            public void run() {
            	try {
            		logServiceMessage(LogEventType.SERV_START, "start creating background thread");
	                //1. Initializing
	                doInit();

	                //2. Executing
	                Runnable task = createDoRunTask();
	                submitTask(task);
	                logServiceMessage(LogEventType.SERV_START, "created background thread");
            	} catch(Throwable th) {
            		String msg = "cannot start background thread for "+serviceBeanCode;
                    logError(LogEventType.ERROR, msg, th);
            		logger.error(msg, th);
            	}
            }
        });

        logServiceMessage(LogEventType.SERV_START, "started service");
    }

    protected Runnable createDoRunTask() {
		return new DoRun();
	}

    protected void submitTask(Runnable task) {
		if (task != null) {
			executor.submit(task);
			logServiceMessage(LogEventType.SERV_START, "task is added to executor");
		}
	}

    @Override
    protected void doStop() {
        //1. Start stopping service
    	logServiceMessage(LogEventType.SERV_STOP, "stopping service");
        shouldInternalTaskStop.set(true); //this call will make internal task to stop when it call getDoStopCalled

        //2. Stopping threads but if they ignore InterruptedException, it does not help
        if(executor!=null){
            executor.shutdownNow();
        }
    }

    @Override
    protected void waitUntilStopped() {
        while(isInternalTaskRunning.get()){
            logServiceMessage(LogEventType.SERV_STOP, "waiting until internal task is stopped");
            try {
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

    /**
     * This method should be used for long-running tasks like file transport services
     * @return
     */
    protected boolean shouldInternalTaskStop(){
        return shouldInternalTaskStop.get();
    }


    public int getWaitSeconds() {
		return waitSeconds;
	}

	public void setWaitSeconds(int waitSeconds) {
		this.waitSeconds = waitSeconds;
	}

	public abstract void doInit();

    public abstract void doRun();

    public static ThreadGroup getThreadgroup() {
		return threadGroup;
	}

    protected ExecutorService getExecutor() {
    	return executor;
    }

    public boolean isInternalTaskRunning() {
        return isInternalTaskRunning.get();
    }

    public long getLastDoRunStart() {
        return lastDoRunStart.get();
    }

    private class DoRun implements Runnable {
        @Override
        public void run() {
            //1. Waiting a bit to get other threads chance to finish
            Thread.yield();

            //2. Executing one time
            try {
                isInternalTaskRunning.set(true);
                lastDoRunStart.set(System.currentTimeMillis());
                doRun();
            } catch (Throwable e) {
                logError(LogEventType.ERROR, "error in " + getClass().getSimpleName(), e);
                tagLogger.log("Uncaught Exception in this service: "+e);
                logger.error("unexpected error in " + getClass().getSimpleName(), e);
            } finally {
                isInternalTaskRunning.set(false);
                setLastActionComment("");
            }

            //3. Waiting configured interval
            try {
                Thread.sleep(waitSeconds*1000);
            } catch (InterruptedException e) {
                logger.debug("Service "+getServiceBeanCode()+" was interrupted while sleeping");
                return;
            }

            //4. Scheduling next execution
            if(!executor.isShutdown()){
                try {
                    executor.execute(new DoRun());
                }catch (Throwable e) {
                    logger.error(e, e);
                }
            }
        }
    }

}
