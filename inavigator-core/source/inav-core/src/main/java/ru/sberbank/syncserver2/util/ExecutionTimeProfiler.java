package ru.sberbank.syncserver2.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sbt-kozhinsky-lb on 16.06.14.
 */
public class ExecutionTimeProfiler {
    private static ThreadLocal localStarts = new ThreadLocal();
    private static Map<String,Measurement> summaries = new HashMap<String,Measurement>();

    public static void start(String name){
        long now = System.currentTimeMillis();
        Map localMeasurements = (Map) localStarts.get();
        if(localMeasurements==null){
            localMeasurements = new HashMap();
            localStarts.set(localMeasurements);
        }
        localMeasurements.put(name,new Long(now));
    }

    public static void finish(String name){
        //1. Calculating difference
        long now = System.currentTimeMillis();
        Map thisThreadStarts = (Map) localStarts.get();
        if(thisThreadStarts==null){
            try {
                throw new RuntimeException("Invalid ExecutionTimeProfiler call #1");
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        Long start = (Long)thisThreadStarts.get(name);
        if(start==null){
            try {
                throw new RuntimeException("Invalid ExecutionTimeProfiler call #2");
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            return;
        }
        long diff = now - start;

        //2. Amending count
        synchronized (summaries){
            Measurement measurement = summaries.get(name);
            if(measurement==null){
                measurement = new Measurement(name,diff,1);
                summaries.put(name,measurement);
            } else {
                measurement.add(diff);
            }
        }
    }

    public static String printAll(){
        synchronized (summaries){
            StringBuilder sb = new StringBuilder();
            for (java.util.Iterator iterator = summaries.values().iterator(); iterator.hasNext(); ) {
                Measurement measurement = (Measurement) iterator.next();
                sb.append(measurement.toString()).append("<br>");
            }
            return sb.toString();
        }
    }

    public static void clear(){
        synchronized (summaries){
            summaries.clear();
        }
    }

    private static class Measurement {
        private String name;
        private AtomicLong totalMillis;
        private AtomicLong totalAttempts;

        private Measurement(String name, long totalMillis, long totalAttempts) {
            this.name = name;
            this.totalMillis = new AtomicLong(totalMillis);
            this.totalAttempts = new AtomicLong(totalAttempts);
        }

        public void add(long millis) {
            totalMillis.addAndGet(millis);
            totalAttempts.addAndGet(1);
        }

        @Override
        public String toString() {
            double time = totalMillis.get() / totalAttempts.get();
            return "Avg time for " + name +" is "+time+" ms . Calculated based on "+totalAttempts.get()+" attempts";
        }
    }
}
