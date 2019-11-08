package ru.sberbank.syncserver2.service.sql;

/**
 * Instances of this interface are dispatchers and they can add and remove sub services.
 */
public interface ManagerDispatchableSubServices {
    void addSubService(Dispatchable service);

    void removeSubService(String serviceName);
}
