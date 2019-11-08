package ru.sberbank.syncserver2.gui.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.validation.BindException;

import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;


@Controller
public class EmployeeController extends DatabaseController {
    public EmployeeController() {
        super(EmployeeController.class);
    }

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        String servletPath = HttpRequestUtils.getFulRequestPath(request);
        if(servletPath.contains("list.employee.gui") ){
            return showListForm(request,response,errors);
        } else if(servletPath.contains("edit.employee.gui") ){
            return showEditForm(request,response,errors);
        } else if(servletPath.contains("delete.employee.gui") ){
            return delete(request,response,errors);
        } else {
            return new ModelAndView("index");
        }
    }

    protected ModelAndView showEditForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        //1. Parsing input parameters
        int employeeId = -1;
        try {
            employeeId = Integer.parseInt( request.getParameter("employeeId") );
        } catch (NumberFormatException e1) {
            logger.debug("Missing employeeId - severe logic problem");
            return new ModelAndView("index");
        }

        //2. Loading employee if necessary
        Employee target = (Employee) errors.getTarget();
        Employee employee = super.isFormSubmission(request) ? target : database.getEmployee(employeeId) ;
        if(employee==null){
            employee = new Employee() ;
        }
        List roles = database.listEmployeeRoles();
        ModelAndView mv = new ModelAndView("employeeEdit");
        mv.addObject("employee"      , employee);
        mv.addObject("errors"        , errors );
        mv.addObject("roles"         , roles);
        return mv;
    }

    protected ModelAndView showListForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        List employees = database.listEmployees();
        ModelAndView mv = new ModelAndView("employeeList");
        mv.addObject("employees", employees);
        return mv;
    }


    protected ModelAndView onSubmit(
        HttpServletRequest request,
        HttpServletResponse response,
        Object command,
        BindException errors)
        throws Exception {
        Employee employee = (Employee) command;
        Employee oldEmployee = database.getEmployee(employee.getEmployeeId());
        database.saveEmployee(employee);
        if (oldEmployee==null)
    		AuditHelper.write(request, "Создание пользователя", 
    				"Создание пользователя "+employee.getEmployeeEmail()+". \nСоздан пользователь \n"+employee, AuditHelper.EMPLOYEE); 
    	else   
    		AuditHelper.write(request, "Редактирование пользователя", 
    				"Редактирование пользователя "+employee.getEmployeeEmail()+". \nПользователь \n"+oldEmployee+"\n изменен на \n"+employee, AuditHelper.EMPLOYEE);        	
        
        return new ModelAndView("redirect:list.employee.gui");
    }

    protected ModelAndView delete(
        HttpServletRequest request,
        HttpServletResponse response,
        BindException errors)
        throws Exception {
        //1. Parsing input parameters
        int employeeId = -1;
        try {
            employeeId = Integer.parseInt( request.getParameter("employeeId") );
        } catch (NumberFormatException e1) {
            logger.debug("Missing employeeId - severe logic problem");
            return new ModelAndView("index");
        }

        //2. Deleting employee
        Employee oldEmployee = database.getEmployee(employeeId);
        database.deleteEmployee(employeeId);
        AuditHelper.write(request, "Удаление пользователя", "Удаление пользователя \n"+oldEmployee, AuditHelper.EMPLOYEE);
        return new ModelAndView("redirect:list.employee.gui");
    }
}
