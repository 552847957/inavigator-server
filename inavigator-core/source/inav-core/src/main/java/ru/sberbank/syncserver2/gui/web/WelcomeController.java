package ru.sberbank.syncserver2.gui.web;


import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.sberbank.syncserver2.gui.data.AuthContext;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: kozhleo
 * Date: Oct 1, 2009
 * Time: 11:29:40 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class WelcomeController extends DatabaseController {

    public WelcomeController() {
        super(WelcomeController.class);
    }

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
		throws Exception {
		//1. Processing stuff not related to specific existing schedule
		String servletPath = HttpRequestUtils.getFulRequestPath(request);
		LOGGER.info("showForm [" + getAuthContext(request) + "] - " + servletPath
			+ (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
		if (servletPath.contains("welcome.public.gui")) {
			return publicWelcome(request, response, errors);
        } else if (servletPath.contains("notimplemented.public.gui")) {
            return publicNotImplemented(request, response, errors);
        } else {
            ModelAndView mv = new ModelAndView("security");
            mv.addObject("message","Unexpected servlet path - "+servletPath);
            return mv;
        }
    }


    private ModelAndView publicWelcome(HttpServletRequest request, HttpServletResponse response, BindException errors) {
        AuthContext ctx = getAuthContext(request);
        if(ctx==null || ctx.getEmployee()==null){
            return new ModelAndView("login");
        } else {
            return new ModelAndView("welcome");
        }
    }

    private ModelAndView publicNotImplemented(HttpServletRequest request, HttpServletResponse response, BindException errors) {
        ModelAndView mv = new ModelAndView("notimplemented");
        return mv;
    }

}
