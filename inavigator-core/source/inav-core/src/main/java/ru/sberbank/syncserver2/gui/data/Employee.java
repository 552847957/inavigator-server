package ru.sberbank.syncserver2.gui.data;

import ru.sberbank.syncserver2.gui.util.MessageDigestHelper;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlElement;


@XmlRootElement(name = "employee")
public class Employee implements Serializable{
    private int employeeId;
    private int employeeRoleId;
    private String employeeEmail;
    private String employeeName;
    private String employeePassword;
    private String employeePasswordAgain;
    private boolean remote = false;
    private boolean readOnly = false;

    public Employee() {
        this.employeeId = -1;
        this.employeeRoleId  = EmployeeRole.ADMIN;
        this.employeeEmail = "";
        this.employeeName = "";
    }


    public Employee(ResultSet rs) throws SQLException {
        this.employeeId = rs.getInt("EMPLOYEE_ID");
        this.employeeRoleId = rs.getInt("EMPLOYEE_ROLE_ID");
        this.employeeEmail = rs.getString("EMPLOYEE_EMAIL");
        this.employeeName = rs.getString("EMPLOYEE_NAME");
        this.remote = rs.getBoolean("IS_REMOTE");
		this.readOnly = rs.getBoolean("IS_READ_ONLY");
    }
    @XmlTransient
    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }
    @XmlTransient
    public int getEmployeeRoleId() {
        return employeeRoleId;
    }
    @XmlElement
	public boolean isRemote() {
		return remote;
	}
	
	public void setRemote(boolean isRemote) {
		this.remote = isRemote;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	@XmlElement
	public boolean isReadOnly() {
		return readOnly;
	}
    public void setEmployeeRoleId(int employeeRoleId) {
        this.employeeRoleId = employeeRoleId;
    }
    @XmlElement
    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }
    @XmlElement
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
    @XmlElement
    public String getEmployeePassword() {
        return employeePassword;
    }

    public void setEmployeePassword(String employeePassword) {
        this.employeePassword = employeePassword;
    }
    @XmlElement
    public String getEmployeePasswordAgain() {
        return employeePasswordAgain;
    }

    public void setEmployeePasswordAgain(String employeePasswordAgain) {
        this.employeePasswordAgain = employeePasswordAgain;
    }


    @Override
    public String toString() {
        return "Employee{" +
                " employeeRole='" + employeeRoleId + '\'' +
                ", employeePasswordAgain='***" + '\'' +
                ", employeePassword='***"  + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", employeeEmail='" + employeeEmail + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", remote=" + (isRemote()?isReadOnly()?"'readOnly'":"'true'":"'false'") + 
                '}';
    }

    static SQLDescriptor descriptor = new EmployeeSQLDescriptor();

     private static class EmployeeSQLDescriptor implements SQLDescriptor {
        public String composeSQL(Object o, int queryType){
            Employee v = (Employee)o;
            switch(queryType){
                case SQLDescriptor.SQL_SELECT :
                	return "SELECT EMPLOYEE_ID,EMPLOYEE_ROLE_ID,EMPLOYEE_EMAIL,EMPLOYEE_NAME,IS_REMOTE,IS_READ_ONLY\n" +
                    		"FROM EMPLOYEES ";
                case SQLDescriptor.SQL_GET_CURRVAL :
                    return null;//"SELECT @@IDENTITY";
                default :
                    throw new IllegalArgumentException("Unexpected query type - "+queryType);
            }
        }

        public String composePrepareSQL(int queryType){

        	switch(queryType){
            case SQLDescriptor.SQL_INSERT  :
                return "INSERT INTO EMPLOYEES(EMPLOYEE_ROLE_ID,EMPLOYEE_EMAIL,EMPLOYEE_NAME,EMPLOYEE_PASSWORD,IS_REMOTE,IS_READ_ONLY)\n "
                     + "VALUES(               ?               ,?             ,?            ,? 				 ,?			,?)";
            case SQLDescriptor.SQL_UPDATE  :
                return "UPDATE EMPLOYEES SET EMPLOYEE_ROLE_ID = ? , EMPLOYEE_EMAIL = ?  , EMPLOYEE_NAME = ?, IS_REMOTE = ?, IS_READ_ONLY = ? WHERE EMPLOYEE_ID =  ? ";
            case SQLDescriptor.SQL_DELETE  :
                return "DELETE FROM EMPLOYEES WHERE EMPLOYEE_ID = ? ";
            case SQLDescriptor.SQL_GET_WHERE :
                return "WHERE EMPLOYEE_ID =  ? ";
            case SQLDescriptor.SQL_DUBLICATE :
            default :
                throw new IllegalArgumentException("Unexpected query type - "+queryType);
        }
        }

        public void setParameters(Object o, PreparedStatement st,int queryType)
            throws SQLException {
            Employee v = (Employee)o;
            if(queryType==SQL_INSERT){
                int i=1;
                st.setInt(i++, v.employeeRoleId);
                st.setString(i++, v.employeeEmail);
                st.setString(i++, v.employeeName);
                try {
                   v.employeePassword = MessageDigestHelper.toDigest(v.employeePassword);
               } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                st.setString(i++, v.employeePassword);
                st.setBoolean(i++, v.remote);
                st.setBoolean(i++, v.readOnly);
           } else if(queryType==SQL_UPDATE){
                int u=1;
                st.setInt(u++, v.employeeRoleId);
                st.setString(u++, v.employeeEmail);
                st.setString(u++, v.employeeName);
                st.setBoolean(u++, v.remote);
                st.setBoolean(u++, v.readOnly);
                st.setInt(u++, v.employeeId);
           } else {
               throw new IllegalArgumentException("Unexpected query type - "+queryType);
            }
         }

         public Object newInstance(ResultSet rs) throws SQLException {
            return new Employee(rs);
        }
     };
}
