package ru.sberbank.syncserver2.gui.web;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import ru.sberbank.syncserver2.gui.data.AuthContext;
import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.gui.data.EmployeeRole;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.gui.util.JSPHelper;
import ru.sberbank.syncserver2.gui.util.MessageDigestHelper;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChangePasswordController extends DatabaseController {

	public ChangePasswordController() {
		super(ChangePasswordController.class);
	}

	@Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
		throws Exception {
		String servletPath = HttpRequestUtils.getFulRequestPath(request);
        AuthContext ctx = getAuthContext(request);
		LOGGER.info("showForm [" + getAuthContext(request) + "] - " + servletPath
			+ (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
        if (servletPath.contains("show.changepassword.gui")) {
            ModelAndView mv = new ModelAndView("changePassword");
            mv.addObject("adminMode", request.getParameter("adminMode"));
            return mv;
		} else {
			return new ModelAndView("index");
		}
	}

	private ModelAndView publicWelcome(HttpServletRequest request, HttpServletResponse response, BindException errors) {
		ModelAndView mv = new ModelAndView("welcome");
		return mv;
	}

	@Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o,
		BindException e) throws Exception {
        //1. Parsing params
        String currentPassword = request.getParameter("currentPassword");
		String password = request.getParameter("password");
        String passwordAgain = request.getParameter("passwordAgain");
        boolean adminMode = "true".equalsIgnoreCase(request.getParameter("adminMode"));

        //2. Check permissions
        Employee loggedEmployee = getEmployee(request);
        if(loggedEmployee==null){
            return showMessage("Перед сменой пароля необходимо войти в систему");
        }
        int employeeId = JSPHelper.getIntegerParameter(request, "employeeId",-1);
        if(employeeId==-1){
            if(!adminMode){
                employeeId = loggedEmployee.getEmployeeId();
            }
        }
        Employee changeableEmployee = database.getEmployee(employeeId);
        if(changeableEmployee==null){
            return showMessage("Нельзя сменить пароль для неизвестного пользователя");
        }

        //3. If the current password is not displayed then it's admin mode and only admins could do it
        if(adminMode){
            if(loggedEmployee.getEmployeeRoleId()!=EmployeeRole.ADMIN){
                return showMessage("Только администратор может сменить пароль без ввода текущего пароля");
            }
        } else {
            String savedPassword = database.getPassword(changeableEmployee.getEmployeeEmail());
            if(!MessageDigestHelper.toDigest(currentPassword).equals(savedPassword)){
                return showError(employeeId, "Неправильно введен текущий пароль", adminMode);
            }
        }

        //4. Проверяем совпадение паролей
        if(JSPHelper.isStringEmpty(password) || JSPHelper.isStringEmpty(password)){
            return showError(employeeId,"Пароль не может быть пустым",adminMode);
        }
        if(!password.equals(passwordAgain)){
            return showError(employeeId,"Пароли должны совпадать",adminMode);
        }

        //5. Все хорошо - сохраняем введенный пароль
        database.changePassword(changeableEmployee.getEmployeeEmail(), password);
        AuditHelper.write(request, "Изменение пароля", "Изменение пароля для пользователя "+changeableEmployee.getEmployeeEmail(), AuditHelper.EMPLOYEE);
        return showMessage("Пароль успешно изменен.");
	}

    private ModelAndView showMessage(String txt) {
        ModelAndView mv = new ModelAndView("changePasswordSuccess");
        mv.addObject("successText", txt);
        return mv;
    }

    private ModelAndView showError(int employeeId, String error, boolean adminMode){
        ModelAndView mv = new ModelAndView("changePassword");
        mv.addObject("employeeId" ,employeeId);
        mv.addObject("error"    , error);
        mv.addObject("adminMode", adminMode);
        return mv;
    }

	private ModelAndView showResetPassword(HttpServletRequest request, HttpServletResponse response,
		BindException errors) {

		ModelAndView mv = new ModelAndView("resetPassword");
		mv.addObject("email", request.getParameter("email"));

		return mv;
	}
}
