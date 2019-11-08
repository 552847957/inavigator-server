package ru.sberbank.syncserver2.gui.web;


import java.io.File;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import ru.sberbank.syncserver2.gui.data.AuthContext;
import ru.sberbank.syncserver2.gui.util.PasswordGenerator;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA. User: leo Date: 17-Aug-2005 Time: 22:54:24 To
 * change this template use File | Settings | File Templates.
 */

public class PasswordController extends DatabaseController {

	public PasswordController() {
		super(PasswordController.class);
	}

	protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
		throws Exception {
		String servletPath = HttpRequestUtils.getFulRequestPath(request);
		LOGGER.info("showForm [" + getAuthContext(request) + "] - " + servletPath
			+ (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
		if (servletPath.contains("welcome.public.gui")) {
            AuthContext ctx = getAuthContext(request);
            if(ctx==null || ctx.getEmployee()==null){
            	
                return new ModelAndView("login");
            } else {
                return new ModelAndView("welcome");
            }
		} else if (servletPath.contains("reset.password.gui")) {
			return showResetPassword(request, response, errors);
		} else {
			return new ModelAndView("index");
		}
	}

	private ModelAndView publicWelcome(HttpServletRequest request, HttpServletResponse response, BindException errors) {
		ModelAndView mv = new ModelAndView("welcome");
		return mv;
	}

	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o,
		BindException e) throws Exception {

		LOGGER.info("onSubmit [" + getAuthContext(request) + "] - " + request.getServletPath() + "?email="
			+ request.getParameter("email"));

		String email = request.getParameter("email");

		if (email != null && database.existEmployeeWithEmail(email)) {
			String password = PasswordGenerator.generatePassword();
			database.changePassword(email, password);
		}
		return new ModelAndView("welcome");
	}

	private ModelAndView showResetPassword(HttpServletRequest request, HttpServletResponse response,
		BindException errors) {

		ModelAndView mv = new ModelAndView("resetPassword");
		mv.addObject("email", request.getParameter("email"));

		return mv;
	}

}