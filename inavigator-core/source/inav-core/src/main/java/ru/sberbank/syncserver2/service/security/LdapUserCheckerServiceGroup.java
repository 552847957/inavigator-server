package ru.sberbank.syncserver2.service.security;

import org.bouncycastle.asn1.x509.Certificate;
import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 14.07.14.
 */
public class LdapUserCheckerServiceGroup extends SingleThreadBackgroundService {
    private String provider;
    private String domain;
    private String username;
    private String password;
    private List<LdapUserCheckerService> subservices = new ArrayList<LdapUserCheckerService>();

    public LdapUserCheckerServiceGroup() {
        super(60);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected void doStop() {
        for (int i = 0; i < subservices.size(); i++) {
            LdapUserCheckerService ldapUserCheckerService =  subservices.get(i);
            ldapUserCheckerService.doStop();
        }
        subservices.clear();
    }

    @Override
    protected void waitUntilStopped() {
        for (int i = 0; i < subservices.size(); i++) {
            LdapUserCheckerService ldapUserCheckerService =  subservices.get(i);
            ldapUserCheckerService.waitUntilStopped();
        }
        subservices.clear();
    }

    @Override
    public void doInit() {
        String[] providers = split(provider);
        for (int i = 0; i < providers.length; i++) {
            String p = providers[i];
            LdapUserCheckerService service = new LdapUserCheckerService();
            service.setProvider(p);
            service.setDomain(domain);
            service.setUsername(username);
            service.setPassword(password);
            service.setServiceContainer(super.getServiceContainer());
            service.doInit();
            subservices.add(service);
        }
    }

    @Override
    public void doRun() {
        for (int i = 0; i < subservices.size(); i++) {
            LdapUserCheckerService ldapUserCheckerService =  subservices.get(i);
            ldapUserCheckerService.doRun();
        }
    }

    public boolean checkCachedUserCertByEmail(String email, X509Certificate clientCert){
        //System.out.println("SERVICE COUNT = "+subservices.size());
        for (int i = 0; i < subservices.size(); i++) {
            LdapUserCheckerService ldapUserCheckerService =  subservices.get(i);
            boolean result = ldapUserCheckerService.checkCachedUserCertByEmail(email,clientCert);
            if(result){
                return true;
            }
        }
        return false;
    }

    private static String[] split(String toSplit){
        toSplit = toSplit==null ? null:toSplit.trim();
        if(toSplit==null || toSplit.length()==0){
            return new String[0];
        }
        String[] result = toSplit.split(";");
        return result==null ? new String[0]:result;
    }

    public static void main(String[] args) throws Exception {
        //1. Creating loaders
        LdapUserCheckerServiceGroup group = new LdapUserCheckerServiceGroup();
        group.setProvider("ldap://lake1.sigma.sbrf.ru:389");
        group.setDomain("SIGMA");
        group.setUsername("IncidentManagement");
        group.setPassword("c,th,fyr2013");
        group.doInit();

        //2. For every loader we request certificate for Novoselov and compare them
        String EMAIL = "RONovoselov.SBT@sberbank.ru";
        group.checkCachedUserCertByEmail(EMAIL,null);
    }

}
