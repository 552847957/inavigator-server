/**
 *
 */
package ru.sberbank.syncserver2.service.security;

import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * @author Yuliya Solomina
 *
 */
public interface IGroupLoader {

	/**
	 * @param principal
	 * @param credentials
	 * @return
	 */
	Set<String> getUserGroups(String email, X509Certificate credentials);

	/**
	 * @return
	 */
	boolean isStub();

}
