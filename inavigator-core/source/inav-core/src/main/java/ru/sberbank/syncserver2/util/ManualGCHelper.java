package ru.sberbank.syncserver2.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sbt-kozhinsky-lb on 10.06.14.
 */
public class ManualGCHelper {
    private AtomicLong actionCounter = new AtomicLong(0);
    private AtomicLong previousGCTimestamp = new AtomicLong(System.currentTimeMillis());

    public void action(){
        long counter = actionCounter.addAndGet(1);
        long prevTime = previousGCTimestamp.get();
        if(counter % 1000 == 0 || prevTime<System.currentTimeMillis()-10*60*1000){
            System.gc();
            previousGCTimestamp.set(System.currentTimeMillis());
        }
    }
}
