package ru.sberbank.syncserver2.gui.web;


import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.web.servlet.mvc.SimpleFormController;
import ru.sberbank.syncserver2.gui.data.AuthContext;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.gui.db.DatabaseManager;

import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA. User: Administrator Date: 25.04.2005 Time: 11:26:00
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseController extends SimpleFormController {

	protected Logger LOGGER = Logger.getLogger(getClass());

	protected DatabaseManager database;


    public DatabaseController() {
        super.setBindOnNewForm(false);
    }

	public DatabaseController(Class loggerClass) {
	    // TODO remove parameter
		super.setBindOnNewForm(false);
	}

	public DatabaseManager getDatabase() {
		return database;
	}

	public void setDatabase(DatabaseManager database) {
		this.database = database;
	}

	public static AuthContext getAuthContext(HttpServletRequest request) {
		return (AuthContext) request.getSession().getAttribute("user");
	}

	public Employee getEmployee(HttpServletRequest request) {
		AuthContext user = getAuthContext(request);
		Employee employee = user == null ? null : user.getEmployee();
		return employee;
	}

	public int getEmployeeId(HttpServletRequest request) {
		AuthContext user = getAuthContext(request);
		Employee employee = user == null ? null : user.getEmployee();
		return employee == null ? -1 : employee.getEmployeeId();
	}
}
