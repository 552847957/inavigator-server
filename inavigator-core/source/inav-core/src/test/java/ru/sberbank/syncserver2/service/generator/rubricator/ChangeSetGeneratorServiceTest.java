package ru.sberbank.syncserver2.service.generator.rubricator;

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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.BeanProperty;
import ru.sberbank.syncserver2.service.core.config.Folder;
import ru.sberbank.syncserver2.service.core.config.MSSQLConfigLoader;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.log.DbLogServiceTestBase;
import ru.sberbank.syncserver2.util.FileCopyHelper;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/changeset-generator-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class ChangeSetGeneratorServiceTest extends DbLogServiceTestBase {

    @Autowired
    @Qualifier("spyChangeSetGeneratorServiceTestBean")
    private ChangeSetGeneratorService generator;

    @Autowired
    @Qualifier("spyClusterManagerServiceTestBean")
    private ClusterManager сlusterManager;

    private Map<String, ServiceContainer> serviceContainer = new HashMap<String, ServiceContainer>();

    @Autowired
    @Qualifier("spyConfigLoaderTestBean")
    private MSSQLConfigLoader configLoader;

    @Autowired
    @Qualifier("spyServiceManagerTestBean")
    private ServiceManager serviceManager;

    private DataSource dataSource;
    private Connection conn;
    private NamedParameterJdbcTemplate jdbcTemplate;
    private CallableStatement loadUpdatesSQLps;
    private ResultSet loadUpdatesSQLrs;
    private int loadUpdatesSQLcnt = -1;
    private Timestamp nowTimeStamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
    private Boolean missionComplite = false;

    @Before
    @Override
    public void before() throws ComponentException, InterruptedException, SQLException {

        FileCopyHelper.reliableDeleteFolderAndSubFolders(new File(generator.getLocalFolder()));

        super.before();

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
                        if ("ru.sberbank.syncserver2.service.generator.rubricator.ChangeSetGeneratorService".equals(bean.getClazz())) {
                            return generator;
                        }
                        if ("ru.sberbank.syncserver2.service.generator.ClusterManager".equals(bean.getClazz())) {
                            return сlusterManager;
                        }
                        return invocation.callRealMethod();
                    }
                }).when(objSc).createAndConfigure(Mockito.any(Bean.class));

                serviceContainer.put(bean.getClazz(), objSc);
                return objSc;
            }
        }).when(serviceManager).createNewServiceContainer(Mockito.any(ServiceManager.class), Mockito.any(String.class), Mockito.any(Bean.class));

        List<Folder> folders = new ArrayList<Folder>();
        folders.add(new Folder("changeset", 0, "changeset"));
        Mockito.doReturn(folders).when(configLoader).getFolders();

        List<Bean> listBean = new ArrayList<Bean>();
        final Bean changesetGeneratorServiceBean = new Bean();
        changesetGeneratorServiceBean.setCode("changesetGenerator");
        changesetGeneratorServiceBean.setClazz("ru.sberbank.syncserver2.service.generator.rubricator.ChangeSetGeneratorService");
        listBean.add(changesetGeneratorServiceBean);
        Bean singleClusterManagerBean = new Bean();
        singleClusterManagerBean.setCode("singleClusterManager");
        singleClusterManagerBean.setClazz("ru.sberbank.syncserver2.service.generator.ClusterManager");
        listBean.add(singleClusterManagerBean);
        Mockito.doReturn(listBean).when(configLoader).getBeans("changeset");

        Mockito.doReturn(new ArrayList<BeanProperty>()).when(configLoader).getBeanProperties("changeset", "changesetGenerator", true);
        Mockito.doReturn(new ArrayList<BeanProperty>()).when(configLoader).getBeanProperties("changeset", "singleClusterManager", true);

        Mockito.doReturn(true).when(сlusterManager).isActive();

        Mockito.doReturn("ALPHA_SOURCE_DB").when(configLoader).getSyncConfigProperty("ALPHA_SOURCE_DB");
        Mockito.doReturn(new ClusterManager.ActiveInfo(true, "localhost")).when(configLoader).<ClusterManager.ActiveInfo>getValue(Mockito.eq("exec SP_IS_HOST_ACTIVE ?"), Mockito.any(ResultSetExtractor.class), Matchers.anyVararg());

        loadUpdatesSQLrs = Mockito.mock(ResultSet.class, new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                Object[] args = invocation.getArguments();
                if ("next".equals(m.getName())) {
                    return loadUpdatesSQLcnt-- > 0;
                }
                if ("getString".equals(m.getName())) {
                    String arg = (String) args[0];
                    if (ChangeSetGeneratorService.COL_RESOURCE_ID.equals(arg)) {
                        if (loadUpdatesSQLcnt == 1) {
                            return "file1.txt";
                        } else if (loadUpdatesSQLcnt == 0) {
                            return "file2.txt";
                        }
                    }
                    if (ChangeSetGeneratorService.COL_OPERATION.equals(arg)) {
                        if (loadUpdatesSQLcnt == 1) {
                            return ChangeSetGeneratorService.INSERTED;
                        } else if (loadUpdatesSQLcnt == 0) {
                            return ChangeSetGeneratorService.INSERTED;
                        }
                    }
                }
                if ("getTimestamp".equals(m.getName())) {
                    String arg = (String) args[0];
                    if (ChangeSetGeneratorService.COL_CHANGE_DATE.equals(arg)) {
                        if (loadUpdatesSQLcnt == 1) {
                            return nowTimeStamp;
                        } else if (loadUpdatesSQLcnt == 0) {
                            return nowTimeStamp;
                        }
                    }
                }
                if ("getInt".equals(m.getName())) {
                    String arg = (String) args[0];
                    if (ChangeSetGeneratorService.COL_CHANGELOG_RESOURCE_ID.equals(arg)) {
                        if (loadUpdatesSQLcnt == 1) {
                            return 1;
                        } else if (loadUpdatesSQLcnt == 0) {
                            return 2;
                        }
                    }
                    if (ChangeSetGeneratorService.COL_AUTOINCREMENT_ID.equals(arg)) {
                        if (loadUpdatesSQLcnt == 1) {
                            return 1;
                        } else if (loadUpdatesSQLcnt == 0) {
                            return 2;
                        }
                    }
                }
                if ("getBlob".equals(m.getName())) {
                    String arg = (String) args[0];
                    if (ChangeSetGeneratorService.COL_DATA.equals(arg)) {
                        if (loadUpdatesSQLcnt == 1) {
                            Blob b1 = Mockito.mock(Blob.class, new Answer() {

                                @Override
                                public Object answer(InvocationOnMock invocation) throws Throwable {
                                    Method m = invocation.getMethod();
                                    if ("getBinaryStream".equals(m.getName())) {
                                        return new ByteArrayInputStream("blob 1".getBytes());
                                    }
                                    return null;
                                }
                            });
                            return b1;
                        } else if (loadUpdatesSQLcnt == 0) {
                            Blob b1 = Mockito.mock(Blob.class, new Answer() {

                                @Override
                                public Object answer(InvocationOnMock invocation) throws Throwable {
                                    Method m = invocation.getMethod();
                                    if ("getBinaryStream".equals(m.getName())) {
                                        return new ByteArrayInputStream("blob 2".getBytes());
                                    }
                                    return null;
                                }
                            });
                            return b1;
                        }
                    }
                }
                return null;
            }
        });
        loadUpdatesSQLps = Mockito.mock(CallableStatement.class, new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                Object[] args = invocation.getArguments();
                if ("executeQuery".equals(m.getName())) {
                    if (loadUpdatesSQLcnt == -1) {
                        loadUpdatesSQLcnt = 2;
                    }
                    return loadUpdatesSQLrs;
                }
                return null;
            }
        });
        conn = Mockito.mock(Connection.class, new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                Object[] args = invocation.getArguments();
                if ("prepareCall".equals(m.getName())) {
                    String arg = (String) args[0];
                    return loadUpdatesSQLps;
                }
                return null;
            }
        });
        dataSource = Mockito.mock(DataSource.class, new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                if ("getConnection".equals(m.getName())) {
                    return conn;
                }
                return null;
            }
        });
        jdbcTemplate = Mockito.mock(NamedParameterJdbcTemplate.class, new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        });
        Mockito.doReturn(dataSource).when(generator).createNewDataSource();
        Mockito.doReturn(jdbcTemplate).when(generator).createNewNamedParameterJdbcTemplate(Mockito.any(DataSource.class));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Object result = invocation.callRealMethod();
                ChangeSetComposer composer = (ChangeSetComposer) args[0];
                File tempFile = composer.getTempFile();
                synchronized (missionComplite) {
                    missionComplite = true;
                }
                return result;
            }
        }).when(generator).moveTempFileToGenerated(Mockito.any(ChangeSetComposer.class), Mockito.any(ChangeSetGeneratorService.LastDateAndId.class));

        serviceManager.startAll();

    }

    @After
    public void after() throws ComponentException {
        serviceManager.stopAll();
        FileCopyHelper.reliableDeleteFolderAndSubFolders(new File(generator.getLocalFolder()));
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
    }

    public Boolean getMissionComplite() {
        synchronized (missionComplite) {
            return missionComplite;
        }
    }

}
