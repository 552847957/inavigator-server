package ru.sberbank.syncserver2.gui.db;


import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Controller;

import ru.sberbank.syncserver2.gui.data.*;
import ru.sberbank.syncserver2.gui.util.MessageDigestHelper;
import ru.sberbank.syncserver2.service.core.config.Folder;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;

import javax.sql.DataSource;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: leo Date: 15-Aug-2005 Time: 23:02:03 To
 * change this template use File | Settings | File Templates.
 */
@Controller
public class DatabaseManager extends DatabaseServices {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class);
    private DataSource configSource;
    protected JdbcTemplate jdbcTemplate;
    
    public DatabaseManager() {
        super();
    }

    public DatabaseManager(String jndiName) {
        super(jndiName);
    }

    public DatabaseManager(javax.sql.DataSource dataSource) {
        super(dataSource);
    }

    private static String format(String str) {
        return str.replace("'", "''");
    }

    
    
    @Autowired
    @Qualifier("configSource")
	public void setDataSource(DataSource dataSource) {
		// TODO Auto-generated method stub
		super.setDataSource(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Autowired
    public DataSource getConfigSource() {
        return super.getDataSource();
    }

    public void setConfigSource(DataSource configSource) {
        super.setDataSource(configSource);
    }

    /**
     * ************************************************************************
     * AUTHENTICATION FUNCTIONS
     * ************************************************************************
     */
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
            LOGGER.error("authenticate - Can't calculate message digest", e);
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

    /**
     * ************************************************************************
     * FUNCTIONS FOR EMPLOYEE MANAGEMENT
     * ************************************************************************
     */
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
            LOGGER.error("changePassword - Can't calculate message digest", e);
            return false;
        }
        // При смене пароля необходимо обязательно обновлять поле PASSWORD_CHANGED_DATE 
        // для того, чтобы не конфликтовало с Sqlite базой данных не используем в общем SQL коде специфических функций какой либо базы данных ( например getDate() )  
        String sql = "UPDATE EMPLOYEES SET EMPLOYEE_PASSWORD='" + format(password) + "',PASSWORD_CHANGED_DATE = '" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "' WHERE lower(EMPLOYEE_EMAIL)=lower('"
                + format(email) + "')";
        super.execute(sql);

        return true;
    }

    public String getPassword(String email) {
        return getStringValue("SELECT EMPLOYEE_PASSWORD FROM EMPLOYEES WHERE lower(EMPLOYEE_EMAIL)=lower('" + format(email) + "')");
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

    public List listEmployees() {
        return super.list(Employee.class);
    }

    public List listEmployeeRoles() {
        return super.list(EmployeeRole.class);
    }

    public List listProperties() {
        return super.list(SyncConfig.class);
    }

    public void updateProperty(String value, String key){
        SyncConfig p = new SyncConfig();
        p.setPropertyValue(value);
        p.setPropertyKey(key);
        super.update(p);
    }

    /**
     * THE PROCEDURES FOR CONFIGURATION SERVER SHOULD WORK BOTH IN SQLITE AND MSSQL
     * @return
     */
    public List<String> listClientApplications() {
        return super.getStringList("select APP_BUNDLE from CONFSERVER_APPS ORDER BY 1");
    }

    public List listClientAppVersions(String appBundle) {
        String sql = "select APP_VERSION\n" +
                     "from CONFSERVER_VERSIONS\n" +
                     "where app_id in (select app_id from CONFSERVER_APPS where app_bundle='"+appBundle+"')\n";
        return super.getStringList(sql);
    }

    public List listClientProperties(String appBundle, String appVersion) {
        String sql = "select property_code property, property_value value\n" +
                     "from CONFSERVER_PROPERTY_VALUES\n" +
                     "where app_id in (select app_id from CONFSERVER_APPS where app_bundle='"+appBundle+"')\n" +
                     "  and app_version='"+appVersion+"'";
        return super.list(ClientConfig.class, sql, "", null,null);
    }

    public void insertClientAppVersion(String appBundle, String appVersion) {
        //1. Finding application
        int appId = super.getIntValue("select app_id from CONFSERVER_APPS where app_bundle='"+appBundle+"'");
        if(appId==-1){
            return;
        }

        //2. Check if app and version already exists and add
        boolean exists = super.getIntValue("select count(*) from CONFSERVER_VERSIONS where app_id="+ appId+ " and app_version='" + appBundle + "'")>0;
        if(!exists){
            //below are 3 sql because the code should run in MSSQL and sqlite without changes
            String sql1 = "insert into CONFSERVER_VERSIONS(APP_ID, APP_VERSION) values("+ appId+ ",?)\n";
            String sql2 = "delete from CONFSERVER_PROPERTY_VALUES where app_id="+ appId+ " and app_version=? \n";
            String sql3 = "insert into CONFSERVER_PROPERTY_VALUES(APP_ID, PROPERTY_CODE, APP_VERSION , PROPERTY_VALUE)\n" +
                         "select                                "+ appId+ ", PROPERTY_CODE, ?, ' '\n" +
                         "from CONFSERVER_PROPERTY_TEMPLATES\n" +
                         "where app_id="+ appId;
            super.executePatternUnicode(sql1,new Object[]{appVersion},null);
            super.executePatternUnicode(sql2,new Object[]{appVersion},null);
            super.executePatternUnicode(sql3,new Object[]{appVersion},null);
        }
    }

    public void deleteClientAppVersion(String appBundle, String appVersion) {
        //1. Finding application
        int appId = super.getIntValue("select app_id from CONFSERVER_APPS where app_bundle='"+appBundle+"'");
        if(appId==-1){
            return;
        }

        //2. Deleting
        String sql1 = "delete from CONFSERVER_PROPERTY_VALUES\n" +
                      "where app_id=?\n" + "  and app_version=?\n";
        super.executePatternUnicode(sql1, new Object[]{appId,appVersion}, null);

        String sql2 = "delete from CONFSERVER_VERSIONS where app_id=? and app_version=? \n";
        super.executePatternUnicode(sql2, new Object[]{appId,appVersion}, null);
    }

    public void updateClientProperty(String appBundle, String appVersion, String property, String value) {
        //1. Finding application
        int appId = super.getIntValue("select app_id from CONFSERVER_APPS where app_bundle='"+appBundle+"'");
        if(appId==-1){
            return;
        }

        //2. Updating
        String sql = "update CONFSERVER_PROPERTY_VALUES set property_value=?\n" +
                     "where app_id=? and app_version=? and property_code=?\n";
        super.executePatternUnicode(sql, new Object[]{value, appId, appVersion, property, }, null);
    }

    /**
     * Проверка, необходимо ли обязать пользователя поменять пароль(если не заполнена дата изменения пароля)
     * @param email
     * @return
     */
    public boolean isEmployeeNeedChangePassword(String email) {
    	return getIntValue("SELECT COUNT(*) FROM EMPLOYEES WHERE EMPLOYEE_EMAIL = '" + email + "' AND PASSWORD_CHANGED_DATE IS NOT NULL") == 0;
    }
    
    public static void main(String[] args) {
        //1. Creating datasource
        DatabaseManager db = new DatabaseManager();
        DataSource ds =new SimpleDriverDataSource(new SQLServerDriver(),
                "jdbc:sqlserver://10.21.25.55:1433;databaseName=confserver", "syncserver", "123456");
        db.setDataSource(ds);
        System.out.println(db.listClientApplications());
        System.out.println(db.listClientAppVersions("balance"));
        System.out.println(db.listClientProperties("balance", "1.0"));
//        db.updateClientProperty("balance", "1.0", "offlineServer1", "changed");
//        System.out.println(db.listClientProperties("balance", "1.0"));
        db.insertClientAppVersion("balance", "4.0");
        System.out.println("balance/4.0 : "+db.listClientProperties("balance", "4.0"));
        db.deleteClientAppVersion("balance", "4.0");
        System.out.println("balance/4.0 : "+db.listClientProperties("balance", "4.0"));
        System.out.println("balance/5.0 : "+db.listClientProperties("balance", "5.0"));

/*
        //2. Listing employees
        List employees = db.listEmployees();
        Employee employee = (Employee) employees.get(0);
        System.out.println(employees);

        //3. Amending email
        employee.setEmployeeEmail("admin@mail.ru");
        db.saveEmployee(employee);

        employees = db.listEmployees();
        System.out.println(employees);

        //4. Authenticating
        AuthContext ctxv = db.authenticate("admin@mail.ru","1234");
        System.out.println("VALID CTX = "+ctxv.getStatus());

        AuthContext ctxi = db.authenticate("admin@mail.ru","123");
        System.out.println("INVALID CTX = "+ctxi.getStatus());

        //3. Adding new employee
        employee = new Employee();
        employee.setEmployeeEmail("user@mail.ru");
        employee.setEmployeePassword("hello");
        db.saveEmployee(employee);
        db.changePassword("user@mail.ru","bye");

        employees = db.listEmployees();
        System.out.println(employees);

        int last = employees.size()-1;
        employee = (Employee) employees.get(last);
        String password = db.getPassword(employee.getEmployeeEmail());
        System.out.println("Password for "+employee.getEmployeeEmail()+" is "+password);
        employee = db.getEmployeeByEmail("user@mail.ru");
        System.out.println(employee);
        db.deleteEmployee(employee.getEmployeeId());
*/
        List  props = db.listProperties();
        System.out.println(props);

        db.updateProperty("q","q");
    }

}