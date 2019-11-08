package ru.sberbank.syncserver2.gui.util;


import oracle.sql.BLOB;
import oracle.sql.CLOB;

import java.io.*;
import java.sql.*;

public class SQLHelper {
    public static void closeResultSet(ResultSet rs){
        if(rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static void closeStatement(Statement st){
        if(st!=null){
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static void cancelStatement(Statement st){
        if(st!=null){
            try {
                st.cancel();
            } catch (SQLException e) {
            }
        }
    }

    public static void closeConnection(Connection conn){//3 - services, 2 - psu
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static void closeConnectionWithCommit(Connection conn) {//21 manager, 2 - psu
        if(conn!=null){
            try {
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static void closeSecondConnection(Connection connection, Connection localConnection) { // 1 - custom, 14 - services
        if (localConnection != null && connection == null) {
            try {
            	localConnection.setAutoCommit(true);
                localConnection.close();
            } catch (SQLException e1) {
            }
        }
    }

    public static void closeSecondConnectionWithCommit(Connection connection, Connection localConnection, boolean doCommit) {
        if (localConnection != null && connection == null) {
            try {
                if (doCommit) localConnection.commit();
                localConnection.close();
            } catch (SQLException e1) {
            }
        }
    }


    public static void saveString(String sql, String value, Connection conn){
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(sql);
            st.setString(1, value);
            st.executeQuery();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            SQLHelper.closeStatement(st);
        }
    }

    public static void saveClob(String sql, String value, Connection conn) throws SQLException {
        //1. Getting resultset and clob
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        rs.next();
        Clob clob = rs.getClob(1);

        //2. Writing clob
        try {
            Writer writer = ((CLOB)clob).setCharacterStream(0);
            writer.write(value);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static String loadClob(ResultSet rs, String columnName) throws SQLException {
        Clob clob = rs.getClob(columnName);
        try {
            Reader reader = ((CLOB)clob).getCharacterStream();
            StringBuffer sb = new StringBuffer();
            int length = (int) clob.length();
            char[] buffer = new char[length];
            int count;
            while ((count = reader.read(buffer)) != -1){
                sb.append(buffer);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";
    }

    public static void saveBlob(String sql, byte[] data, Connection conn) throws SQLException {
        //1. Getting resultset and clob
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        rs.next();
        Blob blob = rs.getBlob(1);

        //2. Writing clob
        OutputStream writer = null;
        try {
            writer = ((BLOB)blob).setBinaryStream(0);
            writer.write(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static byte[] loadBlob(ResultSet rs, String columnName) throws SQLException {
        //System.out.println("LOADING BLOB");
        Blob blob = rs.getBlob(columnName);
        InputStream reader = null;
        try {
            reader = ((BLOB)blob).getBinaryStream(0);
            int length = (int) blob.length();
            byte[] buffer = new byte[length];
            int count = reader.read(buffer);
            //System.out.println("READ COUNT = "+count+" OF "+length);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return null;
    }

    public static byte[] loadBlob(ResultSet rs) throws SQLException {
        //System.out.println("LOADING BLOB");
        Blob blob = rs.getBlob(1);
        InputStream reader = null;
        try {
            reader = ((BLOB)blob).getBinaryStream(0);
            int length = (int) blob.length();
            byte[] buffer = new byte[length];
            int count = reader.read(buffer);
            //System.out.println("READ COUNT = "+count+" OF "+length);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return null;
    }

    public static boolean containsColumn(ResultSetMetaData metaData, String columnName) throws SQLException {
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String name = metaData.getColumnName(i);
            if(name!=null && name.equals(columnName)){
                return true;
            }
        }
        return false;
    }

    public static java.sql.Date toDate(java.util.Date date){
        return date==null ? null:new java.sql.Date(date.getTime());
    }

    public static String escapeSingleQuotes(String s){
        StringBuffer sb = new StringBuffer(s.length()*2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c);
            if(c=='\''){
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static java.sql.Date toSQLDate(java.util.Date date){
        return date==null ? null:new java.sql.Date(date.getTime());
    }
}
