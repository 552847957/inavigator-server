package ru.sberbank.syncserver2.gui.db.dao;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Component;

import ru.sberbank.syncserver2.gui.data.AuthContext;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.gui.data.SQLDescriptor;
import ru.sberbank.syncserver2.gui.data.SyncConfig;
import ru.sberbank.syncserver2.gui.util.MessageDigestHelper;
import ru.sberbank.syncserver2.service.pub.xml.Settings.Setting;

@Component
public class AdminConsoleDao extends BaseDao {
	
	public Map<String, Setting> getProperties() {
		List<SyncConfig> list = super.list(SyncConfig.class);
		Map<String, Setting> settings = new HashMap<String, Setting>(list.size());
		for (SyncConfig config: list) {
			settings.put(config.getPropertyKey(),new Setting(config.getPropertyKey(), config.getPropertyValue(), config.getPropertyDesc()));
		}	
		return settings;
	}
	
    public void updateProperty(String value, String key){
        SyncConfig p = new SyncConfig();
        p.setPropertyValue(value);
        p.setPropertyKey(key);
        super.update(p);
    }
    public AuthContext authenticate(String email, String password) {
        //1. Check parameters
        if (email == null || password == null) {
            return new AuthContext(AuthContext.NO_PERMISSION);
        }

        //2. Generate hash from password
        String passwordMD5;
        try {
            passwordMD5 = MessageDigestHelper.toDigest(password);
        } catch (NoSuchAlgorithmException e) {
            return new AuthContext(AuthContext.NO_PERMISSION);
        }

        //3. Check if user and password are valud
        String whereForUserName = "WHERE leave_date is null and lower(EMPLOYEE_EMAIL)=lower('" + format(email) + "')";
        String whereForPassword = " and EMPLOYEE_PASSWORD='" + format(passwordMD5) + "'";
        Employee employee = super.get(Employee.class, null, whereForUserName + whereForPassword, null);
        if(employee!=null){
            return new AuthContext(employee);
        }

        //4. Check whether username is wrong
        employee = super.get(Employee.class, null, whereForUserName, null);
        if(employee!=null){
            return new AuthContext(AuthContext.WRONG_PASSWORD);
        } else {
            return new AuthContext(AuthContext.WRONG_USERNAME);
        }
    }
    private static String format(String str) {
        return str.replace("'", "''");
    }
    
    public List<Employee> listEmployees() {
        return super.list(Employee.class);
    }
    
    public List<Employee> listEmployeesWithPass() {
    	List<Employee> employees = super.list(Employee.class);
    	List<String> passwords = super.getStringList("SELECT EMPLOYEE_PASSWORD FROM EMPLOYEES ORDER BY EMPLOYEE_ID");
    	for (int i=0;i<employees.size();i++) {
    		employees.get(i).setEmployeePassword(passwords.get(i));
    	}
    	return employees;
    }
    
    public boolean setEmployess(List<Employee> employees) {
    	int size = employees.size();
    	SQLDescriptor<Employee> descriptor = new SQLDescriptor<Employee>() {
    		public String composeSQL(Object o, int queryType){
    			throw new IllegalArgumentException("Unexpected query type - "+queryType);
            }

            public String composePrepareSQL(int queryType){

            	switch(queryType){
                case SQLDescriptor.SQL_INSERT  :
                    return "INSERT INTO EMPLOYEES(EMPLOYEE_ROLE_ID,EMPLOYEE_EMAIL,EMPLOYEE_NAME,EMPLOYEE_PASSWORD,IS_REMOTE,IS_READ_ONLY,PASSWORD_CHANGED_DATE)\n "
                         + "VALUES(               ?               ,?             ,?            ,? 				 ,?			,?			,?)";
                default :
                    throw new IllegalArgumentException("Unexpected query type - "+queryType);
            	}
            }

            public void setParameters(Object o, PreparedStatement st,int queryType)
                throws SQLException {
                Employee v = (Employee)o;
                if(queryType==SQL_INSERT){
                    int i=1;
                    st.setInt(i++, v.getEmployeeRoleId());
                    st.setString(i++, v.getEmployeeEmail());
                    st.setString(i++, v.getEmployeeName());
                    st.setString(i++, v.getEmployeePassword());
                    st.setBoolean(i++, v.isRemote());
                    st.setBoolean(i++, v.isReadOnly());
                    st.setString(i++, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));                    
                } else {
                   throw new IllegalArgumentException("Unexpected query type - "+queryType);
                }
             }

             public Employee newInstance(ResultSet rs) throws SQLException {
                throw new UnsupportedOperationException();
            }
		};
    	size -=super.insert(Employee.class, employees, descriptor);
    	return size==0;
    }
    
    public Employee getEmployeeByEmail(String email) {
        String where = "WHERE lower(employee_email) = lower('" + email + "')";
        return super.get(Employee.class, null, where, null);
    }

    public boolean existEmployeeWithEmail(String email) {
        String sql = "FROM EMPLOYEES WHERE lower(EMPLOYEE_EMAIL)=lower('" + format(email) + "')";
        return super.exists(sql);
    }

    public boolean changePassword(String email, String password) {

        try {
            password = MessageDigestHelper.toDigest(password);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }      
        String sql = "UPDATE EMPLOYEES SET EMPLOYEE_PASSWORD='" + format(password) + "',PASSWORD_CHANGED_DATE = '" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "' WHERE lower(EMPLOYEE_EMAIL)=lower('"
                + format(email) + "')";
        super.execute(sql);
        return true;
    }

    public Employee getEmployee(int employeeId) {
        return super.get(Employee.class, new int[]{employeeId});
    }

    public void saveEmployee(Employee employee) {
        if (employee.getEmployeeId() == -1) {
            super.insert(employee);
        } else {
            super.update(employee);
        }
    }

    public void deleteEmployee(int employeeId) {
        super.delete(Employee.class, new int[]{employeeId});
    }
    

}
