/**
 *
 */
package ru.sberbank.syncserver2.util;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Yuliya Solomina
 *
 */
public class HttpRequestUtils {
    private static String SYNC_USER_NAME = "SYNC_USER_NAME";

    public static void setUsernameToRequest(HttpServletRequest request, String userName) {
        request.setAttribute(SYNC_USER_NAME, userName);
    }

	public static String getUsernameFromRequest(HttpServletRequest request) {
        //1. By default we use SYNC_USER_NAME
        String userName = null;
        try {
            userName = (String) request.getAttribute(SYNC_USER_NAME);
            // TODO: add null check
            return userName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //2. On fail we use principal
        
        // TODO: implement correct getting of username 
        Principal principal = request.getUserPrincipal();
		if (principal != null) {
			userName = principal.getName();
		}

        //3. Here we also could define default user for anonymous
        //userName = "RONovoselov.SBT@sberbank.ru";
        return userName;
	}

	public static String getClientIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
            //System.out.println("Proxy-Client-IP = "+ip);
        }
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
            //System.out.println("WL-Proxy-Client-IP = "+ip);
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
            //System.out.println("HTTP_CLIENT_IP = "+ip);
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            //System.out.println("HTTP_X_FORWARDED_FOR = "+ip);
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
            //System.out.println("getRemoteAddr = "+ip);
		}
		return ip;
	}

	public static String getFulRequestPath(HttpServletRequest request) {
		String uri = request.getScheme()
				+ "://"
				+ request.getServerName()
				+ ("http".equals(request.getScheme())
						&& request.getServerPort() == 80
						|| "https".equals(request.getScheme())
						&& request.getServerPort() == 443 ? "" : ":"
						+ request.getServerPort())
				+ request.getRequestURI()
				+ (request.getQueryString() != null ? "?"
						+ request.getQueryString() : "");

		return uri;
	}

    public static String getServerAddressWithOtherServer(HttpServletRequest request, String otherServerName) {
        String uri = request.getScheme()
                + "://"
                + otherServerName
                + ("http".equals(request.getScheme())
                && request.getServerPort() == 80
                || "https".equals(request.getScheme())
                && request.getServerPort() == 443 ? "" : ":"
                + request.getServerPort());
        return uri;
    }

}
