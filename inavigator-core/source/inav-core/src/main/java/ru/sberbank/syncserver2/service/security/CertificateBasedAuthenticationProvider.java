package ru.sberbank.syncserver2.service.security;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * This class is using for LDAP certificate based user authentication. It is
 * planned to extract common functionality to separate module Code is based on
 * SBT-Karmanov-AV's
 * ru.sberbank.mobilelibrary.security.MobileLibAuthenticationProvider
 *
 * @author Yuliya Solomina
 *
 */
public class CertificateBasedAuthenticationProvider extends
		SberbankAuthenticationProvider {
	protected IGroupLoader loader;
	protected Logger logger = Logger.getLogger(CertificateBasedAuthenticationProvider.class);


	public CertificateBasedAuthenticationProvider(IGroupLoader loader) {
		this.loader = loader;
	}

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		logger.info("authentication = " + authentication);
		String principal = (String) authentication.getPrincipal();

		try {
			X509Certificate credentials = (X509Certificate) authentication
					.getCredentials();
			// check certificate expire date, throwing exception if expired
			credentials.checkValidity();

			Set<String> userGroups;

			userGroups = loader.getUserGroups(principal, credentials);

			if (userGroups.size() == 0) {
				return null;
			}

			ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			for (String group : userGroups) {
				authorities.add(new SimpleGrantedAuthority(group));
			}

			return new PreAuthenticatedAuthenticationToken(principal, "123",
					authorities);
		} catch (Exception e) {
			logger.warn("Exception during auth&auth for credentionals " + authentication, e);
			if (loader.isStub()) {
				ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				authorities.add(new SimpleGrantedAuthority("ADMIN"));

				return new PreAuthenticatedAuthenticationToken(principal, "123",
						authorities);
			} else if (! (e instanceof AuthenticationException)) {
					throw new SecurityException(e.getMessage(), e);
			} else {
				throw (AuthenticationException) e;
			}
		}
	}

	@Override
	public boolean supports(Class<?> aClass) {
		return aClass.isInstance(new PreAuthenticatedAuthenticationToken(null,
				null));
	}

}
