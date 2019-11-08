package ru.sberbank.syncserver2.service.security;

/**
 * Created by sbt-kozhinsky-lb on 04.03.14.
 */
public interface SecurityService {
    public boolean isAllowedToUseApp(String app, String userEmail, String deviceId);

    public boolean isAllowedToDownloadFile(String app, String fileName, String userEmail, String deviceId);
}
