package ru.sberbank.syncserver2.service.generator.single;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.*;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.generator.single.data.ETLAction;
import ru.sberbank.syncserver2.service.generator.single.data.ETLExecutionInfo;
import ru.sberbank.syncserver2.service.log.DbLogServiceTestBase;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.log.LogMsg;
import ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger;
import ru.sberbank.syncserver2.util.FileCopyHelper;

import javax.sql.DataSource;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/single-generator-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class SingleGeneratorServiceTest extends DbLogServiceTestBase {

    @Autowired
    @Qualifier("spySingleGeneratorServiceTestBean")
    private SingleGeneratorService generator;

    @Autowired
    @Qualifier("spyClusterManagerServiceTestBean")
    private ClusterManager сlusterManager;

    @Autowired
    @Qualifier("spyDatabaseNotificationLoggerTestBean")
    private DatabaseNotificationLogger databaseNotificationLogger;

    private Map<String, ServiceContainer> serviceContainer = new HashMap<String, ServiceContainer>();

    @Autowired
    @Qualifier("spyConfigLoaderTestBean")
    private MSSQLConfigLoader configLoader;

    @Autowired
    @Qualifier("spyServiceManagerTestBean")
    private ServiceManager serviceManager;

    private TaskScheduler taskScheduler;

    private SQLiteGenerator sqLiteGenerator;

    private int rowCount = 2;

    private Boolean missionComplite = false;

    @Before
    @Override
    public void before() throws ComponentException, InterruptedException, SQLException {

        super.before();

        final List<Timestamp> refreshTimes = new ArrayList<Timestamp>();
        Timestamp ts = new Timestamp(Calendar.getInstance().getTimeInMillis());
        refreshTimes.add(ts);

        final List<Timestamp> actualTimes = new ArrayList<Timestamp>();
        Timestamp tsActual = new Timestamp(Calendar.getInstance().getTimeInMillis());
        tsActual.setTime(tsActual.getTime() + 10000L);
        actualTimes.add(tsActual);

        ConfigManager configManager = Mockito.spy(new ConfigManager());
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                taskScheduler = Mockito.spy((TaskScheduler) invocation.callRealMethod());
                Mockito.doReturn(actualTimes).when(taskScheduler).getDataActualTimes(Mockito.any(ETLAction.class));
                return taskScheduler;
            }
        }).when(configManager).createNewTaskScheduler(Mockito.any(ConfigLoader.class));
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ETLExecutionInfo info = (ETLExecutionInfo) invocation.getArguments()[0];
                dbLogService.log(new LogMsg("eventId", Calendar.getInstance().getTime(), "qq@qq.qq", "clientEventId", "clientDeviceId",
                        LogEventType.DEBUG, "startEventId", "ETLExecutionInfo: " + info, "clientIpAddress", "webHostName",
                        "webAppName", "distribServer", "eventInfo", "errorStackTrace"));

                synchronized(missionComplite) {
                    missionComplite = true;
                }
                return null;
            }
        }).when(configManager).completeAction(Mockito.any(ETLExecutionInfo.class));
        Mockito.doReturn(configManager).when(generator).createNewConfigManager();

        Mockito.doReturn(null).when(configLoader).getSyncConfigProperty("LOGGING_SERVICE");
        Mockito.doReturn(new ArrayList<BeanProperty>()).when(configLoader).getBeanProperties("admin", "adminDbLogService", false);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Bean bean = (Bean) invocation.getArguments()[2];
                ServiceContainer objSc = (ServiceContainer) Mockito.spy(invocation.callRealMethod());

                Mockito.doAnswer(new Answer() {

                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        Bean bean = (Bean) invocation.getArguments()[0];
                        if ("ru.sberbank.syncserver2.service.log.DbLogService".equals(bean.getClazz())) {
                            return dbLogService;
                        }
                        if ("ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService".equals(bean.getClazz())) {
                            return generator;
                        }
                        if ("ru.sberbank.syncserver2.service.generator.ClusterManager".equals(bean.getClazz())) {
                            return сlusterManager;
                        }
                        if ("ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger".equals(bean.getClazz())) {
                            return databaseNotificationLogger;
                        }
                        return invocation.callRealMethod();
                    }
                }).when(objSc).createAndConfigure(Mockito.any(Bean.class));

                serviceContainer.put(bean.getClazz(), objSc);
                return objSc;
            }
        }).when(serviceManager).createNewServiceContainer(Mockito.any(ServiceManager.class), Mockito.any(String.class), Mockito.any(Bean.class));

        List<Folder> folders = new ArrayList<Folder>();
        folders.add(new Folder("single", 0, "single"));
        Mockito.doReturn(folders).when(configLoader).getFolders();

        List<Bean> listBean = new ArrayList<Bean>();
        Bean singleGeneratorServiceBean = new Bean();
        singleGeneratorServiceBean.setCode("singleGenerator");
        singleGeneratorServiceBean.setClazz("ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService");
        listBean.add(singleGeneratorServiceBean);
        Bean singleClusterManagerBean = new Bean();
        singleClusterManagerBean.setCode("singleClusterManager");
        singleClusterManagerBean.setClazz("ru.sberbank.syncserver2.service.generator.ClusterManager");
        listBean.add(singleClusterManagerBean);
        Bean databaseNotificationLoggerBean = new Bean();
        databaseNotificationLoggerBean.setCode("singleDatabaseNotificationLogger");
        databaseNotificationLoggerBean.setClazz("ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger");
        listBean.add(databaseNotificationLoggerBean);
        Mockito.doReturn(listBean).when(configLoader).getBeans("single");

        Mockito.doReturn(new ArrayList<BeanProperty>()).when(configLoader).getBeanProperties("single", "singleGenerator", true);
        Mockito.doReturn(new ArrayList<BeanProperty>()).when(configLoader).getBeanProperties("single", "singleClusterManager", true);
        Mockito.doReturn(new ArrayList<BeanProperty>()).when(configLoader).getBeanProperties("single", "singleDatabaseNotificationLogger", true);

        Mockito.doReturn(true).when(сlusterManager).isActive();

        Mockito.doReturn("ALPHA_SOURCE_DB").when(configLoader).getSyncConfigProperty("ALPHA_SOURCE_DB");
        Mockito.doReturn(new ClusterManager.ActiveInfo(true, "localhost")).when(configLoader).<ClusterManager.ActiveInfo>getValue(Mockito.eq("exec SP_IS_HOST_ACTIVE ?"), Mockito.any(ResultSetExtractor.class), Matchers.anyVararg());

        FileCopyHelper.reliableCopy(new File("src/test/resources/data/SingleGeneratorServiceTest/etl.xml"), new File("test/tmp/directory/SingleGeneratorServiceTest/configHome/inbox/etl.xml"));

        List<StaticFileInfo> staticFileInfos = new ArrayList<StaticFileInfo>();
        StaticFileInfo staticFileInfo = new StaticFileInfo("MISMobile", "RETAIL_CRED", "RETAIL_CRED.sqlite", "127.0.0.1", true, true, false, true);
        staticFileInfos.add(staticFileInfo);
        Mockito.doReturn(staticFileInfos).when(configLoader).getStaticFileList();

        Mockito.doReturn(refreshTimes).when(configLoader).getValue(Mockito.eq("exec SP_IPAD_GET_LAST_REFRESH_TIME ?"), Mockito.any(TaskScheduler.TimestampListExtractor.class), Mockito.eq("retail_cred"));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String fileName = (String) invocation.getArguments()[0];
                dbLogService.log(new LogMsg("eventId", Calendar.getInstance().getTime(), "qq@qq.qq", "clientEventId", "clientDeviceId",
                        LogEventType.DEBUG, "startEventId", "addGeneration: " + fileName, "clientIpAddress", "webHostName",
                        "webAppName", "distribServer", "eventInfo", "errorStackTrace"));
                return null;
            }
        }).when(databaseNotificationLogger).addGeneration(Mockito.any(String.class));
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String fileName = (String) invocation.getArguments()[0];
                dbLogService.log(new LogMsg("eventId", Calendar.getInstance().getTime(), "qq@qq.qq", "clientEventId", "clientDeviceId",
                        LogEventType.DEBUG, "startEventId", "startGenStaticFileEvent: " + fileName, "clientIpAddress", "webHostName",
                        "webAppName", "distribServer", "eventInfo", "errorStackTrace"));
                return null;
            }
        }).when(databaseNotificationLogger).startGenStaticFileEvent(Mockito.any(String.class));
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String fileName = (String) invocation.getArguments()[0];
                String phaseCode = (String) invocation.getArguments()[1];
                String statusCode = (String) invocation.getArguments()[2];
                String comment = (String) invocation.getArguments()[3];
                String host = (String) invocation.getArguments()[4];
                dbLogService.log(new LogMsg("eventId", Calendar.getInstance().getTime(), "qq@qq.qq", "clientEventId", "clientDeviceId",
                        LogEventType.DEBUG, phaseCode, "addGenStaticFileEvent: " + fileName, host, statusCode,
                        "webAppName", "distribServer", comment, "errorStackTrace"));
                return null;
            }
        }).when(databaseNotificationLogger).addGenStaticFileEvent(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class));
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String fileName = (String) invocation.getArguments()[0];
                dbLogService.log(new LogMsg("eventId", Calendar.getInstance().getTime(), "qq@qq.qq", "clientEventId", "clientDeviceId",
                        LogEventType.DEBUG, "startEventId", "addError: " + fileName, "clientIpAddress", "webHostName",
                        "webAppName", "distribServer", "eventInfo", "errorStackTrace"));
                return null;
            }
        }).when(databaseNotificationLogger).addError(Mockito.any(String.class));
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String fileName = (String) invocation.getArguments()[0];
                dbLogService.log(new LogMsg("eventId", Calendar.getInstance().getTime(), "qq@qq.qq", "clientEventId", "clientDeviceId",
                        LogEventType.DEBUG, "startEventId", "delGeneration: " + fileName, "clientIpAddress", "webHostName",
                        "webAppName", "distribServer", "eventInfo", "errorStackTrace"));
                return null;
            }
        }).when(databaseNotificationLogger).delGeneration(Mockito.any(String.class));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String sql = (String) invocation.getArguments()[0];
                dbLogService.log(new LogMsg("eventId", Calendar.getInstance().getTime(), "qq@qq.qq", "clientEventId", "clientDeviceId",
                        LogEventType.DEBUG, "startEventId", "executePattern: sql = " + sql, "clientIpAddress", "webHostName",
                        "webAppName", "distribServer", "eventInfo", "errorStackTrace"));
                return null;
            }
        }).when(configLoader).executePattern(Mockito.any(String.class), Matchers.anyVararg());

        sqLiteGenerator = Mockito.spy(new SQLiteGenerator());
        DataSource dataSource = Mockito.mock(DataSource.class);
        final PreparedStatement pstmt = Mockito.mock(PreparedStatement.class);
        Connection mssqlConn = Mockito.mock(Connection.class);
        Statement statement = Mockito.mock(Statement.class);

        ResultSet rs = Mockito.mock(ResultSet.class, new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                Object[] args = invocation.getArguments();
                if ("next".equals(m.getName())) {
                    rowCount--;
                    return rowCount >= 0;
                }
                if ("getObject".equals(m.getName())) {
                    Integer i = (Integer) args[0];
                    if (i == 1) {
                        if (rowCount == 1) {
                            return 1;
                        } else if (rowCount == 0) {
                            return 2;
                        }
                    } else if (i == 2) {
                        if (rowCount == 1) {
                            return "This is 1";
                        } else if (rowCount == 0) {
                            return "This is 2";
                        }
                    }
                }
                if ("getBoolean".equals(m.getName())) {
                    Integer i = (Integer) args[0];
                }
                if ("getTimestamp".equals(m.getName())) {
                    Integer i = (Integer) args[0];
                }
                if ("getDate".equals(m.getName())) {
                    Integer i = (Integer) args[0];
                }
                return null;
            }
        });


        ResultSetMetaData rsmd = Mockito.mock(ResultSetMetaData.class, new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                Object[] args = invocation.getArguments();
                if ("getColumnCount".equals(m.getName())) {
                    return 2;
                }
                if ("getColumnName".equals(m.getName())) {
                    Integer i = (Integer) args[0];
                    if (i == 1) {
                        return "F1";
                    } else if (i == 2) {
                        return "F2";
                    }
                }
                if ("getColumnType".equals(m.getName())) {
                    Integer i = (Integer) args[0];
                    if (i == 1) {
                        return Types.INTEGER;
                    } else if (i == 2) {
                        return Types.CHAR;
                    }
                }
                if ("getColumnTypeName".equals(m.getName())) {
                    Integer i = (Integer) args[0];
                    if (i == 1) {
                        return "INTEGER";
                    } else if (i == 2) {
                        return "CHAR";
                    }
                }
                if ("getPrecision".equals(m.getName())) {
                    Integer i = (Integer) args[0];
                    if (i == 1) {
                        return 10;
                    } else if (i == 2) {
                        return 128;
                    }
                }
                if ("isNullable".equals(m.getName())) {
                    Integer i = (Integer) args[0];
                    if (i == 1) {
                        return 0;
                    } else if (i == 2) {
                        return 1;
                    }
                }
                return null;
            }
        });

        Mockito.doReturn(rsmd).when(rs).getMetaData();
        Mockito.doReturn(false).when(pstmt).getMoreResults();
        Mockito.doReturn(rs).when(pstmt).executeQuery();
        Mockito.doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                if ("prepareStatement".equals(m.getName())) {
                    rowCount = 2;
                    return pstmt;
                }
                return null;
            }
        }).when(mssqlConn).prepareStatement(Mockito.any(String.class));
        Mockito.doReturn(statement).when(mssqlConn).createStatement();
        Mockito.doReturn(mssqlConn).when(dataSource).getConnection();
        Mockito.doReturn(dataSource).when(sqLiteGenerator).createMsSqlDataSource(Mockito.any(String.class));
        Mockito.doReturn(sqLiteGenerator).when(generator).createSQLiteGenerator();

        serviceManager.startAll();

    }

    @After
    public void after() throws ComponentException {
        serviceManager.stopAll();
        FileCopyHelper.reliableDelete(new File("test/tmp/directory/SingleGeneratorServiceTest/configHome/inbox/etl.xml"));
        FileCopyHelper.reliableDelete(new File("test/tmp/directory/SingleGeneratorServiceTest/configHome/work/etl.xml"));
        FileCopyHelper.reliableDeleteFolderContent(new File("test/tmp/directory/SingleGeneratorServiceTest/localFileHome/generated"));
    }

    @Test
    public void test1() throws InterruptedException, SQLException {
        int cnt = 0;
        while (!getMissionComplite()) {
            if (cnt > 180) {
                break;
            }
            Thread.sleep(1000);
            cnt++;
        }
        Assert.assertTrue(getMissionComplite());
        String dataFileName = "test/tmp/directory/SingleGeneratorServiceTest/localFileHome/generated/RETAIL_CRED.sqlite";
        File tempFile = new File("test/tmp/directory/SingleGeneratorServiceTest/localFileHome/generated/RETAIL_CRED.sqlite");
        String url = "jdbc:sqlite:" + tempFile.getAbsolutePath();
        Connection sqliteConn = DriverManager.getConnection(url);
        try {
            sqLiteGenerator.configureSQLite(sqliteConn, dataFileName);
            sqLiteGenerator.setAutoCommit(sqliteConn, true, dataFileName);
            PreparedStatement prepStmt = sqliteConn.prepareStatement("SELECT * FROM \"t 000 080 Volume Requests\" order by F1");
            ResultSet rs = prepStmt.executeQuery();
            rs.next();
            Assert.assertEquals(rs.getInt("F1"), 1);
            Assert.assertEquals(rs.getString("F2"), "This is 1");
            rs.next();
            Assert.assertEquals(rs.getInt("F1"), 2);
            Assert.assertEquals(rs.getString("F2"), "This is 2");
        } finally {
            sqliteConn.close();
        }

    }

    public Boolean getMissionComplite() {
        synchronized(missionComplite) {
            return missionComplite;
        }
    }

}
