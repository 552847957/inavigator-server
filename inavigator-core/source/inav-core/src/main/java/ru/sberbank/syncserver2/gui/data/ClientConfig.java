package ru.sberbank.syncserver2.gui.data;



import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class allows to read records for client properties as Sync Config records and reuse code
 */
public class ClientConfig extends SyncConfig{

    public ClientConfig(ResultSet rs) throws SQLException {
        String propertyKey   = rs.getString("PROPERTY");
        super.setPropertyKey(propertyKey);
        String propertyValue = rs.getString("VALUE");
        super.setPropertyValue(propertyValue);
    }

    static SQLDescriptor descriptor = new ClientConfigSQLDescriptor();

    private static class ClientConfigSQLDescriptor implements SQLDescriptor {
        public String composeSQL(Object o, int queryType){
            throw new IllegalArgumentException("Unexpected query type - "+queryType);
        }

        public String composePrepareSQL(int queryType){
            throw new IllegalArgumentException("Unexpected query type - "+queryType);
        }

        public void setParameters(Object o, PreparedStatement st,int queryType) throws SQLException {
            throw new IllegalArgumentException("Unexpected query type - "+queryType);
         }

         public Object newInstance(ResultSet rs) throws SQLException {
            return new ClientConfig(rs);
        }
     };
}
