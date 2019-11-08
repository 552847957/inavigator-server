package ru.sberbank.syncserver2.service.generator.single;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import ru.sberbank.syncserver2.gui.util.SQLHelper;
import ru.sberbank.syncserver2.service.generator.DataSourceHelper;
import ru.sberbank.syncserver2.service.generator.single.data.*;
import ru.sberbank.syncserver2.service.generator.single.data.ETLCheck.CHECK_ERROR_TYPES;
import ru.sberbank.syncserver2.service.generator.single.data.ETLCheck.TARGETS;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 31.03.14.
 */
public class SQLiteGenerator extends AbstractGenerator {
    private static final Logger log = Logger.getLogger(SQLiteGenerator.class);
    private boolean manualMode = false;

    public SQLiteGenerator() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean generateFile(ETLAction action, File tempFile) throws GeneratorException{
        //1. Declaring and dropping old logs
        //1.1. Declaring
        Connection sqliteConn = null;
        Connection mssqlConn = null;
        final String dataFileName = action.getDataFileName();

        //1.2. Before beginning we drop all old logs
       /* String dropOldLogsSQL = "DELETE FROM SYNC_LOGS WHERE EVENT_INFO='"+dataFileName+"'";
        dbLogService.executeSQL(dropOldLogsSQL);*/

        //2. Start generation
        try {
            getDbNotificationLogger().addGenStaticFileEvent(dataFileName, ActionState.PHASE_DB_CONNECT, ActionState.STATUS_PERFORM);
            //2.1. Create folder if necessary
            FileHelper.createMissingFolders(tempFile.getParent());

            //2.2. Finding connection to right MSSQL database
            String jndi = action.getJndi();
            String useDatabase = "USE "+action.getDatabase();
            logEvent(LogEventType.GEN_DB_CONNECTION_START, dataFileName, "Try to connect to " + jndi + " and " + String.valueOf(useDatabase).toLowerCase(),-1,0);
            if(jndi==null || jndi.trim().length()==0){
                jndi = action.getDatabase();
                useDatabase = null;
            }
            processInterruption();

            //2.2.2. Connecting to the database server
            DataSource dataSource = createMsSqlDataSource(jndi);
            if(dataSource==null){
                logEvent(LogEventType.ERROR, dataFileName, "Failed to connect to " + jndi + " and " + String.valueOf(useDatabase).toLowerCase(),-1,0);
                getDbNotificationLogger().addGenStaticFileEvent(dataFileName, ActionState.PHASE_DB_CONNECT, ActionState.STATUS_COMPLETED_ERROR);
                return false;
            }
            mssqlConn = dataSource.getConnection();
            processInterruption();

            //2.2.3. Switching to right database
            execute(mssqlConn,dataFileName, useDatabase);
            logEvent(LogEventType.GEN_DB_CONNECTION_FINISH, dataFileName, "Connected to " + jndi + " and " + String.valueOf(useDatabase).toLowerCase(), -1, 0);
            processInterruption();

            //2.3. Make an sqlite connection
            String url = "jdbc:sqlite:" + tempFile.getAbsolutePath();
            sqliteConn = DriverManager.getConnection(url);
            configureSQLite(sqliteConn,dataFileName);
            setAutoCommit(sqliteConn, true, dataFileName);
            logEvent(LogEventType.DEBUG, dataFileName, "Connected to " + url,-1,0);
            processInterruption();

            getDbNotificationLogger().addGenStaticFileEvent(dataFileName, ActionState.PHASE_DB_CONNECT, ActionState.STATUS_COMPLETED_SUCCESSFULLY);
            //2.4. Listing tables to fill and execute one by one

            // Если проверки MSSQL базы данных исходника перед генерацией не прошли, то генерацию запускать не будем и выбрасываем исключение.
            List<ETLCheckError> errors = doGeneratorSqlChecks(action, mssqlConn, sqliteConn, dataFileName, true);
            if (!errors.isEmpty())
            	throw new GeneratorException("The generator MSSQL checks failed.", false,errors);

            // если генерация прошла успешно, то запускаем проверки SQlite результирующей базы.
            boolean generatorResult = doGenerate(action, sqliteConn, mssqlConn);
            if (generatorResult) {
            	// Если проверки SQlite результирующей базы данных не пройдены, то выбрасываем исключение, то есть считаем что генерация не удалась.
                errors = doGeneratorSqlChecks(action, mssqlConn, sqliteConn, dataFileName, false);
                if (!errors.isEmpty())
                	throw new GeneratorException("The generator SQLITE checks failed.", false,errors);
               // Если после проверки сгенеренного Sqlite БД ошибок не найдено, то считаем что БД успешно сгенерилась
               getDbNotificationLogger().addGenStaticFileEvent(dataFileName, ActionState.PHASE_FILE_GENERATING, ActionState.STATUS_COMPLETED_SUCCESSFULLY);

            }
            return generatorResult;

            //2.6. Schedule execution of VACUUM;
            //executeQuery(null, sqliteConn, "VACUUM");

            //2.7.Schedule dropping of old logs
            //long lastStart = dbLogService.getLastEventIdForEventType(LogEventType.GEN_QUEUED, dataFileName);
            //String sql = "DELETE FROM SYNC_LOGS WHERE EVENT_ID<"+lastStart+" AND EVENT_INFO='"+dataFileName+"'";
            //dbLogService.executeSQL(sql);
            //logProgress(dataFileName, 1,1);
        } catch (GeneratorException ge){
            if(ge.isCancelled()){
                logEvent(LogEventType.GEN_CANCELLED, dataFileName, "The generation has been cancelled successfully", -1 , 0);
                throw ge;
            } else {
                ge.printStackTrace();
                logEvent(LogEventType.ERROR, dataFileName, "Error generation on " + dataFileName + " exception message : " + ge.getMessage(), -1 , 0);
                throw ge;
            }
        } catch (SQLException se){
            se.printStackTrace();
            logEvent(LogEventType.ERROR, dataFileName, "Error generation on " + dataFileName + " exception message : " + se.getSQLState() + " - " + se.getMessage(), -1 , 0);
            processException(dataFileName, "", se);
        } finally {
            logEvent(LogEventType.GEN_DEBUG, dataFileName, "Start closing SQLite connection", -1, 0);
            SQLHelper.closeConnection(sqliteConn);
            logEvent(LogEventType.GEN_DEBUG, dataFileName, "Start closing MSSQL connection", -1, 0);
            SQLHelper.closeConnection(mssqlConn);
            logEvent(LogEventType.GEN_DEBUG, dataFileName, "Finish closing MSSQL connection", -1, 0);
        }
        return false;
    }

