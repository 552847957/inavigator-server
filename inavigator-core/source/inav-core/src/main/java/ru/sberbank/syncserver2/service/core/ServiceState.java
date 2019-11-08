package ru.sberbank.syncserver2.service.core;

/**
 * Created by sbt-kozhinsky-lb on 21.02.14.
 */
public enum ServiceState {
    STOPPED ,
    CREATED ,
    STARTING,
    STARTED ,
    STOPPING,
    UNKNOWN;

    @Override
    public String toString() {
        switch (this){
            case STOPPED:  return "Stopped";
            case CREATED:  return "Created";
            case STARTING: return "Starting";
            case STARTED:  return "Started";
            case STOPPING: return "Stopped";
            case UNKNOWN:  return "Unknown";
        }
        return super.toString();
    }
}
