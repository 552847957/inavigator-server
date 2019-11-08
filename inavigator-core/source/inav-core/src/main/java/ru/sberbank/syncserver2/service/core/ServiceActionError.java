package ru.sberbank.syncserver2.service.core;

/**
 * Created by sbt-kozhinsky-lb on 21.02.14.
 */
public class ServiceActionError extends Exception {
    private boolean start;

    public ServiceActionError(String message, boolean start) {
        super(message);
        this.start = start;
    }
}