    public DataSource createMsSqlDataSource(String jndi) {
        return DataSourceHelper.getOrCreateDataSource(jndi);
    }

    /**
     * Выполнить проверки корректности БД MSSQL/SQLite
     * @param action
     * @param mssqlConn
     * @param sqliteConn
     * @param dataFileName
     * @return
     */
    private List<ETLCheckError> doGeneratorSqlChecks(ETLAction action,Connection mssqlConn,Connection sqliteConn,String dataFileName,boolean isMssql) {
    	List<ETLCheckError>	resultErrors = new ArrayList<ETLCheckError>();
    	// Пробегаемся по всем проверкам текущего действия
    	for(ETLCheck currentCheck : action.getPatternObject().getChecks()) {
    			PreparedStatement pst = null;
    			ResultSet rs = null;
    			List<ETLCheckError>	checkErrors = null;
    			try {

    				// Проверяем что TARGET коректен

    				// В зависимости от типа проверки выбираем нужное соединение
    	    		if (TARGETS.MSSQL.equals(currentCheck.getTarget()) && isMssql)
    	    			pst = mssqlConn.prepareStatement(currentCheck.getQuery().trim());
    	    		else if (TARGETS.SQLITE.equals(currentCheck.getTarget()) && !isMssql)
    	    			pst = sqliteConn.prepareStatement(currentCheck.getQuery().trim());
    	    		else if (!TARGETS.MSSQL.equals(currentCheck.getTarget()) && !TARGETS.SQLITE.equals(currentCheck.getTarget())) {
    					if (ETLCheck.IS_FAIL_ON_CHECK_DEFINITION_ERROR) {
    						ETLCheckError error = new ETLCheckError(CHECK_ERROR_TYPES.TARGET, "Sql check " + currentCheck.getName() + " failed because target " + currentCheck.getTarget() + " is invalid.");
    						resultErrors.add(error);
    					}
						continue;
    				} else
    	    			continue;

    	    		// Выполняем запрос
    				rs = pst.executeQuery();

    				// Получаем список ошибок
    				checkErrors = currentCheck.check(rs,action.getPatternObject().getApplication() + ":" + action.getDataFileName()/* TODO: Проверить корректность */,getParentService());

    			} catch (SQLException sqle) {
    				if (ETLCheck.IS_FAIL_ON_CHECK_DEFINITION_ERROR) {
        				if (checkErrors == null)
        					checkErrors = new ArrayList<ETLCheckError>();
        				// добавляем сообщение что проверки не пройдены
    					ETLCheckError error = new ETLCheckError(CHECK_ERROR_TYPES.EXCEPTION, "Sql exception (" + sqle.getMessage() + ") during executing sql check" + currentCheck.getName() + ". Checks Failed.");
    					checkErrors.add(error);
    				}
    			} finally {
    				SQLHelper.closeResultSet(rs);
    				SQLHelper.closeStatement(pst);
    			}

    			// ДОбавляем список ошибок текущей проверки к общему списку ошибок
    			resultErrors.addAll(checkErrors);
    	}

    	return resultErrors;

    }

