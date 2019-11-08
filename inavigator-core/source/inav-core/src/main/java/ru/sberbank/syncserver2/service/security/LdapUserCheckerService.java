package ru.sberbank.syncserver2.service.security;

import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.util.CollectionUtils;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.log.TagLogger;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.FileInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LdapUserCheckerService extends SingleThreadBackgroundService  {
    private static final String DISTINGUISHED_NAME = "distinguishedName";
    private static final String CN = "cn";
    private static final String MEMBER = "member";
    private static final String MEMBER_OF = "memberOf";
    private static final String CERTIFICATE = "usercertificate";
    private static final String SEARCH_BY_EMAIL = "(mail={0})";
    private String provider;
    private String domain;
    private String username;
    private String password;
    private DirContext ctx;
    private Hashtable<String, String> env = new Hashtable<String, String>();
    private static int sessionTimeoutSeconds = 600;
    private ConcurrentHashMap<String,CertificateList> emailCertsMap = new ConcurrentHashMap<String, CertificateList>();


    public LdapUserCheckerService() {
        super(sessionTimeoutSeconds);
        provider = "ldap://lake3.sigma.sbrf.ru:389";
        domain = "SIGMA";
        username = "IncidentManagement";
        password = "c,th,fyr2013";
        init();
    }

    public LdapUserCheckerService(String provider, String domain, String username, String password) {
        super(sessionTimeoutSeconds);
        this.provider = provider;
        this.domain = domain;
        this.username = username;
        this.password = password;
    }

    private void init() {
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, provider);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, domain + "\\" + username);
        env.put(Context.SECURITY_CREDENTIALS, password);
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

    /**
     * Prepares and returns CN that can be used for AD query
     * e.g. Converts "CN=**Dev - Test Group" to "**Dev - Test Group"
     * Converts CN=**Dev - Test Group,OU=Distribution Lists,DC=domain,DC=com to "**Dev - Test Group"
     */
    public String getCN(String cnName) {
        if (cnName == null) return null;
        if (cnName.toUpperCase().startsWith("CN=")) {
            cnName = cnName.substring(3);
        }
        int position = cnName.indexOf(',');
        if (position == -1) {
            return cnName;
        } else {
            return cnName.substring(0, position);
        }
    }

    public boolean checkCachedUserCertByEmail(String email, Certificate certificate) {
        try {
            EncodedCertificate encodedCertificate = new EncodedCertificate(certificate);
//            System.out.println("CERTIFICATE IS "+ MD5Helper.toHexString(encodedCertificate.encoded));
            CertificateList certificates = getCachedUserCerts(email);
            boolean result = certificates != null && certificates.contains(encodedCertificate);
            if(result){
                logError("Certificate has been accepted for "+email,null);
            } else {
                logError("Certificate has NOT been accepted for "+email,null);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logError("Certificate has NOT been accepted for "+email,e);
            return false;
        }
    }

    public boolean checkUserCertByEmail(String email, Certificate certificate) {
        try {
            EncodedCertificate encodedCertificate = new EncodedCertificate(certificate);
            //System.out.println("CERTIFICATE IS "+ MD5Helper.toHexString(encodedCertificate.encoded));
            CertificateList certificates = getUserCerts(email);
            boolean result = certificates != null && certificates.contains(encodedCertificate);
            if(result){
                logError("Certificate has been accepted for "+email,null);
            } else {
                logError("Certificate has NOT been accepted for "+email,null);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logError("Certificate has NOT been accepted for "+email,e);
            return false;
        }
    }

    public CertificateList getCachedUserCerts(String email) {
        //1. Check if email is defined
        if (email == null){
            return null;
        }

        //2. Return cached emails if available
        CertificateList certificateList = emailCertsMap.get(email);
        if (certificateList != null) {
            return certificateList;
        }

        //3. Get certificates and update cache
        try {
        	// если проверить сертификат не удалось (запрос в LDAP не удался) НЕЛЬЗЯ кэшировать пустой результат 
        	certificateList = getUserCerts(email);
        	emailCertsMap.put(email, certificateList);
        } catch (NamingException e) {
        	return new CertificateList(email, Collections.EMPTY_SET);
        }
        
        
        return certificateList;
    }

    @Override
    public void doInit() {
        init();
    }

    /**
     * Cleaning obsolete certificate cache
     */
    @Override
    public void doRun() {
        for (java.util.Iterator iterator = emailCertsMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry next = (Map.Entry) iterator.next();
            CertificateList certificateList = (CertificateList) next.getValue();
            if(certificateList.isObsolete()){
                iterator.remove();
            }
        }
    }

    @Override
    protected void doStop() {
        super.doStop();
    }

    @Override
    protected void waitUntilStopped() {
        super.waitUntilStopped();
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public CertificateList getUserCerts(String email) throws NamingException {
        try {
            //1. Make a request to LDAP
            logError("Checking certificates for "+email+" at "+provider,null);
            String defaultSearchBase = "DC=sigma,DC=sbrf,DC=ru";
            ctx = new InitialDirContext(env);
            // userName is SAMAccountName
            SearchResult sr = executeSearchSingleResult(ctx, SearchControls.SUBTREE_SCOPE, defaultSearchBase,
                    MessageFormat.format( SEARCH_BY_EMAIL, email),
                    new String[] {DISTINGUISHED_NAME, CERTIFICATE, CN, MEMBER_OF}
            );


            //2. Finish if no resules return
            if (sr == null) {
                logError("Search results are null for "+email+" at "+provider,null);
                return new CertificateList(email,Collections.EMPTY_SET);
            }

            //3. Extracting list of certificates and find matching
            NamingEnumeration<?> all = sr.getAttributes().get(CERTIFICATE).getAll();
            Set<EncodedCertificate> certSet = new HashSet<EncodedCertificate>();
            String STATIC_ERROR = "";
            while (all != null && all.hasMore()){
                try {
                    byte[] userCertificate = (byte[])all.next();
                    STATIC_ERROR = "Failed to decode field "+CERTIFICATE+" from Base64";
                    
                	// по умолчанию считаем что это base64 данные - раскодируем
                	// если раскодировка не удалась, то считаем что это бинарные данные
                    try {
                    	userCertificate = Base64.decode(userCertificate);
                    	logError("Found base64 certificate for " + email,null);
                    } catch (Exception ex) {
                    	logError("Found binary certificate for " + email,null);
                    }
                    //System.out.println("LDAP IS "+ MD5Helper.toHexString(userCertificate));
                    STATIC_ERROR = "Failed to transform field "+CERTIFICATE+" to org.bouncycastle.asn1.x509.Certificate ";
                    org.bouncycastle.asn1.x509.Certificate certificate = new X509CertificateHolder(userCertificate).toASN1Structure();
                    STATIC_ERROR = "Failed to create X509CertificateObject from field "+CERTIFICATE;
                    X509CertificateObject parsedCertificate = new X509CertificateObject(certificate);
                    STATIC_ERROR = "Failed to create encoded certificate from field "+CERTIFICATE;
                    EncodedCertificate encodedCertificate = new EncodedCertificate(parsedCertificate);
                    //System.out.println("ENCODED LDAP IS "+ MD5Helper.toHexString(userCertificate));
                    certSet.add(encodedCertificate);
                } catch (Exception e) {
                    logError(STATIC_ERROR + " for " + email, e);
                }
            }
            if (certSet.size()>0) {
                logError("Found "+certSet.size()+" certificates for" + email, null);
                return new CertificateList(email,certSet);
            }

            //4. Log error if no certificate found
            logError("NOT VALID user email:" + email, null);
            return new CertificateList(email,Collections.EMPTY_SET);
        } catch (NamingException e) {
            logError("Can't check user " + email, e);
            throw e;
           // return new CertificateList(email,Collections.EMPTY_SET);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    logError("Can't check user " + email, e);
                }
            }
        }
    }


    @SuppressWarnings("ThrowFromFinallyBlock")
    public boolean checkUser(String email) {
        String defaultSearchBase = "DC=sigma,DC=sbrf,DC=ru";

        try {
            ctx = new InitialDirContext(env);

            // userName is SAMAccountName
            SearchResult sr = executeSearchSingleResult(ctx, SearchControls.SUBTREE_SCOPE, defaultSearchBase,
                    MessageFormat.format( SEARCH_BY_EMAIL, email),
                    new String[] {DISTINGUISHED_NAME, CERTIFICATE, CN, MEMBER_OF}
            );

/*

            if (sr == null) return false;
            NamingEnumeration<?> all = sr.getAttributes().get(CERTIFICATE).getAll();
            boolean found = false;
            while (all != null && all.hasMore()){
                try {
                    Object next = all.next();
                    String encoded = new String((byte[]) next);
                    if (encoded.length() == 0) continue;
                    byte[] decode = new BASE64Decoder().decodeBuffer(encoded);
                    found = true;
                } catch (Exception e) {
                    // pass
                }
            }
            if (!found) {
                return new HashSet<String>();
            }
*/

            if (sr != null && sr.getAttributes().get(DISTINGUISHED_NAME) != null ) {
                return true;
            }

            logError("NOT VALID user email:" + email, null);
            return false;
        } catch (NamingException e) {
            logError("Can't check user " + email, e);
            return false;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    logError("Can't check user " + email, e);
                }
            }
        }
    }

    private void logError(String txt, Exception e){
        if(e==null){
            tagLogger.log(txt);
            logger.info(txt);
        } else {
            e.printStackTrace();
            tagLogger.log(txt + " : " + e.getMessage());
            logger.info(txt, e);
        }
    }

    /*
    private void processGroup(DirContext ctx, Attribute memberOf, String defaultSearchBase, Set<String> groups) throws NamingException {
        SearchResult sr;
        for ( Enumeration e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
            String unprocessedGroupDN = e1.nextElement().toString();
            String unprocessedGroupCN = getCN(unprocessedGroupDN);

            groups.add(unprocessedGroupCN);

            sr = executeSearchSingleResult(ctx, SearchControls.SUBTREE_SCOPE, defaultSearchBase,
                    "CN=" + unprocessedGroupCN, new String[]{DISTINGUISHED_NAME, CN, CERTIFICATE, MEMBER_OF}
            );

            Attribute member = sr.getAttributes().get(MEMBER);
            if (member != null) {
                processGroup(ctx, member, defaultSearchBase, groups);
            }

        }
    }*/

    private NamingEnumeration executeSearch(DirContext ctx, int searchScope,  String searchBase, String searchFilter, String[] attributes) throws NamingException {
        // Create the search controls
        SearchControls searchCtls = new SearchControls();

        // Specify the attributes to return
        if (attributes != null) {
            searchCtls.setReturningAttributes(attributes);
        }

        // Specify the search scope
        searchCtls.setSearchScope(searchScope);

        // Search for objects using the filter
        return ctx.search(searchBase, searchFilter,searchCtls);
    }

    private SearchResult executeSearchSingleResult(DirContext ctx, int searchScope,  String searchBase, String searchFilter, String[] attributes) throws NamingException {
        NamingEnumeration result = executeSearch(ctx, searchScope,  searchBase, searchFilter, attributes);

        SearchResult sr = null;
        // Loop through the search results
        if (result.hasMoreElements()) {
            sr = (SearchResult) result.next();
        }
        return sr;
    }

    private class CertificateList {
        private String email;
        private Set<EncodedCertificate> certificates;
        private AtomicLong lastUsed;

        private CertificateList(String email, Set<EncodedCertificate> certificates) {
            this.email = email;
            this.certificates = certificates;
            this.lastUsed = new AtomicLong(System.currentTimeMillis());
        }

        private boolean contains(EncodedCertificate certificate){
            this.lastUsed.set(System.currentTimeMillis());
            return certificates.contains(certificate);
        }

        private boolean isObsolete(){
            return (lastUsed.get()+sessionTimeoutSeconds*100)<System.currentTimeMillis();
        }

        private String getProvider(){
            return LdapUserCheckerService.this.provider;
        }

        @Override
        public boolean equals(Object obj) {
            //1. Compare number of certifications
            CertificateList another = (CertificateList) obj;
            if(another.certificates.size()!=this.certificates.size()){
                System.out.println("Different number of certificates between "+this.getProvider()+" and "+another.getProvider());
            }

            //2. Compare contents
            return CollectionUtils.containsAny(another.certificates, this.certificates)
                && CollectionUtils.containsAny(this.certificates   , another.certificates);
        }
    }

    private static class EncodedCertificate {
        private byte[] encoded;

        private EncodedCertificate(byte[] encoded) {
            this.encoded = encoded;
        }

        private EncodedCertificate(X509Certificate certificate) throws CertificateEncodingException {
            this.encoded = certificate.getEncoded();
        }

        public EncodedCertificate(Certificate certificate) throws CertificateEncodingException {
            this.encoded = certificate.getEncoded();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EncodedCertificate)) return false;

            EncodedCertificate that = (EncodedCertificate) o;

            if (!Arrays.equals(encoded, that.encoded)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return encoded != null ? Arrays.hashCode(encoded) : 0;
        }
    }
    
    public static void main(String[] args) throws Exception {
    	
    	FileInputStream fis = new FileInputStream("C:\\Users\\sbt-gordienko-mv\\mgordienko\\_COMMON\\certificate_novoselov\\client_cert.der");
    	Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(fis);
    	fis.close();

        LdapUserCheckerService[] loaders = new LdapUserCheckerService[5];
        TagLogger tagLogger = TagLogger.getTagLogger(LdapUserCheckerService.class, Logger.getLogger(LdapUserCheckerService.class), "debug");
        for(int i=0; i<5; i++){
            loaders[i] = new LdapUserCheckerService();
            loaders[i].setProvider("ldap://lake"+(i+1) + ".sigma.sbrf.ru:389");
            loaders[i].doInit();
            loaders[i].tagLogger = tagLogger;
        }
    	
        //1. Creating loaders
    	while(true) {
	
	        //2. For every loader we request certificate for Novoselov and compare them
	        String EMAIL = "RONovoselov.SBT@sberbank.ru";
	        //String EMAIL = "INTsukanov@sberbank.ru";
//	        System.out.println("TESTING FOR "+EMAIL);
	        CertificateList previous = null;
	        System.out.print(new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(new Date()) + ": ");
	        for(int i=0; i<5; i++){
	            boolean result = loaders[i].checkCachedUserCertByEmail(EMAIL, cert);
	            System.out.print(result + "; ");
//	            if(previous!=null){
//	              //  System.out.println("EQUALS for "+loaders[i].provider+" and "+loaders[i-1].provider+" = " + previous.equals(current));
//	            }
//	            previous = current;
	            loaders[i].doRun();
	        }
            System.out.println();
	        
	        Thread.sleep(70000);
    	}
    }

}