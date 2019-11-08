package ru.sberbank.syncserver2.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sbt-Shakhov-IN on 13.06.2017.
 */
public class FileCertificateCheckHelper {
    public static final Logger LOGGER = Logger.getLogger(FileCertificateCheckHelper.class);

    public static X509Certificate getCertWithClosestExpirationDate(byte[] pkcs12, String password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks=KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(pkcs12), password.toCharArray());
        X509Certificate closestCert = null;
        for (Enumeration<String> als = ks.aliases(); als.hasMoreElements();) {
            String al = als.nextElement();
            Certificate[] certs = ks.getCertificateChain(al);
            if (certs != null) {
                for (Certificate cert: certs) {
                    X509Certificate certX509 = (X509Certificate) cert;
                    if (closestCert == null || certX509.getNotAfter().before(closestCert.getNotAfter())) {
                        //X500Name name = X500Name.asX500Name(certX509.getSubjectX500Principal());
                        closestCert = certX509;
                    }
                }
            }
        }
        return closestCert;
    }

    public static final String CERTIFICATE_LOCAL_EXTENSION = ".p12";

    private static CertificateInfo getCertificateInformation(File root) {

        File certificates[] = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(CERTIFICATE_LOCAL_EXTENSION);
            }
        });
        File passwords[] = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals("settings.properties");
            }
        });
        if (certificates.length < 1 || passwords.length < 1)
            return null;

        File passwordFile = passwords[0];
        File certificateFile =  certificates[0];
        BufferedReader br = null;
        FileInputStream fis = null;
        try {
            br = new BufferedReader(new FileReader(passwordFile));
            Properties properties = new Properties();
            properties.load(br);

            String password = properties.getProperty("password");

            fis = new FileInputStream(certificateFile);
            byte[] binaryData = new byte[(int)certificateFile.length()];
            fis.read(binaryData);
            X509Certificate cert = getCertWithClosestExpirationDate(binaryData, password);
            return cert == null ? null : new CertificateInfo(cert, root.getName()+"/"+certificateFile.getName());
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            FileHelper.close(fis);
            FileHelper.close(br);
        }
        return null;

    }

    public static void addCertificatesFromFolder(List<CertificateInfo> certs, File root) {
        if (!root.isDirectory())
            return;
        CertificateInfo cert = getCertificateInformation(root);
        if (cert != null)
            certs.add(cert);
        for (File file : root.listFiles()) {
            addCertificatesFromFolder(certs, file);
        }
    }

    public static class CertificateInfo {
        private final X509Certificate cert;
        private final String name;
        private static final String DATE_FORMAT = "dd.MM.yyyy";

        public CertificateInfo(X509Certificate cert, String name) {
            this.cert = cert;
            this.name = name;
        }

        public X509Certificate getCert() {
            return cert;
        }

        public Date getExpirationDate() {
            return cert.getNotAfter();
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Сертифкат "+name+" ("+cert.getSubjectDN()+") "+" истекает "+
                    new SimpleDateFormat(DATE_FORMAT).format(cert.getNotAfter());
        }
    }

    public static void main(String[] args) {

        File f = new File("C:\\Users\\sbt-Shakhov-IN\\ALL_NEW_FOLDER\\Projects\\inavigator\\docs\\push_certificates\\DEV");

        List<CertificateInfo> certs = new ArrayList<CertificateInfo>();
        addCertificatesFromFolder(certs, f);

        System.out.println(certs);

    }
}
