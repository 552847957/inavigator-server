package ru.sberbank.syncserver2.util;

import org.apache.commons.io.IOUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sbt-Shakhov-IN on 06.06.2017.
 *
 * Класс для получения серверных сертифкатов. Решение ОДНОПОТОЧНОЕ, т.е. не является thread-safe,
 * т.к. хранит список сертификатов последнего хоста
 */
public class ServersCertificateCheckHelper {
    private SSLSocketFactory socketFactory;
    private SavingServerCertTrustManager trustManager = new SavingServerCertTrustManager();

    public ServersCertificateCheckHelper(String SSLProtocol) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance(SSLProtocol);
        context.init(null, new TrustManager[] {trustManager}, new SecureRandom());
        socketFactory = context.getSocketFactory();
    }

    public CertificateChainInformation check(String host, int port) throws IOException {
        SSLSocket socket = null;
        try {
            socket = (SSLSocket) socketFactory.createSocket(host, port);
            socket.startHandshake();
        } catch(IOException e) {
            if (trustManager.isEmpty())
                throw e;
        } finally {
            IOUtils.closeQuietly(socket);
        }
        X509Certificate[] certs = trustManager.getLastCertsAndUpdateState();

        return ((certs == null || certs.length == 0) ? null : new CertificateChainInformation(certs, host+":"+port));
    }

    private static class SavingServerCertTrustManager implements X509TrustManager {
        private X509Certificate[] certs = null;

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            this.certs = x509Certificates;
            throw new CertificateException("Server certificate information was added for checking");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public X509Certificate[] getLastCertsAndUpdateState() {
            X509Certificate[] local = certs;
            certs = null;
            return local;
        }

        public boolean isEmpty() {
            return certs == null || certs.length == 0;
        }
    }

    public static class CertificateChainInformation {
        private final X509Certificate[] certs;
        private final int closest;
        private final String host;
        private static final String DATE_FORMAT = "dd.MM.yyyy";

        public CertificateChainInformation(X509Certificate[] certs, String host) {
            this.host = host;
            this.certs = certs;
            int t = 0;
            for (int i = 1; i < certs.length; i++) {
                if (certs[i].getNotAfter().before(certs[i].getNotAfter()))
                    t = i;
            }
            this.closest = t;
        }

        public X509Certificate getClosestCert() {
            return certs[closest];
        }

        public Date getExpiredDate() {
            return certs[closest].getNotAfter();
        }

        public X509Certificate[] getChain() {
            return certs;
        }

        public String getHost() {
            return host;
        }

        @Override
        public String toString() {
            return "Сертифкат хоста "+host+" ("+getClosestCert().getSubjectDN()+") "+" истекает "+
                    new SimpleDateFormat(DATE_FORMAT).format(getClosestCert().getNotAfter());
        }
    }
}
