package ru.sberbank.syncserver2.service.core;

/**
 * Created by sbt-kozhinsky-lb on 21.02.14.
 */
public abstract class BackgroundService extends AbstractService {
    @Override
    protected void waitUntilStopped() {
    }
}
