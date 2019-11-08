package ru.sberbank.syncserver2.service.sql;

public interface QlikSenseServiceConfig {
    String getRootCertificateFileName();

    void setRootCertificateFileName(String rootCertificateFileName);

    String getClientCertificateFileName();

    void setClientCertificateFileName(String clientCertificateFileName);

    String getClientKeyPathFileName();

    void setClientKeyPathFileName(String clientKeyPathFileName);

    String getServiceName();

    void setServiceName(String serviceName);

    String getServerHost();

    void setServerHost(String serverHost);

    String getServerPort();

    void setServerPort(String serverPort);

    String getServerContext();

    void setServerContext(String serverContext);

    String getCertificateDir();

    void setCertificateDir(String certificateDir);

    String getClientKeyPassword();

    void setClientKeyPassword(String clientKeyPassword);

    String getUser();

    void setUser(String user);

    String getDomain();

    void setDomain(String domain);

    String getMaxTotal();

    void setMaxTotal(String maxTotal);

    String getMaxIdle();

    void setMaxIdle(String maxIdle);

    String getMinIdle();

    void setMinIdle(String minIdle);

    String getViewUserLogin();

    void setViewUserLogin(String login);

    String getViewUserPassword();

    void setViewUserPassword(String password);
}
