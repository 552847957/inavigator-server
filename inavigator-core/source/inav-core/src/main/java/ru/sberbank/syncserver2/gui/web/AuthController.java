package ru.sberbank.syncserver2.gui.web;

/*
 * @author    Leonid Kohinsky <br>
 * @module
 * @version   $Revision: 1.1.1.1 $ <br>
 * @last mod  $Date: 2005/10/22 07:37:05 $ <br>
 */

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.Controller;

import ru.sberbank.syncserver2.gui.data.AuthContext;
import ru.sberbank.syncserver2.gui.data.AuthContextHolder;
import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.gui.data.EmployeeRole;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.gui.db.DatabaseManager;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

@org.springframework.stereotype.Controller
public class AuthController extends HandlerInterceptorAdapter implements Controller {

	private static final Logger LOGGER = Logger.getLogger(AuthController.class);

    private DatabaseManager databaseManager;

	private String loginPage;

	private static Map loggedUsers = new HashMap();

    private String newUserMessage;

    private final static String LOGIN_COOKIE = "login";
    
    private final static String CHANGE_PASSWORD_REDIRECT_URL = "show.changepassword.gui"; 


    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String servletPath = HttpRequestUtils.getFulRequestPath(request);
        LOGGER.info("#" + ".handleRequest - " + servletPath
			+ (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
		if (servletPath.contains("login.auth.gui")) {
		    //addVersionInfo(request);
			return login(request, response, false);
		} else if (servletPath.contains("logout.auth.gui")) {
			return logout(request, response);
        } else if (servletPath.contains("relogin.adminauth.gui")) {
            return relogin(request, response);
		} else {
			return null;
		}
	}

    public ModelAndView login(HttpServletRequest request, HttpServletResponse response, boolean isLoginAs) throws Exception {
        AuthContextHolder authHolder = (AuthContextHolder) WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext()).getBean("authHolder");

		//1. Getting username and password from request
		String login = request.getParameter("login");
		String password = request.getParameter("password");
        String loginAsEmail = request.getParameter("loginAsEmail");

        //2. Checking user name and password
        AuthContext ctx = null;
		if (login != null) {
			ctx = databaseManager.authenticate(login, password); //it returns non-NULL value
		} else {
			ctx = new AuthContext(AuthContext.GENERAL_FAILURE);
		}
		String resultPage = request.getParameter("forwardAction");
		if (resultPage == null || "".equals(resultPage.trim()) || "null".equals(resultPage)) {
			resultPage = "welcome.public.gui";
		}

        //4. Changing authentication
		int status = ctx.getStatus();
		if ((status == AuthContext.VALID_USER) && (ctx.getEmployee().isReadOnly() != true)) {
			request.getSession().setAttribute("user", ctx);

			LOGGER.info("#" + ".login [" + ctx + "] - redirection of valid user to "
				+ response.encodeURL(resultPage));
//			response.sendRedirect(response.encodeURL(resultPage));
			authHolder.setAuthContext(ctx);
	        AuditHelper.write(request, "Вход в систему", "", AuditHelper.LOGIN);

			// Если при первом входе не изменен пароль пользователя, то переадресуем его на страницу изменения пароля
        	if (!request.getRequestURI().contains(CHANGE_PASSWORD_REDIRECT_URL) && databaseManager.isEmployeeNeedChangePassword(ctx.getEmployee().getEmployeeEmail())) {
        		resultPage = CHANGE_PASSWORD_REDIRECT_URL+"?firstIn=1";
        	}
			
			return new ModelAndView("redirect:"+resultPage);
        } else {
			LOGGER.info("#" + ".login - redirection of invalid user to " + loginPage + " with status " + status);
			ModelAndView result = new ModelAndView(loginPage);
			result.addObject("forwardAction", resultPage);
			if (status == AuthContext.WRONG_USERNAME) {
                String contextPath = request.getContextPath();
                result.addObject("message", "Неизвестный пользователь");
			} else if (status == AuthContext.WRONG_PASSWORD) {
                result.addObject("message", "Неверный пароль");
            } else {
                Employee employee = ctx.getEmployee();
                //String detail = " The required role is "+role+" and you have just "+(employee==null ? "null":""+employee.getEmployeeRoleId());
                result.addObject("message", "Доступ запрещен");
            }
            authHolder.setAuthContext(ctx);
			return result;
		}
//        authHolder.setAuthContext(ctx);
//		return null;
	}
    
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOGGER.info("#" + ".logout [" + AuthController.getAuthContext(request) + "]");		
		AuditHelper.write(request, "Выход из системы", "", AuditHelper.LOGIN);
		request.getSession().removeAttribute("user");
		request.getSession().invalidate();		
//		String resultPage = loginPage + ".jsp";
//		response.sendRedirect(response.encodeURL(resultPage));
		return new ModelAndView(loginPage);
	}

    public ModelAndView relogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOGGER.info("#" + ".relogin [" + AuthController.getAuthContext(request) + "]");	
		AuditHelper.write(request, "Выход из системы", "", AuditHelper.LOGIN);
        request.getSession().removeAttribute("user");
        request.getSession().invalidate();
        return new ModelAndView("loginas");
    }

    private boolean checkAccess(HttpServletRequest request) {
        AuthContext ctx = getAuthContext(request);
		String baseUrI = request.getRequestURI();
		// check input params
		if (baseUrI == null || ctx == null || ctx.getEmployee() == null)
			return false;
		boolean checkResult =  
				((ctx.getEmployee().getEmployeeRoleId() == EmployeeRole.ADMIN) ||
				((ctx.getEmployee().getEmployeeRoleId() == EmployeeRole.OPERATOR) &&
				(
				 baseUrI.contains(".generator.gui") || 
				 baseUrI.contains("show.changepassword.gui")
				)) ||
				((ctx.getEmployee().getEmployeeRoleId() == EmployeeRole.OPERATOR_MIS_ACCESS) &&
				(
						baseUrI.contains(".mobaccess.gui") || 
						baseUrI.contains("show.changepassword.gui") ||
						baseUrI.contains("mobile-access-admin")
						))
				);

		LOGGER.info("#" + ".checkAccess [" + ctx + "] to " + baseUrI + " access" + (checkResult?"OK":"DEDIED"));
		return checkResult;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		//1. Check if user authenticated
        AuthContext ctx = getAuthContext(request);
		if (ctx != null && ctx.getEmployee()!=null) {
			if (LOGGER.isEnabledFor(Level.ALL)) {
				LOGGER.log(Level.ALL, "#" + ".preHandle [" + ctx + "] - Success");
			}
            ctx.setLastUsed();
            synchronized (loggedUsers){
                loggedUsers.put(ctx, new Date());
            }
            Employee employee = ctx.getEmployee();
            String email = employee.getEmployeeEmail();
            Cookie cookie = new Cookie(LOGIN_COOKIE, email);
            response.addCookie(cookie);
            
            // если не изменен пароль пользователя, то переадресует на страницу изменения пароля
        	if (!request.getRequestURI().contains(CHANGE_PASSWORD_REDIRECT_URL) && databaseManager.isEmployeeNeedChangePassword(ctx.getEmployee().getEmployeeEmail())) {
        		response.sendRedirect(CHANGE_PASSWORD_REDIRECT_URL + "?firstIn=1");
        		return false;
        	}
            
            if (checkAccess(request))
            	return true;
		} else if (ctx != null) {
			LOGGER.warn("#" + ".preHandle [" + ctx + "] - Access denied");
		} else {
			LOGGER.error("#" + ".preHandle - There is no context");
		}

		//2. Process incorrect authentication
		ModelAndView mv = new ModelAndView(loginPage);
		if (ctx != null && ctx.getEmployee()==null) {
			mv.addObject("message", "Access Denied");
		}

		//3. Process case when no authentication
		String url = request.getPathInfo().substring(1);
		String query = request.getQueryString();
		if (query != null) {
			url = url + "?" + query;
		}
		mv.addObject("forwardAction", response.encodeRedirectURL(url));
		throw new ModelAndViewDefiningException(mv);
	}

    public static AuthContext getAuthContext(HttpServletRequest request) {
        HttpSession session = request.getSession();
        AuthContext ctx = (AuthContext) session.getAttribute("user");
        return ctx;
    }

    @Autowired
    public DatabaseManager getDatabase() {
        return databaseManager;
    }

    public void setDatabase(DatabaseManager databaseManager) {
        //1. Set database
        this.databaseManager = databaseManager;

        //2. Set new user message
        this.newUserMessage = "";

    }

	public void setLoginPage(String loginPage) {
		this.loginPage = loginPage;
	}

    public static String getLoginCookie(HttpServletRequest request) {
        //1. Check if any cookie is defined
        Cookie[] cookies = request.getCookies();
        if(cookies==null){
            return "";
        }

        //2. Finding login cookie
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if(cookie!=null && LOGIN_COOKIE.equalsIgnoreCase(cookie.getName()) ){
                return cookie.getValue();
            }
        }
        return "";
    }

}
