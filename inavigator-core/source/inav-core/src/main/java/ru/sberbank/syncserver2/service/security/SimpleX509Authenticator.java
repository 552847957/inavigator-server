package ru.sberbank.syncserver2.service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.security.cert.X509Certificate;

/**
 * Created by sbt-kozhinsky-lb on 07.06.14.
 */
public class SimpleX509Authenticator extends SberbankAuthenticationProvider {
    private SimpleGrantedAuthority USER = new SimpleGrantedAuthority("USER");

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            //1. Get certificate
            System.out.println("AUTHENTICATE CALL");
            X509Certificate credentials = (X509Certificate) authentication.getCredentials();

            //2. Check certificate
            credentials.checkValidity();

            //3. Extract principal \
            String principal = (String) authentication.getPrincipal();
            System.out.println("AUTHENTICATED: "+principal);
            return new PreAuthenticatedAuthenticationToken(principal, USER);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception ex){
            ex.printStackTrace();
            throw new SecurityException("Unxpected security exception",ex);
        }

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isInstance(new PreAuthenticatedAuthenticationToken(null,null));
    }
}
