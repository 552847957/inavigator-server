package ru.sberbank.syncserver2.service.sql;

/**
 * Created by sbt-kozhinsky-lb on 31.03.14.
 */
public interface Dispatchable {
    public boolean isRestarted();
    public String getServiceName();
}
