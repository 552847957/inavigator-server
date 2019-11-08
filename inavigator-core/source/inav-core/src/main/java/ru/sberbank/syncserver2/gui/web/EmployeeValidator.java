package ru.sberbank.syncserver2.gui.web;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.gui.db.DatabaseManager;
import ru.sberbank.syncserver2.gui.util.JSPHelper;

public class EmployeeValidator implements Validator {
    protected DatabaseManager database;

    public DatabaseManager getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseManager database) {
        this.database = database;
    }

    public boolean supports(Class aClass) {
        return aClass == Employee.class;
    }

    public void validate(Object o, Errors errors) {
        Employee e = (Employee) o;
        if (e.getEmployeeId() == -1) {
            String password = e.getEmployeePassword();
            String passwordAgain = e.getEmployeePasswordAgain();
            if ((JSPHelper.isStringEmpty(password) || JSPHelper.isStringEmpty(passwordAgain)
                    || !password.equals(passwordAgain))) {
                errors.rejectValue("employeePassword", "Пароль должен быть введен и совпадать с повтором пароля");
            }
            if (database.existEmployeeWithEmail(e.getEmployeeEmail()))
                errors.rejectValue("employeeEmail", "Пользователь с таким e-mail уже существует");
        }

        if (JSPHelper.isStringEmpty(e.getEmployeeName())) {
            errors.rejectValue("employeeName", "Имя не может быть пустым");
        }
    }
}