    private boolean doGenerate(ETLAction action, Connection sqliteConn, Connection mssqlConn) throws SQLException {
        //1. Declaring
        String dataFileName = action.getDataFileName();
        printThreadId("doGenerate("+dataFileName+")");
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        getDbNotificationLogger().addGenStaticFileEvent(dataFileName, ActionState.PHASE_FILE_GENERATING, ActionState.STATUS_PERFORM);

        //2. Executing query by query
        try {
            List<String> sqls = action.getQueriesWithParamValues();
            List<ETLSeriesName> names = action.getNames();
            ETLSeriesName cEtlSeriesName = null;
            boolean skipStatementExecution = false;
            int queryIndex = 0;
            int nameIndex  = 0;
            for (; nameIndex < names.size(); nameIndex++) {
                processInterruption();
                if (!skipStatementExecution) {
                    String sql = sqls.get(nameIndex);
                    logEvent(LogEventType.GEN_DEBUG, dataFileName, "Executing sql :" + sql, nameIndex, sqls.size());
                    try {
                        pstmt = mssqlConn.prepareStatement(sql);
                        rs = pstmt.executeQuery();
                    } catch (Throwable t) {
                        processException(dataFileName, sql, t);
                    }
                    // skipStatementExecution = false;
                }
                ETLSeriesName etlSeriesName = names.get(nameIndex);

                // 2. Finding right change types
                processInterruption();
                List<ETLActionChangeType> seriesChangeTypes = new ArrayList<ETLActionChangeType>();
                for (int j = 0; j < action.getChangeTypes().size(); j++) {
                    ETLActionChangeType t = action.getChangeTypes().get(j);
                    if (t.getSeriesIndex() == etlSeriesName.getSeriesIndex()) {
                        seriesChangeTypes.add(t);
                    }
                }

                //3. Creating table and running custom query
                processInterruption();
                if (!(cEtlSeriesName != null && etlSeriesName.equals(cEtlSeriesName))) {
                    createTable(sqliteConn, dataFileName, etlSeriesName.getSeriesName(), seriesChangeTypes, rs);
                    String customQuery = etlSeriesName.getCustomSQLInTarget();
                    if(customQuery!=null && !"".equalsIgnoreCase(customQuery.trim())){
                        logEvent(LogEventType.GEN_DEBUG, dataFileName, "Start executing unnamed query from pattern " + action.getPatternName() + " : " + customQuery, nameIndex, sqls.size());
                        execute(sqliteConn, dataFileName, customQuery);
                        if(Thread.currentThread().isInterrupted()){return false;}
                    }
                    cEtlSeriesName = etlSeriesName;
                }

                //4. Populating table
                processInterruption();
                setAutoCommit(sqliteConn, false, dataFileName);
                logEvent(LogEventType.GEN_DEBUG, dataFileName, "Start populating table " + etlSeriesName, queryIndex, sqls.size());
                populateTable(sqliteConn, dataFileName, etlSeriesName.getSeriesName(), seriesChangeTypes, rs);
                logEvent(LogEventType.GEN_DEBUG  , dataFileName, "Finish populating table " + etlSeriesName,queryIndex,sqls.size());
                logProgress(dataFileName, queryIndex, sqls.size());
                try {
                    sqliteConn.commit();
                } catch (SQLException e) {
                    System.out.println("EXCEPTION WHEN COMMIT FOR "+dataFileName);
                    e.printStackTrace();
                }
                logEvent(LogEventType.GEN_DEBUG, dataFileName, "After commit " + etlSeriesName, -1, 0);

                if (pstmt.getMoreResults()) {
                    rs.close();
                    rs = pstmt.getResultSet();
                    skipStatementExecution = true;
                    continue;
                } else {
                    skipStatementExecution = false;
                    queryIndex++;

                    logEvent(LogEventType.GEN_DEBUG  , dataFileName, "Before close " + etlSeriesName,-1,0);
                    rs.close();
                    pstmt.close();
                    logEvent(LogEventType.GEN_DEBUG, dataFileName, "After close " + etlSeriesName, -1, 0);
                }
            }

            //2.5. Executing unnamed queries
            setAutoCommit(sqliteConn, true, dataFileName);
            for (int i = queryIndex; i < sqls.size(); i++) {
                processInterruption();
                String sql = sqls.get(i);
                logEvent(LogEventType.GEN_DEBUG, dataFileName, "Start executing unnamed query from pattern " + action.getPatternName() + " : " + sql,i,sqls.size());
                execute(sqliteConn, dataFileName, sql);
                logProgress(dataFileName, i,sqls.size());
            }

        //we do not catch Exception - they will be caught at level higher
        } finally {
            SQLHelper.closeResultSet(rs);
            SQLHelper.closeStatement(pstmt);
        }
        return true;
    }


