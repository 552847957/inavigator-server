package ru.sberbank.syncserver2.gui.data;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SyncConfig {
    private String propertyKey;
    private String propertyValue;
    private String propertyDesc;
    private String propertyGroup;
    private String[] services;
    public static final String DEFAULT_PROPERTY_GROUP = "Общие настройки"; 

    public SyncConfig() {
        this.propertyKey = "";
        this.propertyValue = "";
        this.propertyDesc = "";
        this.services = new String[0];
    }


    public SyncConfig(ResultSet rs) throws SQLException {
        this.propertyKey = rs.getString("PROPERTY_KEY");
        this.propertyValue = rs.getString("PROPERTY_VALUE");
        this.propertyDesc = rs.getString("PROPERTY_DESC");
        try {
        	this.propertyGroup = rs.getString("PROPERTY_GROUP");
        } catch(SQLException e) {}
        String services = rs.getString("services"); 
        this.services = services == null ? new String[0] : services.split(",");
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyDesc() {
        return propertyDesc;
    }

    public void setPropertyDesc(String propertyDesc) {
        this.propertyDesc = propertyDesc;
    }
    
    public String getPropertyGroup() {
		return propertyGroup == null ? DEFAULT_PROPERTY_GROUP : propertyGroup;
	}
    
    public void setPropertyGroup(String propertyGroup) {
		this.propertyGroup = propertyGroup;
	}    

    public String[] getServices() {
		return services;
	}

	public void setServices(String[] services) {
		this.services = services;
	}


	@Override
    public String toString() {
        return "SyncConfig{" +
                "propertyKey='" + propertyKey + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                '}';
    }

    static SQLDescriptor descriptor = new SyncConfigSQLDescriptor();

     private static class SyncConfigSQLDescriptor implements SQLDescriptor {
        public String composeSQL(Object o, int queryType){
            SyncConfig v = (SyncConfig)o;
            switch(queryType){
                case SQLDescriptor.SQL_SELECT :
                    return "select sc.*, SUBSTRING((select distinct ','+ pl.LIST_CODE from PROPERTY_LISTS pl inner join PROPERTY_VALUES pv on  pv.LIST_ID=pl.LIST_ID where pv.VALUE like '%@'+sc.PROPERTY_KEY+'@%' for xml path('')), 2, 1000) services from SYNC_CONFIG sc";
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
                    return "UPDATE SYNC_CONFIG SET PROPERTY_VALUE = ?  WHERE PROPERTY_KEY =  ? ";
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
            SyncConfig v = (SyncConfig)o;
            if(queryType==SQL_UPDATE){
                 int u=1;
                 st.setString(u++, v.propertyValue);
                 st.setString(u++, v.propertyKey);
            } else {
                throw new IllegalArgumentException("Unexpected query type - "+queryType);
             }
         }

         public Object newInstance(ResultSet rs) throws SQLException {
            return new SyncConfig(rs);
        }
     };
}
