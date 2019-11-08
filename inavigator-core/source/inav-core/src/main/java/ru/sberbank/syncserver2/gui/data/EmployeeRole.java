package ru.sberbank.syncserver2.gui.data;



import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeRole {
    private int roleId;
    private String roleName;

    public static final int ADMIN    = 1;
    public static final int OPERATOR = 2;
    public static final int OPERATOR_MIS_ACCESS = 3;

    public EmployeeRole() {
        this.roleId = 0;
        this.roleName= "";
    }


    public EmployeeRole(ResultSet rs) throws SQLException {
        this.roleId = rs.getInt("EMPLOYEE_ROLE_ID");
        this.roleName = rs.getString("EMPLOYEE_ROLE_NAME");
    }


     public int getRoleId() {
		return roleId;
	}


	public String getRoleName() {
		return roleName;
	}




	static SQLDescriptor descriptor = new SyncConfigSQLDescriptor();

     private static class SyncConfigSQLDescriptor implements SQLDescriptor {
        public String composeSQL(Object o, int queryType){
            EmployeeRole v = (EmployeeRole)o;
            switch(queryType){
                case SQLDescriptor.SQL_SELECT :
                    return "SELECT EMPLOYEE_ROLE_ID,EMPLOYEE_ROLE_NAME FROM EMPLOYEE_ROLES";
                case SQLDescriptor.SQL_GET_CURRVAL :
                    return "";
                default :
                    throw new IllegalArgumentException("Unexpected query type - "+queryType);
            }
        }

        public String composePrepareSQL(int queryType){

            switch(queryType){
                case SQLDescriptor.SQL_INSERT  :
                    return "";
                case SQLDescriptor.SQL_UPDATE  :
                    return "";
                case SQLDescriptor.SQL_DELETE  :
                    return "";
                case SQLDescriptor.SQL_GET_WHERE :
                    return "";
                case SQLDescriptor.SQL_DUBLICATE :
                default :
                    throw new IllegalArgumentException("Unexpected query type - "+queryType);
            }
        }

        public void setParameters(Object o, PreparedStatement st,int queryType)
            throws SQLException {
            throw new IllegalArgumentException("Unexpected query type - "+queryType);
         }

         public Object newInstance(ResultSet rs) throws SQLException {
            return new EmployeeRole(rs);
        }
     };
}