    private void execute(Connection connection, String dataFileName, String sql) throws GeneratorException {
        Statement st = null;
        try {
            st = connection.createStatement();
            st.execute(sql);
        } catch (Throwable ex) {
            processException(dataFileName, sql, ex);
        } finally {
            System.out.println("CLOSING STATEMENT");
            SQLHelper.closeStatement(st);
        }
    }

    private void processException(String dataFileName, String sql, Throwable t) throws GeneratorException {
        log.error("Error executing statement :{" + sql + "} for "+dataFileName, t);
        //System.out.println("Error executing statement :{" + sql + "}");
        t.printStackTrace();
        if (isManualMode()) {
            System.exit(-1);
        } else {
            if (t instanceof GeneratorException) {
                throw (GeneratorException) t;
            } else {
                throw new GeneratorException("Error executing statement :{" + sql + "} for "+dataFileName, t, false);
            }
        }
    }

    private void processInterruption() throws GeneratorException {
        if(Thread.currentThread().isInterrupted()){
            throw new GeneratorException("Generation was cancelled", true);
        }
    }

    private void createTable(Connection sqliteConnection, String dataFileName, String sqliteTableName, List<ETLActionChangeType> changeTypes, ResultSet mssqlResultSet) throws SQLException {
        //1. Composing statement
        //1.1. Extract metadata
        printThreadId("createTable("+dataFileName+")");
        final StringBuilder stmtBuilder = new StringBuilder();
        ResultSetMetaData rsmd = mssqlResultSet.getMetaData();

        //1.2. Build create table statement
        stmtBuilder.append("CREATE TABLE " + escapeIdentifier(sqliteTableName) + " (");

        final int columnCount = rsmd.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rsmd.getColumnName(i);
            int columnType = rsmd.getColumnType(i);
            //log.debug("  column[" + columnName + "]: " + columnType);

            stmtBuilder.append(escapeIdentifier(columnName));
            stmtBuilder.append(" ");

            Integer newDataType = null;
            try {
                String newDataTypeStr = SeriesGenerator.getNewDataType(changeTypes, rsmd.getColumnName(i));
                if (newDataTypeStr != null)
                    newDataType = Integer.valueOf(newDataTypeStr);
            } catch (NumberFormatException e) {
                log.warn("cannot convert newDataType for column :" + rsmd.getColumnName(i) + " to any java.sql.Types, use original: " + rsmd.getColumnTypeName(i));
            }

            switch (newDataType == null ? rsmd.getColumnType(i) : newDataType) {

                /* Blob */
                case Types.BIT:
                case Types.DECIMAL:
                    stmtBuilder.append("NUMERIC");
                    break;

                /* Timestamp */
                case Types.TIMESTAMP:
                case Types.DATE:
                    stmtBuilder.append("DATETIME");
                    break;

                /* Strings */
                case Types.CHAR:
                case Types.LONGNVARCHAR:
                case Types.LONGVARCHAR:
                case Types.BINARY:
                    stmtBuilder.append("TEXT");
                    break;

                case Types.INTEGER:
                    stmtBuilder.append("INTEGER");
                    break;

                case Types.TIME:
                    stmtBuilder.append("NUMERIC");
                    break;
                case Types.VARBINARY:
                case Types.BLOB:
                    stmtBuilder.append("BLOB");
                    break;
                default:
                    stmtBuilder.append(rsmd.getColumnTypeName(i) + "(" + rsmd.getPrecision(i) + ")");
            }

            if(rsmd.isNullable(i)==ResultSetMetaData.columnNoNulls){
                stmtBuilder.append(" NOT NULL");
            }

            if (i + 1 <= columnCount)
                stmtBuilder.append(", ");
        }
        stmtBuilder.append(")");
        System.out.println(new java.util.Date()+" CREATE TABLE SQL: "+stmtBuilder);

