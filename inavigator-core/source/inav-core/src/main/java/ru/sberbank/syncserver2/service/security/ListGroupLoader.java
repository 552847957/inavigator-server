/**
 *
 */
package ru.sberbank.syncserver2.service.security;

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * @author Yuliya Solomina
 *
 */
public class ListGroupLoader implements IGroupLoader {

	private Set<String> testUserEmailSet = new HashSet<String>();

	/**
	 *
	 */
	public ListGroupLoader(String testUserEmails) {
		StringTokenizer tokenizer = new StringTokenizer(testUserEmails, ",; ");

		while (tokenizer.hasMoreElements()) {
			testUserEmailSet.add(tokenizer.nextToken().toLowerCase().trim());
		}
	}

	/* (non-Javadoc)
	 * @see com.sberbank.vmo.common.security.IGroupLoader#getUserGroups(java.lang.String, java.security.cert.X509Certificate)
	 */
	@Override
	public Set<String> getUserGroups(String email,
			X509Certificate credentials) {
//		if (testUserEmailSet.contains(email.toLowerCase())) {
			Set<String> result = new HashSet<String>();
			result.add("ADMIN");
			return result;
//		}

//		throw new SecurityException("The user is not in testUsersEmailSet " + testUserEmailSet + ". User = " + email);

	}

	@Override
	public boolean isStub() {
		return false;
	}

}
