package ru.sberbank.syncserver2.gui.db;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import ru.sberbank.syncserver2.gui.data.AuthContextHolder;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.gui.data.SQLDescriptor;
import ru.sberbank.syncserver2.gui.data.SQLDescriptorRepository;
import ru.sberbank.syncserver2.gui.util.SQLHelper;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DatabaseServices implements ApplicationContextAware {
    public static String PERM_SHOW  = "'W','R','E','O','C','X'";
    public static String PERM_WRITE = "'W','O','X'";
    public static String PERM_EXEC  = "'E','O','C','X'";

	private static final Logger log = Logger.getLogger(DatabaseServices.class);
    public static final String TOMCAT_JNDI_PREFIX="java:comp/env/";

	private String jndiName;
	private javax.sql.DataSource dataSource;

	//private Set configuredConnections = Collections.synchronizedSet(new HashSet());
    protected ApplicationContext applicationContext;

    public DatabaseServices() {
    	//System.out.println("log");
    }

    public DatabaseServices(String jndiName) {
        this.jndiName = jndiName;
        init();
    }

    public DatabaseServices(javax.sql.DataSource dataSource) {
		/*        com.microsoft.jdbcx.sqlserver.SQLServerDataSource dataSource1 = new com.microsoft.jdbcx.sqlserver.SQLServerDataSource();
		        dataSource1.setServerName("localhost");
		        dataSource1.setDatabaseName("tmp_billing");
		        dataSource1.setUser("sa");
		        dataSource1.setPassword("ujikdesl");
		        dataSource1.setPortNumber(1433);
		        this.dataSource = dataSource1;*/
		this.dataSource = dataSource;
    }

    public void init() {
        log.info("Initializing");
        try {
            Context ctx = new InitialContext();
            log.info("Context: " + ctx.getNameInNamespace());
            /*if (ctx == null) {
                throw new RuntimeException("Server misconfiguration - Context");
            }*/
            DataSource ds = (DataSource) ctx.lookup(jndiName);
            log.info("JNDI name:  " + jndiName);
            log.info("DataSource: " + ds.getClass().getSimpleName());
            this.dataSource = ds;
        } catch (Exception e) {
            log.error(".DatabaseServices - Database connection failure\n"+"JNDI name:"+jndiName, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * lookup JNDI datasource as is and if error try to add tomcat prefix
     * @return
     */
    public static DataSource lookup(String aJNDIName) throws NamingException {
        Context ctx = new InitialContext();
        try{
            return (DataSource) ctx.lookup(aJNDIName);
        }catch (NamingException ex){
            if (aJNDIName.startsWith(DatabaseServices.TOMCAT_JNDI_PREFIX)){
                throw ex;
            }
            return (DataSource) ctx.lookup(DatabaseServices.TOMCAT_JNDI_PREFIX+aJNDIName);
        }
    }

    protected  List list(Class c, String where, Connection connection, PagingContext context) {
		return list(c, null, where, connection, context);
	}

	protected List list(Class c, String select, String where, Connection connection, PagingContext context) {
		return list(c, null, select, where, connection, context);
	}


	protected List list(Class c, SQLDescriptor descriptor, String select, String where, Connection connection,
		PagingContext context) {
		//1. Composing basic sql
		//1.1. Run without paging context if necessary
		if (context == null)
			return list(c, select, where, connection);

		//1.2. Compose basic sql
		if (descriptor == null) {
			descriptor = SQLDescriptorRepository.getSQLDescriptor(c);
		}
		String sql = select == null ? descriptor.composeSQL(null, SQLDescriptor.SQL_SELECT) : select;
		if (where != null) {
			sql += " " + where;
		}

		//2. Prepare and execute query
		Connection localConnection = null;
		Statement st = null;
		List results = new ArrayList();
        String countSQL =null, pagingSQL = null;
        boolean passedCount = false;
        try {
            localConnection = connection != null ? connection : getConnection();
			if (!context.isClient()) {
				//2.1. calculting count
				st = localConnection.createStatement();
				countSQL = context.composeCountSQL(sql);
				if (log.isEnabledFor(Level.ALL)) {
					log.log(Level.ALL, "list(" + c.getSimpleName() + ") - countSQL: " + countSQL);
				}
				ResultSet rs = st.executeQuery(countSQL);
				int count = rs != null && rs.next() ? rs.getInt(1) : 0;
				context.setRecordCount(count);
                passedCount = true;

                //2.2. Composing and executing main sql
				pagingSQL = context.composePageSQL(sql);
				if (log.isEnabledFor(Level.ALL)) {
					log.log(Level.ALL, "list(" + c.getSimpleName() + ") - pagingSQL: " + pagingSQL);
				}
				rs = st.executeQuery(pagingSQL);
				while (rs.next()) {
					Object instance = descriptor.newInstance(rs);
					results.add(instance);
				}
				st.close();
				localConnection.commit();
			} else {
				//2.3. Prepare sql and statement
                passedCount = true;
                st = localConnection.createStatement();
				pagingSQL = context.composePageSQL(sql);

				//2.4. Fetching
				int recordIndex = 0;
				if (log.isEnabledFor(Level.ALL)) {
					log.log(Level.ALL, "list(" + c.getSimpleName() + ") - pagingSQL: " + pagingSQL);
				}
				ResultSet rs = st.executeQuery(pagingSQL);
				while (rs.next()) {
					if (context.processRecordAtClient(rs, recordIndex++)) { //returns true if record is visible
						Object instance = descriptor.newInstance(rs);
						results.add(instance);
					}
				}
				context.setRecordCount(recordIndex);
				st.close();
				localConnection.commit();
			}
		} catch (SQLException e) {
            if(!passedCount){
                System.out.println("FAILED SQL: "+sql);
                System.out.println("FAILED COUNT SQL: "+countSQL);
            } else {
                System.out.println("FAILED SQL: "+sql);
                System.out.println("FAILED PAGING SQL: "+pagingSQL);
            }
            log.error("", e);
		} finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
		return results;
	}

	protected <T> List<T> list(Class<T> c, String where, Connection connection) {
		return list(c, null, where, connection);
	}

	protected <T> List<T> list(Class<T> c, String select, String where, Connection connection) {
		//1. Getting sql
		SQLDescriptor<T> descriptor = SQLDescriptorRepository.getSQLDescriptor(c);
		String sql = select == null ? descriptor.composeSQL(null, SQLDescriptor.SQL_SELECT) : select;
		if (where != null) {
			sql += " " + where;
		}
        log.debug(sql);

        //2. Prepare and execute query
		Connection localConnection = null;
		Statement st = null;
		List<T> results = new ArrayList<T>();
		try {
			localConnection = connection != null ? connection : getConnection();
			st = localConnection.createStatement();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "list(" + c.getSimpleName() + ") - sql: " + sql);
			}
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
                try {
                    T instance = descriptor.newInstance(rs);
                    results.add(instance);
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
			}
			localConnection.commit();
		} catch (SQLException e) {
			log.error("", e);
            e.printStackTrace();
            System.out.println("FAILED SQL : " + sql);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
		return results;
	}

	protected List listWithoutCommit(Class c, String select, String where, Connection connection) {
        //1. Getting sql
        SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(c);
        String sql = select == null ? descriptor.composeSQL(null, SQLDescriptor.SQL_SELECT) : select;
        if (where != null) {
            sql += " " + where;
        }
        log.debug(sql);

        //2. Prepare and execute query
        Connection localConnection = null;
        Statement st = null;
        List results = new ArrayList();
        try {
            localConnection = connection != null ? connection : getConnection();
            st = localConnection.createStatement();
            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "list(" + c.getSimpleName() + ") - sql: " + sql);
            }
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Object instance = descriptor.newInstance(rs);
                results.add(instance);
            }
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
        return results;
    }

    /**
     * Constructs map of objects specified by provided class.<br />
     * Note, it's supposed map key is first column values from result set,
     *
     * @param clazz
     * @param connection
     * @return
     */
    protected  <T> Map<Object, T> getMap(Class<T> clazz, Connection connection) {

        Map<Object, T> results = new HashMap<Object, T>();

        //1. Getting sql
        SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(clazz);
        String query = descriptor.composeSQL(null, SQLDescriptor.SQL_SELECT);
        log.debug("Running query: " + query);

        //2. Prepare and execute query
        Connection localConnection = null;
        Statement st = null;
        try {
            localConnection = connection != null ? connection : getConnection();
            st = localConnection.createStatement();
            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "list(" + clazz.getSimpleName() + ") - sql: " + query);
            }
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Object key = rs.getObject(1);
                results.put(key, (T) descriptor.newInstance(rs));
            }
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
        return results;
    }

    protected List<List<Object>> getObjects(String query) {
        List<List<Object>> result = new ArrayList<List<Object>>();

        Connection conn = null;
        Statement st = null;
        try {
            conn = getConnection();
            st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                List<Object> row = new ArrayList<Object>();
                result.add(row);
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
            }
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeConnection(conn);
        }

        return result;
    }

    private void closeSecondConnection(Connection connection, Connection localConnection) {
        if (localConnection != null && connection == null) {
            try {
                localConnection.close();
            } catch (SQLException e1) {
            }
        }
    }

   protected <T> List<T> list(Class<T> c, String where) {
		return list(c, where, null);
	}

   protected <T> List<T> list(Class<T> c) {
		return list(c, null, null);
	}

	protected <T> T get(Class<T> c, int[] parameters, Connection connection, String customSelect, String customWhere) {
		//1. Getting sql
		SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(c);
		String selectSql = customSelect != null ? customSelect : descriptor.composeSQL(null, SQLDescriptor.SQL_SELECT);
		String whereSql = customWhere != null ? customWhere : descriptor.composePrepareSQL(SQLDescriptor.SQL_GET_WHERE);

		//2. Prepare and execute query
		Connection localConnection = null;
		PreparedStatement st = null;
		String sql = selectSql + " " + whereSql;
		try {
			localConnection = connection != null ? connection : getConnection();
            if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "get(" + c.getSimpleName() + ") - sql: " + sql + "; parameters: " + Arrays.toString(parameters));
			}
			st = localConnection.prepareStatement(sql);
			for (int i = 0; i < parameters.length; i++) {
				st.setInt(i + 1, parameters[i]);
			}
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				return (T) descriptor.newInstance(rs);
			}
			localConnection.commit();
		} catch (SQLException e) {
			log.error("", e);
            log.error("FAILED STATEMENT: " + sql);
		} finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
		return null;
	}

    public <T> T get(Class<T> c, Object[] parameters) {
        // 1. Getting sql
        SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(c);
        String selectSql = descriptor.composeSQL(null, SQLDescriptor.SQL_SELECT);
        String whereSql = descriptor.composePrepareSQL(SQLDescriptor.SQL_GET_WHERE);

        // 2. Prepare and execute query
        Connection connection = null;
        PreparedStatement st = null;
        String sql = selectSql + " " + whereSql;
        try {
            connection = getConnection();
            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "get(" + c.getSimpleName() + ") - sql: " + sql + "; parameters: " + Arrays.toString(parameters));
            }
            st = connection.prepareStatement(sql);
            for (int i = 0; i < parameters.length; i++) {
                st.setObject(i + 1, parameters[i]);
            }
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return (T) descriptor.newInstance(rs);
            }
            connection.commit();
        } catch (SQLException e) {
            log.error("", e);
            log.error("FAILED STATEMENT: " + sql);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeConnection(connection);
        }
        return null;
    }

    protected Object get(Class c, int[] identifiers, Connection connection) {
		return get(c, identifiers, connection, null, null);
	}

	protected <T> T get(Class<T> c, String customWhere, Connection connection) {
		return get(c, new int[0], connection, null, customWhere);
	}

	protected <T> T get(Class<T> c, String customSelect, String customWhere, Connection connection) {
		return get(c, new int[0], connection, customSelect, customWhere);
	}

	protected <T> T get(Class<T> c, int[] identifiers) {
		return (T) get(c, identifiers, null);
	}

	protected void insert(Class c, List objects, Connection connection) {
		//1. Getting sql
		SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(c);
		String insertSql = descriptor.composePrepareSQL(SQLDescriptor.SQL_INSERT);

		//2. Prepare and execute query
		Connection localConnection = null;
		PreparedStatement st = null;
		try {
			localConnection = connection != null ? connection : getConnection();
			if (log.isEnabledFor(Level.ALL)) {
				log
					.log(Level.ALL, "insert(" + c.getSimpleName() + ") - sql: " + insertSql + "; objects: " + objects);
			}
			st = localConnection.prepareStatement(insertSql);
			for (int i = 0; i < objects.size(); i++) {
				Object o = objects.get(i);
				descriptor.setParameters(o, st, SQLDescriptor.SQL_INSERT);
				st.execute();
			}
			localConnection.commit();
		} catch (SQLException e) {
			log.error("", e);
		} finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
	}


	/**
	 * @param o
	 * @param connection
	 * @return -1 if failed to insert or associated sequence id (new object id)
	 */
	protected int insert(Object o, Connection connection) {
		//1. Getting sql
		SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(o.getClass());
		String insertSql = descriptor.composePrepareSQL(SQLDescriptor.SQL_INSERT);

		//2. Prepare and execute query
		Connection localConnection = null;
		PreparedStatement st = null;
		Statement st2 = null;
		try {
			localConnection = connection != null ? connection : getConnection();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "insert(" + o.getClass().getSimpleName() + ") - sql: " + insertSql + "; object: "
					+ o);
			}
            st = localConnection.prepareStatement(insertSql);
			descriptor.setParameters(o, st, SQLDescriptor.SQL_INSERT);
			st.execute();


			String getCurValSql = null;
            ResultSet identityRS = null;
            try{
                getCurValSql = descriptor.composeSQL(o, SQLDescriptor.SQL_GET_CURRVAL);
            }catch (IllegalArgumentException ex){
                log.warn("SQL_GET_CURRVAL has not been defined, audit log will not be written");
            }
			if (getCurValSql == null || getCurValSql.length() == 0) {
				localConnection.commit();
				return -1;
			}
            st2 = localConnection.createStatement();
		    identityRS = st2.executeQuery(getCurValSql);
			if (identityRS != null && identityRS.next()) {
                int id = identityRS.getInt(1);
				localConnection.commit();
				return id;
			} else {
				localConnection.commit();
				return -1;
			}
		} catch (SQLException e) {
			log.error("", e);
            System.out.println("FAILED SQL: "+insertSql);
            e.printStackTrace();
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeStatement(st2);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
		return -1;
	}

	protected int insert(Object o) {
		return insert(o, null);
	}

	protected <T> int insert(Class<T> clazz, List<T> list) {
	    int insertedCount = 0;
	    if (list == null || list.isEmpty()) {
	        log.debug("Inserting batch: empty list of objects of type " + clazz.getSimpleName());
	        return insertedCount;
	    }

        SQLDescriptor<T> descriptor = SQLDescriptorRepository.getSQLDescriptor(clazz);
        String insertQuery = descriptor.composePrepareSQL(SQLDescriptor.SQL_INSERT);

        log.debug("Inserting batch: " + insertQuery);

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            log.debug("Inserting batch: " + list.size() + " object(s) of type " + clazz.getSimpleName());
            connection = getConnection();
            statement = connection.prepareStatement(insertQuery);
            for (T obj : list) {
                descriptor.setParameters(obj, statement, SQLDescriptor.SQL_INSERT);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
            insertedCount = list.size();
        } catch (SQLException e) {
            log.error("Can't insert records", e);
        } finally {
            SQLHelper.closeStatement(statement);
            SQLHelper.closeConnection(connection);
        }

        log.debug("Inserting batch: " + insertedCount + " of " + list.size() + " object(s) of type " + clazz.getSimpleName() + " has been inserted");
	    return insertedCount;
	}
	
	protected <T> int insert(Class<T> clazz, List<T> list, SQLDescriptor<T> descriptor) {
	    int insertedCount = 0;
	    if (list == null || list.isEmpty()) {
	        log.debug("Inserting batch: empty list of objects of type " + clazz.getSimpleName());
	        return insertedCount;
	    }
	    if (descriptor == null ) {	        
	        return insertedCount;
	    }
        String insertQuery = descriptor.composePrepareSQL(SQLDescriptor.SQL_INSERT);

        log.debug("Inserting batch: " + insertQuery);

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            log.debug("Inserting batch: " + list.size() + " object(s) of type " + clazz.getSimpleName());
            connection = getConnection();
            statement = connection.prepareStatement(insertQuery);
            for (T obj : list) {
                descriptor.setParameters(obj, statement, SQLDescriptor.SQL_INSERT);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
            insertedCount = list.size();
        } catch (SQLException e) {
            log.error("Can't insert records", e);
        } finally {
            SQLHelper.closeStatement(statement);
            SQLHelper.closeConnection(connection);
        }

        log.debug("Inserting batch: " + insertedCount + " of " + list.size() + " object(s) of type " + clazz.getSimpleName() + " has been inserted");
	    return insertedCount;
	}

	/**
	 * @param o -object
	 * @param connection
	 * @return -1 if failed to execute, 0 - if update has been executed
	 */
	protected int update(Object o, Connection connection) {
	    int result = -1;
		//1. Getting sql
        Class c = o.getClass();
        SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(c);
		String updateSql = descriptor.composePrepareSQL(SQLDescriptor.SQL_UPDATE);

		//2. Prepare and execute query
		Connection localConnection = null;
		PreparedStatement st = null;
		try {
			localConnection = connection != null ? connection : getConnection();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "update(" + o.getClass().getSimpleName() + ") - sql: " + updateSql + "; object: "
					+ o);
			}

			st = localConnection.prepareStatement(updateSql);
			descriptor.setParameters(o, st, SQLDescriptor.SQL_UPDATE);
			st.execute();

			localConnection.commit();
			result = 0;
		} catch (SQLException e) {
			log.error("", e);
            System.out.println("FAILED SQL: "+updateSql);
            System.out.println("FAILED OBJECT: "+o);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }

		return result;
	}

	protected int update(Object o) {
		return update(o, null);
	}

	protected int extraQuery(Object o, Object... changing) {
		//1. Getting sql
		SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(o.getClass());
		String updateSql = descriptor.composePrepareSQL(SQLDescriptor.SQL_EXTRA_QUERY);

		//2. Prepare and execute query
		Connection localConnection = null;
		PreparedStatement st = null;
		try {
			localConnection = getConnection();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "extraQuery(" + o.getClass().getSimpleName() + ") - sql: " + updateSql
					+ "; object: " + o);
			}
			st = localConnection.prepareStatement(updateSql);
			descriptor.setParameters(o, st, SQLDescriptor.SQL_EXTRA_QUERY);
			int updateCount = st.executeUpdate();
			st.close();
			localConnection.commit();
			return updateCount;
		} catch (SQLException e) {
			log.error("", e);
		} finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeConnection(localConnection);
		}
		return 0;
	}

	protected void delete(Class c, int[] identifiers, Connection connection) {
		//1. Getting sql
		SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(c);
		String deleteSql = descriptor.composePrepareSQL(SQLDescriptor.SQL_DELETE);

		//2. Prepare and execute query
		Connection localConnection = null;
		PreparedStatement st = null;
		try {
			localConnection = connection != null ? connection : getConnection();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "delete(" + c.getSimpleName() + ") - sql: " + deleteSql + "; intIdentifiers: "
					+ Arrays.toString(identifiers));
			}
			st = localConnection.prepareStatement(deleteSql);
			for (int i = 0; i < identifiers.length; i++) {
				st.setInt(i + 1, identifiers[i]);
			}
			st.execute();
			st.close();
			localConnection.commit();
		} catch (SQLException e) {
			log.error("", e);
		} finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
	}

	protected void delete(Class c, String[] identifiers, Connection connection) {
		//1. Getting sql
		SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(c);
		String deleteSql = descriptor.composePrepareSQL(SQLDescriptor.SQL_DELETE);

		//2. Prepare and execute query
		Connection localConnection = null;
		PreparedStatement st = null;
		try {
            localConnection = connection != null ? connection : getConnection();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "delete(" + c.getSimpleName() + ") - sql: " + deleteSql + "; stringIdentifiers: "
					+ Arrays.toString(identifiers));
			}
			st = localConnection.prepareStatement(deleteSql);
			for (int i = 0; i < identifiers.length; i++) {
				st.setString(i + 1, identifiers[i]);
			}
			st.execute();
			st.close();
			localConnection.commit();
		} catch (SQLException e) {
			log.error("", e);
		} finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
	}

	protected void delete(Class c, Object[] identifiers, Connection connection) {
        //1. Getting sql
        SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(c);
        String deleteSql = descriptor.composePrepareSQL(SQLDescriptor.SQL_DELETE);

        //2. Prepare and execute query
        Connection localConnection = null;
        PreparedStatement st = null;
        try {
            localConnection = connection != null ? connection : getConnection();
            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "delete(" + c.getSimpleName() + ") - sql: " + deleteSql + "; stringIdentifiers: "
                        + Arrays.toString(identifiers));
            }
            st = localConnection.prepareStatement(deleteSql);
            for (int i = 0; i < identifiers.length; i++) {
                st.setObject(i + 1, identifiers[i]);
            }
            st.execute();
            st.close();
            localConnection.commit();
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
    }

    protected void delete(Class c, int[] identifiers) {
		delete(c, identifiers, null);
	}

    protected void delete(Class c, String[] identifiers) {
		delete(c, identifiers, null);
	}

    protected void delete(Class c, Object[] identifiers) {
        delete(c, identifiers, null);
    }

    boolean existsDublicate(Object o, Connection connection) {
		//1. Getting sql
		SQLDescriptor descriptor = SQLDescriptorRepository.getSQLDescriptor(o.getClass());
		String dublicateSql = descriptor.composePrepareSQL(SQLDescriptor.SQL_DUBLICATE);

		//2. Prepare and execute query
		Connection localConnection = null;
		PreparedStatement st = null;
		try {
			localConnection = connection != null ? connection : getConnection();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "existsDublicate(" + o.getClass().getSimpleName() + ") - sql: " + dublicateSql
					+ "; object: " + o);
			}
			st = localConnection.prepareStatement(dublicateSql);
			descriptor.setParameters(o, st, SQLDescriptor.SQL_DUBLICATE);
			ResultSet rs = st.executeQuery();
			boolean result = rs.next() && rs.getInt(1) > 0;
			st.close();
			return result;
		} catch (SQLException e) {
			log.error("", e);
		} finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
		return true;
	}

	boolean existsDublicate(Object o) {
		return existsDublicate(o, null);
	}

	public boolean exists(String fromAndWhere) {
		return exists(fromAndWhere, null);
	}

	boolean exists(String fromAndWhere, Connection connection) {
		Connection localConnection = null;
		Statement st = null;
		try {
			localConnection = connection != null ? connection : getConnection();
			st = localConnection.createStatement();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "exists - sql: SELECT COUNT(*) " + fromAndWhere);
			}
			ResultSet rs = st.executeQuery("SELECT COUNT(*) " + fromAndWhere);
			boolean result = rs.next() && rs.getInt(1) > 0;
			st.close();
			return result;
		} catch (SQLException e) {
			log.error("", e);
		} finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
		return true;
	}

	public void execute(String sql, Object... changing) {
		execute(sql, null, changing);
	}

	void execute(String sql, Connection connection, Object... changing ) {
		Connection localConnection = null;
		Statement st = null;
		try {
			localConnection = connection != null ? connection : getConnection();
			st = localConnection.createStatement();


            if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "execute - sql: " + sql);
			}
			st.execute(sql);
			st.close();
			localConnection.commit();
		} catch (SQLException e) {
            e.printStackTrace();
            log.error("", e);
            System.out.println("FAILED SQL: "+sql);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
	}

	public void executeBatch(String query, List<?> parameters, Connection connection, Object... changing) {
        Connection localConnection = null;
        PreparedStatement st = null;
        try {
            localConnection = connection != null ? connection : getConnection();

            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "execute - sql: " + query + "; parameters: " + parameters);
            }

            st = localConnection.prepareStatement(query);

            if (parameters != null) {
                Iterator<?> iterator = parameters.iterator();
                for (int i = 0; iterator.hasNext(); i++) {
                    st.setObject(1, iterator.next());
                    st.addBatch();
                }
            }
            st.executeBatch();
            localConnection.commit();
        } catch (SQLException e) {
            log.error("", e);
            log.error("FAILED STATEMENT: " + query);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
    }

    public void executeWithoutCommit(String sql, Connection conn, Object... changing) {
		Statement st = null;
		try {
			st = conn.createStatement();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "executeWithoutCommit - sql: " + sql);
			}
			st.execute(sql);
			st.close();
		} catch (SQLException e) {
			log.error("", e);
		} finally {
            SQLHelper.closeStatement(st);
        }
	}

	public void executeQuery(String sql, Object callback, String method) {
		Connection localConnection = null;
		Statement statement = null;
		try {
			localConnection = getConnection();
			statement = localConnection.createStatement();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "executeQuery - sql: " + sql + "; callback: " + callback + "; mathod: " + method);
			}
			ResultSet rs = statement.executeQuery(sql);
			try {
				Class c = callback.getClass();
				Method m = c.getMethod(method, new Class[] { ResultSet.class });
				m.invoke(callback, new Object[] { rs });
			} catch (NoSuchMethodException e) {
				log.error("", e);
			} catch (IllegalAccessException e) {
				log.error("", e);
			} catch (InvocationTargetException e) {
				log.error("", e);
			}
		} catch (SQLException e) {
			log.error("", e);
		} finally {
            SQLHelper.closeStatement(statement);
            SQLHelper.closeConnection(localConnection);
		}
	}

	public Set<Integer> getIntegerSet(String sql) {
		LinkedHashSet<Integer> result = new LinkedHashSet<Integer>();
		Connection localConnection = null;
		Statement statement = null;
		try {
			localConnection = getConnection();
			statement = localConnection.createStatement();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "getIntegerSet - sql: " + sql);
			}
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				result.add(new Integer(rs.getInt(1)));
			}
			statement.close();
		} catch (SQLException e) {
			log.error("", e);
		} finally {
            closeStatementWithLog(statement);
            SQLHelper.closeConnection(localConnection);
		}
		return result;
	}
	
	public Object[] getMultipleResult(String sql, ResultSetExtractor<?>...extractors) {
		List<Object> result = new LinkedList<Object>();
		Connection localConnection = null;
		Statement statement = null;
		try {			
			localConnection = getConnection();
			statement = localConnection.createStatement();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "getMultipleResult - sql: " + sql);
			}		
			
			int i = 0;
			boolean success = statement.execute(sql);
			do {
				if (success) {
					ResultSet rs = statement.getResultSet();
					result.add(extractors[i++].extractData(rs));
					rs.close();				
				}
				success = statement.getMoreResults();
			} while (success);
			statement.close();
		} catch (SQLException e) {
			log.error("faild execut sql: "+sql, e);
		} finally {
            closeStatementWithLog(statement);
            SQLHelper.closeConnection(localConnection);
		}
		return result.toArray();
	}
	
    public Map getIntegerMap(String sql) {
        HashMap result = new HashMap();
        Connection localConnection = null;
        Statement statement = null;
        try {
            localConnection = getConnection();
            statement = localConnection.createStatement();
            log.debug (sql);
            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "getIntegerMap - sql: " + sql);
            }
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Integer key = new Integer(rs.getInt(1));
                Integer value = new Integer(rs.getInt(2));
                result.put(key, value) ;
            }
            statement.close();
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            closeStatementWithLog(statement);
            SQLHelper.closeConnection(localConnection);
        }
        return result;
    }

    public static <T> Map<?, T> getObjectMap(String sql, Connection connection) {
        Map<Integer,T> result = new HashMap<Integer, T>();
        Connection localConnection = null;
        Statement statement = null;
        try {
            localConnection = connection;
            statement = localConnection.createStatement();
            log.debug (sql);
            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "getObjectMap - sql: " + sql);
            }
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Integer key = new Integer(rs.getInt(1));
                T value = (T)rs.getObject(2);
                result.put(key, value) ;
            }
            statement.close();
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            closeStatementWithLog(statement);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
        return result;
    }

    public static <T> Map<?,List<T>> getListObjectMap(String sql, Connection connection) {
        Map<Integer, List<T>> result = new HashMap<Integer, List<T>>();
        Connection localConnection = null;
        Statement statement = null;
        try {
            localConnection = connection;
            statement = localConnection.createStatement();
            log.debug (sql);
            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "getListObjectMap - sql: " + sql);
            }
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Integer key = new Integer(rs.getInt(1));
                T value = (T)rs.getObject(2);
                List<T> listValues = result.get(key);
                if (listValues == null) {
                    listValues = new ArrayList<T>();
                    result.put(key, listValues);
                }
                listValues.add(value);
            }
            statement.close();
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            closeStatementWithLog(statement);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
        return result;
    }

    private static void closeStatementWithLog(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.error("", e);
            }
        }
    }

    public Set<String> getStringSet(String sql) {
		return getStringSet(sql, null);
	}

	public Set<String> getStringSet(String sql, Connection connection) {
		HashSet<String> result = new HashSet<String>();
		Statement statement = null;
		Connection localConnection = null;
		try {
            localConnection = connection != null ? connection : getConnection();
			statement = localConnection.createStatement();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "getStringSet - sql: " + sql);
			}
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			statement.close();
		} catch (SQLException e) {
			log.error("", e);
		} finally {
            closeStatementWithLog(statement);
            SQLHelper.closeSecondConnection(connection, localConnection);
		}
		return result;
	}

    public int getIntValue(String sql) {
        return getIntValue(sql, -1);
    }

    public int getIntValue(String sql, int defaultValue) {
        return getIntValue(sql, defaultValue, null);
    }

    public int getIntValue(String sql, int defaultValue, Connection connection) {
        Statement statement = null;
        Connection localConnection = null;
        try {
            localConnection = connection != null ? connection : getConnection();
            statement = localConnection.createStatement();
            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "statement - sql: " + sql);
            }
            log.debug(sql);
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                return rs.getInt(1);
            }
            statement.close();
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            closeStatementWithLog(statement);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
        return defaultValue;
    }

    public Date getDateValue(String sql) {
        return getDateValue(sql, null);
    }

    public Date getDateValue(String sql, Date defaultValue) {
        return getDateValue(sql, defaultValue, null);
    }

    public Date getDateValue(String sql, Date defaultValue, Connection connection) {
        Statement statement = null;
        Connection localConnection = null;
        try {
            localConnection = connection != null ? connection : getConnection();
            statement = localConnection.createStatement();
            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "statement - sql: " + sql);
            }
            log.debug(sql);
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                return rs.getTimestamp(1);
            }
            statement.close();
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            closeStatementWithLog(statement);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
        return defaultValue;
    }

    public String getStringValue(String sql) {
        List values = getStringList(sql, null);
        return (String) (!(values == null || values.size() == 0) ? values.get(0) : "");
    }

    public List<String> getStringList(String sql) {
		return getStringList(sql, null);
	}

	public List<String> getStringList(String sql, Connection connection) {
		List<String> result = new ArrayList<String>();
		Statement statement = null;
		Connection localConnection = null;
		try {
            localConnection = connection != null ? connection : getConnection();
			statement = localConnection.createStatement();
			if (log.isEnabledFor(Level.ALL)) {
				log.log(Level.ALL, "getStringList - sql: " + sql);
			}
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			statement.close();
		} catch (SQLException e) {
            System.out.println("FAILED SQL: "+sql);
            log.error("", e);
		} finally {
            closeStatementWithLog(statement);
            SQLHelper.closeSecondConnection(connection, localConnection);
		}
		return result;
	}

	public List<Date> getDateList(String sql) {
        List<Date> result = new ArrayList<Date>();
        Statement statement = null;
        Connection localConnection = null;
        try {
            localConnection = getConnection();
            statement = localConnection.createStatement();
            if (log.isEnabledFor(Level.ALL)) {
                log.log(Level.ALL, "getStringList - sql: " + sql);
            }
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                result.add(rs.getDate(1));
            }
            statement.close();
        } catch (SQLException e) {
            log.error("SQL execution failed", e);
        } finally {
            closeStatementWithLog(statement);
            SQLHelper.closeConnection(localConnection);
        }
        return result;
    }

	Connection getConnection() throws SQLException {
        //OracleConnection connection = (OracleConnection) getDataSourceConnection();
        Connection connection = getDataSourceConnection();
        connection.setAutoCommit(false);
        /*
        if (!configuredConnections.contains(connection)) {
			connection.setAutoCommit(false);
			//connection.setAutoClose(true);
			configuredConnections.add(connection);
		}
		*/
        return connection;
    }

    Connection getDataSourceConnection() throws SQLException {
        return dataSource.getConnection();
	}

    Connection getDriverManagerConnection() throws SQLException {
        org.springframework.jdbc.datasource.DriverManagerDataSource ds = (org.springframework.jdbc.datasource.DriverManagerDataSource) dataSource;
        String url = ds.getUrl();
        String password = ds.getPassword();
        String username = ds.getUsername();
        return dataSource.getConnection();
	}

    public DataSource getDataSource() {
        return dataSource;
    }

    @Autowired
    @Qualifier("configSource")
    public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

    public String composeInClause(Collection values, boolean quotes) {
        StringBuffer sb = new StringBuffer("IN (");
        for (Iterator iterator = values.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            String s = String.valueOf(o);
            if (quotes) {
                sb.append("'");
            }
            sb.append(s);
            if (quotes) {
                sb.append("'");
            }
            if (iterator.hasNext()) {
                sb.append(",");
            } else {
                sb.append(")");
            }
        }
        return sb.toString();
    }

    public String composeInClause(String[] values, boolean quotes) {
		StringBuffer sb = new StringBuffer("IN (");
		for (int i = 0; i < values.length; i++) {
			if (quotes) {
				sb.append("'");
			}
			sb.append(values[i]);
			if (quotes) {
				sb.append("'");
			}
			if (i + 1 < values.length) {
				sb.append(",");
			} else {
				sb.append(")");
			}
		}
		return sb.toString();
	}

    public String composeInClause(char[] values, boolean quotes) {
		StringBuffer sb = new StringBuffer("IN (");
		for (int i = 0; i < values.length; i++) {
			if (quotes) {
				sb.append("'");
			}
			sb.append(values[i]);
			if (quotes) {
				sb.append("'");
			}
			if (i + 1 < values.length) {
				sb.append(",");
			} else {
				sb.append(")");
			}
		}
		return sb.toString();
	}

    public String composeInClause(int[] values, boolean quotes) {
		StringBuffer sb = new StringBuffer("IN (");
		for (int i = 0; i < values.length; i++) {
			if (quotes) {
				sb.append("'");
			}
			sb.append(values[i]);
			if (quotes) {
				sb.append("'");
			}
			if (i + 1 < values.length) {
				sb.append(",");
			} else {
				sb.append(")");
			}
		}
		return sb.toString();
	}

	public List executeQuery(String query, Connection connection) throws SQLException {
	    connection = connection != null ? connection : getConnection();
		Statement st = null;
		List result = new ArrayList();
		st = connection.createStatement();
		if (log.isEnabledFor(Level.ALL)) {
			log.log(Level.ALL, "executeQuery - query: " + query);
		}
		ResultSet rs = st.executeQuery(query);
		if (rs != null) {
			ResultSetMetaData md = rs.getMetaData();
			if (md != null && md.getColumnCount() > 0) {
				int i, c = md.getColumnCount();
				List labels = new ArrayList(c);
				List types = new ArrayList(c);
				for (i = 1; i <= c; ++i) {
					labels.add(md.getColumnLabel(i));
					types.add(md.getColumnType(i));
				}
				result.add(labels);
				result.add(types);

				List row;
				while (rs.next()) {
					row = new ArrayList();
					for (i = 1; i <= c; ++i) {
						row.add(rs.getObject(i));
					}
					result.add(row);
				}
			}
		}
		return result;
	}

    public void executePattern(String sql, Object[] values, Connection connection, Object... changing) {
        Connection localConnection = null;
        PreparedStatement st = null;
        try {
            localConnection = connection != null ? connection : getConnection();
            st = localConnection.prepareStatement(sql);
            String act="manual";
            if (sql.startsWith("delete")) act="delete";
            if (sql.startsWith("insert")) act="insert";
            if (sql.startsWith("update")) act="update";

            for (int i = 0; i < values.length; i++) {
                if(values[i] instanceof String){
                    st.setString(i+1, (String) values[i]);
                } else if(values[i] instanceof Integer){
                    st.setInt(i+1, ((Integer) values[i]).intValue());
                } else if(values[i] instanceof Date){
                    st.setDate(i+1, new java.sql.Date(((Date)values[i]).getTime() ));
                } else if(values[i]==null){
                    st.setObject(i+1, null);
                }
            }
            st.execute();
            localConnection.commit();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.out.println("FAILED SQL: "+sql);
        } finally {
            closeStatementWithLog(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
    }

    public void executePatternUnicode(String sql, Object[] values, Connection connection) {
        Connection localConnection = null;
        PreparedStatement st = null;
        try {
            localConnection = connection != null ? connection : getConnection();
            st = localConnection.prepareStatement(sql);
            for (int i = 0; i < values.length; i++) {
                if(values[i] instanceof String){
                    st.setString(i+1, (String) values[i]);
                } else if(values[i] instanceof Integer){
                    st.setInt(i+1, ((Integer) values[i]).intValue());
                } else if(values[i] instanceof Date){
                    st.setString(i+1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(((Date)values[i]).getTime()));
                } else if(values[i] instanceof Double){
                    st.setDouble(i+1, ((Double)values[i]).doubleValue());
                } else if(values[i]==null){
                    st.setObject(i+1, null);
                };
            }
            st.execute();
            localConnection.commit();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.out.println("FAILED SQL: "+sql);
        } finally {
            closeStatementWithLog(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
    }

    public int executePattern(String sql, Object[] values, String idSQL, Connection connection) {
        //System.out.println("executePattern : sql = "+sql);
        //System.out.println("executePattern : values = "+Arrays.asList(values) );
        Connection localConnection = null;
        PreparedStatement  st = null;
        Statement st2 = null;
        try {
            //1. executing main statement
            localConnection = connection != null ? connection : getConnection();
            st = localConnection.prepareStatement(sql);
            for (int i = 0; i < values.length; i++) {
                if(values[i] instanceof String){
                    st.setString(i+1, (String) values[i]);
                } else if(values[i] instanceof Integer){
                    st.setInt(i+1, ((Integer) values[i]).intValue());
                } else if(values[i] instanceof Date){
                    st.setDate(i+1, new java.sql.Date(((Date)values[i]).getTime() ));
                } else if(values[i]==null){
                    st.setObject(i+1, null);
                };
            }
            st.execute();
            localConnection.commit();

            //2. Executing post statement
            st2 = localConnection.createStatement();
            ResultSet identityRS = st2.executeQuery(idSQL);
            if (identityRS != null && identityRS.next()) {
                localConnection.commit();
                return identityRS.getInt(1);
            } else {
                localConnection.commit();
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            closeStatementWithLog(st);
            closeStatementWithLog(st2);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
        return -1;
    }

    public byte[] loadBlob(String sql, Connection connection) {
        Connection localConnection = null;
        Statement st = null;
        try {
            localConnection = connection != null ? connection : getConnection();
            ResultSet rs = st.executeQuery(sql);
            if(rs.next()){
                return SQLHelper.loadBlob(rs);
            }
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnection(connection, localConnection);
        }
        return null;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

/*
    public <T, C> C loadChild(T parent, Class<C> childClass, String fieldName) {
        int childId = getFieldValue(parent, fieldName);

        return get(childClass, new int[]{childId});
    }

    private <T> T getFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }

        T result = null;
        try {
            Method method = obj.getClass().getMethod(capitalizeFirst(fieldName));
            result = (T) method.invoke(obj);
        } catch (SecurityException e) {
            LOGGER.error(e);
        } catch (NoSuchMethodException e) {
            LOGGER.error(e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e);
        } catch (InvocationTargetException e) {
            LOGGER.error(e);
        }

        return result;
    }

    public static String capitalizeFirst(String s){
        if (s == null || s.isEmpty()) {
            return null;
        }

        char first = s.charAt(0);
        first = Character.toUpperCase(first);

        return first + s.substring(1);
    }
*/

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected Employee getRequestEmployee(){
        ApplicationContext applicationContext1 = getApplicationContext();
        if (applicationContext1==null) return null;
        AuthContextHolder contextHolder = (AuthContextHolder) applicationContext1.getBean("authHolder");
        return contextHolder.getAuthContext().getEmployee();
    }

    /**
     * generate
     * @param permList 'W','R','E','O','C'
     * @param objectId
     * @return
     */
    protected String getObjectFilter(String permList, String objectId){
        String permIdSQL="select distinct p.object_id from v_employee_perm p\n" +
                "where p.object_type='TABLE' and p.PERM_TYPE in (%s) \n" +
                "      and p.employee_id=%d\n" +
                "      and upper(object_id_type) = upper('%s')";
        Employee empl = getRequestEmployee();
        if (empl!=null){
            String sql = String.format(permIdSQL,permList,empl.getEmployeeId(),objectId);
            return sql;
        }
        return "";
    }


    /**
     *
     * @param objectId      id of changed record
     * @param objectType    name of table where change record
//     * @param initiator     user name from UI that change record
     * @param user_comment  user comment about reason for change record
     * @param comment       comment about what object was changed
     * @param old_state     XML with old state of changed record
     * @param new_state     XML with new state of changed record
     */
    public void writeAuditLog( Connection connection,
                                 String objectId,
                                 String objectType,
//                                 String initiator,
                                 String user_comment,
                                 String comment,
                                 String old_state,
                                 String new_state){
        Integer auditId = null;
        Connection localConnection = null;
        CallableStatement st = null;
        boolean commit = false;
        try {
            localConnection = connection != null ? connection : getConnection();
            st = localConnection.prepareCall(
                     "{call p_audit.add_audit_record(?,?,?,?,?,?,?)}" );
//            st.registerOutParameter(8, Types.INTEGER);

            st.setString(1,objectId);
            st.setString(2,objectType);
            Employee employee = getRequestEmployee();
            st.setString(3,employee.getEmployeeId()+":"+employee.getEmployeeName());
            st.setString(4,user_comment);
            st.setString(5,comment);
            st.setString(6,old_state);
            st.setString(7,new_state);
            st.execute();
            commit=true;
        } catch (SQLException e) {
            log.error("", e);
        } finally {
            SQLHelper.closeStatement(st);
            SQLHelper.closeSecondConnectionWithCommit(connection, localConnection, commit);
        }
    }
}