        //2. Execute statement
        //String drop = "DROP TABLE IF EXISTS " +  escapeIdentifier(sqliteTableName);
        //execute(sqliteConnection,drop);
        String create = stmtBuilder.toString();
        execute(sqliteConnection, dataFileName, create);
        //sqliteConnection.commit();
    }

    private void populateTable (Connection sqliteConnection, String dataFileName, String tableName, List<ETLActionChangeType> changeTypes, ResultSet mssqlResultSet) throws SQLException {
        //1. Building SQL
        printThreadId("populateTable("+dataFileName+")");
        final StringBuilder stmtBuilder = new StringBuilder();
        ResultSetMetaData rsmd = mssqlResultSet.getMetaData();
        final StringBuilder valueStmtBuilder = new StringBuilder();

        /* Record the column count */
        final int columnCount = rsmd.getColumnCount();

        /* Build the INSERT statement (in two pieces simultaneously) */
        stmtBuilder.append("INSERT INTO " + escapeIdentifier(tableName) + " (");
        valueStmtBuilder.append("(");

        for (int i = 1; i <= columnCount; i++) {

            /* The column name and the VALUE binding */
            stmtBuilder.append(escapeIdentifier(rsmd.getColumnName(i)));
            valueStmtBuilder.append("?");

            if (i + 1 <= columnCount) {
                stmtBuilder.append(", ");
                valueStmtBuilder.append(", ");
            }
        }

        /* Now append the VALUES piece */
        stmtBuilder.append(") VALUES ");
        stmtBuilder.append(valueStmtBuilder);
        stmtBuilder.append(")");

        /* Create the prepared statement */
        PreparedStatement prep = null;

        //2. Doing all required inserts
        try {
            prep = sqliteConnection.prepareStatement(stmtBuilder.toString());
            int totalCounter = 0 ;
            while (mssqlResultSet.next()) {
                /* Bind all the column values. We let JDBC do type conversion -- is this correct?. */
                for (int i = 1; i <= columnCount; i++) {
                    final Object value = mssqlResultSet.getObject(i);

                    /* If null, just bail out early and avoid a lot of NULL checking */
                    if (value == null) {
                        prep.setNull(i, rsmd.getColumnType(i));
                        continue;
                    }

                    Integer newDataType = null;
                    try {
                        String newDataTypeStr = SeriesGenerator.getNewDataType(changeTypes, rsmd.getColumnName(i));
                        if (newDataTypeStr != null)
                            newDataType = Integer.valueOf(newDataTypeStr);
                    } catch (NumberFormatException e) {
                        log.warn("cannot convert newDataType for column :" + rsmd.getColumnName(i) + " to any java.sql.Types, use original: " + rsmd.getColumnTypeName(i));
                    }

                    /* Perform any conversions */
                    switch (newDataType == null ? rsmd.getColumnType(i) : newDataType) {
                        case Types.BOOLEAN:
                            /* The SQLite JDBC driver does not handle boolean values */
                            final boolean bool;
                            final int intVal;

                            /* Determine the value (1/0) */
                            bool = mssqlResultSet.getBoolean(i);
                            if (bool)
                                intVal = 1;
                            else
                                intVal = 0;

                            /* Store it */
                            prep.setInt(i, intVal);
                            break;
                        case Types.TIMESTAMP:
                            LocalDate ld = new LocalDate(mssqlResultSet.getTimestamp(i).getTime());
                            Date date = new Date(ld.toDateMidnight(DateTimeZone.UTC).toDate().getTime());
                            prep.setDate(i, date);
                            break;
                        case Types.DATE:
                            LocalDate ld1 = new LocalDate(mssqlResultSet.getDate(i).getTime());
                            prep.setString(i, ld1.toString() + " 00:00:00");
                            break;
                        case Types.BLOB:
                            if (value != null) {
                                prep.setBytes(i, (byte[]) value);
                            }
                            break;
                        default:
                            if (value instanceof Float) {
                                prep.setObject(i, ((Float) value).doubleValue());
                            } else {
                                prep.setObject(i, value);
                            }
                            break;
                    }

                }
                totalCounter++;

                /* Execute the insert */
                processInterruption();
                prep.executeUpdate();

                if(totalCounter % 1000000 == 0){
                    logEvent(LogEventType.GEN_DEBUG, dataFileName, "Inserted "+totalCounter+" rows to table "+tableName,-1,0);
                }
            }
        } finally {
            SQLHelper.closeStatement(prep);
        }
    }

    private String escapeIdentifier (final String identifier) {
        return "'" + identifier.replace("'", "''") + "'";
    }

    public boolean isManualMode() {
        return manualMode;
    }

    public void setManualMode(boolean manualMode) {
        this.manualMode = manualMode;
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        ETLDatabaseList databaseList  = (ETLDatabaseList) XMLHelper.readXML("C:\\usr\\temp\\databases.xml", ETLDatabase.class);
        ETLConfig config = ETLConfig.loadConfiFile("C:\\usr\\temp\\etl-test.xml");
        ETLDatabase database = databaseList.getDatabases().get(0);
        ETLAction action = config.getActions().get(0);
        SQLiteGenerator generator = new SQLiteGenerator();
        generator.generateFile(action, new File("C:\\sample.sqlite"));
    }

    protected void configureSQLite(Connection conn, String dataFileName){
        execute(conn, dataFileName, "PRAGMA CACHE = PRIVATE");
        execute(conn, dataFileName, "PRAGMA PAGE_SIZE=4096");
        execute(conn, dataFileName, "PRAGMA SYNCHRONOUS = OFF");
        execute(conn, dataFileName, "PRAGMA LOCKING_MODE = EXCLUSIVE");
    }

    protected void setAutoCommit(Connection conn, boolean autoCommit, String dataFileName){
        try {
            conn.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            System.out.println("EXCEPTION WHEN SET AUTOCOMMIT="+autoCommit+" FOR "+dataFileName);
            e.printStackTrace();
        }
    }

    public void printThreadId(String tag){
        System.out.println("THREAD ID IN "+tag+" = "+Thread.currentThread().toString());
    }
}
