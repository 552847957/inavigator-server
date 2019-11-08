package ru.sberbank.syncserver2.service.security;

import java.security.cert.X509Certificate;

/**
 * Created by sbt-kozhinsky-lb on 10.03.15.
 */
public class NullLdapUserCheckerServiceGroup extends LdapUserCheckerServiceGroup {

    @Override
    public boolean checkCachedUserCertByEmail(String email, X509Certificate clientCert) {
        return true;
    }
}
