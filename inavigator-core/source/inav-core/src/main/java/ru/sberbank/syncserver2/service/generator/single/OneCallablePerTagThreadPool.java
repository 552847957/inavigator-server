package ru.sberbank.syncserver2.service.generator.single;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by sbt-kozhinsky-lb on 22.06.14.
 */
public class OneCallablePerTagThreadPool {
    private ExecutorService executorService;
    private Map<String,TaskContainer> tagsToTaskContainers;

    public static interface RUN_STATUS {
        public static final int NONE       = 0;
        public static final int IN_QUEUE   = 1;
        public static final int RUNNING    = 2;
        public static final int CANCELLING = 3;
    }

    public OneCallablePerTagThreadPool(int threads) {
        synchronized (this){
            this.executorService = Executors.newFixedThreadPool(threads);
            this.tagsToTaskContainers = new HashMap();
        }
    }

    public boolean execute(String tag, final Runnable runnable){
        //1. Creating callable
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                runnable.run();
                return null;
            }
        };

        //2. Finding task container and submit
        return submit(tag,callable);
    }

    public boolean submit(String tag, Callable callable){
        TaskContainer container = getOrCreateContainer(tag);
        return container.submit(callable);
    }

    public void cancel(String tag){
        TaskContainer container = getOrCreateContainer(tag);
        container.cancel();
    }

    public int getRunStatus(String tag){
        TaskContainer container = getOrCreateContainer(tag);
        return container.getRunStatus();
    }

    public Throwable getLastException(String tag){
        TaskContainer container = getOrCreateContainer(tag);
        return container.getLastException();
    }

    public Object getLastResult(String tag){
        TaskContainer container = getOrCreateContainer(tag);
        return container.getLastResult();
    }

    private TaskContainer getOrCreateContainer(String tag){
        TaskContainer container = null;
        synchronized (tagsToTaskContainers){
            container = (TaskContainer) tagsToTaskContainers.get(tag);
            if(container==null){
                container = new TaskContainer(tag);
                tagsToTaskContainers.put(tag,container);
            }
        }
        return container;
    }

    //public synchronized void startup(int threads) {
    //    if(executorService.isShutdown()){
    //        this.executorService = Executors.newFixedThreadPool(threads);
    //    }
    //}

    public synchronized void shutdown() {
        executorService.shutdown();
    }

    private class TaskContainer implements Callable{
        private String    tag;
        private int       status;
        private Callable  task;
        private Object    lastResult;
        private Throwable lastException;
        private Future    lastFuture;

        private TaskContainer(String tag) {
            this.tag = tag;
            this.status = RUN_STATUS.NONE;
        }

        public synchronized boolean submit(Callable nextTask){
            //1. We couldn't add tasks if previous task is not fully finished
            if(this.task!=null){
                return false;
            }

            //2. We couldn't add null as task
            if(nextTask==null){
                return false;
            }

            //2. Adding and starting if required
            this.status = RUN_STATUS.IN_QUEUE;
            this.task = nextTask;
            this.lastResult = null;
            this.lastException = null;
            this.lastFuture = executorService.submit(this);
            return true;
        }

        public synchronized void cancel(){
            switch (status){
                case RUN_STATUS.NONE: return;
                case RUN_STATUS.IN_QUEUE:
                    this.status = RUN_STATUS.NONE;
                    this.task = null;
                    this.lastResult = null;
                    this.lastException = null;
                    this.lastFuture = null;
                    break;
                case RUN_STATUS.RUNNING:
                    if(lastFuture!=null){
                        lastFuture.cancel(true);
                    }
                    this.status = RUN_STATUS.CANCELLING;
                case RUN_STATUS.CANCELLING: break;//cancel already happenned
            }
            if(lastFuture!=null){
                this.status = RUN_STATUS.CANCELLING;
                final Future localLastFuture = lastFuture;
                if(!lastFuture.cancel(true)){
//                    new Thread(){
//                        public void run(){
//                            while(!localLastFuture.isCancelled() || !localLastFuture.isDone()){
//                                localLastFuture.cancel(true);
//                                try {
//                                    Thread.sleep(60000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }.start();
                }
            }
        }

        @Override
        public Object call() throws Exception {
            //1. Get task for execution
            Callable target = null;
            synchronized (this){
                //1.1. If task in a wrong status then we do nothing
                if(status!=RUN_STATUS.IN_QUEUE){
                    return null;
                }

                //1.1. Getting task
                target = task;
                if(target!=null){
                    status = RUN_STATUS.RUNNING;
                } else {
                    return lastResult;
                }
            }

            //2. Running
            try {
                return lastResult = target.call();
            } catch (InterruptedException e) {
                //throw e;
            } catch (Throwable e) {
                e.printStackTrace();
                lastException = e;
            } finally {
                //Indicate that task is finished
                synchronized (this){
                    status = RUN_STATUS.NONE;
                    task = null;
                    lastFuture = null;
                }
            }
            return null;
        }

        public synchronized int getRunStatus() {
            return status;
        }

        public synchronized Object getLastResult() {
            return lastResult;
        }

        public synchronized Throwable getLastException() {
            return lastException;
        }
    }

    public static void main(String[] args) {
        OneCallablePerTagThreadPool pool = new OneCallablePerTagThreadPool(10);
        pool.submit("sample", new BasicWaiterTest("task1", 10));
        if(!pool.submit("sample", new BasicWaiterTest("task2",10))){
            System.out.println("Skip submitting task2 ");
        }
        if(!pool.submit("sample", new BasicWaiterTest("task3",10))){
            System.out.println("Skip submitting task4 ");
        }
        if(!pool.submit("sample", new BasicWaiterTest("task4",10))){
            System.out.println("Skip submitting task4 ");
        }

        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pool.submit("sample", new BasicWaiterTest("task7",10));

        try {
            Thread.sleep(8*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        pool.submit("sample1", new BasicWaiterTest("task8",10));
        pool.submit("sample2", new BasicWaiterTestWithError("task9",1));
        pool.submit("sample3", new BasicWaiterTest("task10",10));

        pool.cancel("sample");
    }

    private static class BasicWaiterTest implements Callable {
        private int seconds;
        private String name;

        private BasicWaiterTest(String name, int seconds) {
            this.seconds = seconds;
            this.name = name;
            System.out.println(new Date()+" Created "+this);
        }

        @Override
        public Object call() throws Exception {
            boolean cancelled = false;
            System.out.println(new Date()+" Starting "+this);
            try {
                Thread.sleep(1000*seconds);
            } catch (InterruptedException e) {
                System.out.println(new Date()+" Cancelled "+this);
                cancelled = true;
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                throw e;
            } finally {
                if(!cancelled){
                    System.out.println(new Date()+" Finished "+this);
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName()+"{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    private static class BasicWaiterTestWithError extends BasicWaiterTest {

        private BasicWaiterTestWithError(String name, int seconds) {
            super(name, seconds);
        }

        @Override
        public Object call() throws Exception {
            super.call();
            throw new OutOfMemoryError("not enough memory");
        }
    }
}
