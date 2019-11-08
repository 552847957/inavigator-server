package ru.sberbank.syncserver2.service.qlikview;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.qlik.view.ObjectData;
import ru.sberbank.qlik.view.Response;
import ru.sberbank.syncserver2.mybatis.dao.QlikViewConectionDao;
import ru.sberbank.syncserver2.mybatis.domain.QlikViewConectionProperties;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.log.DbLogService;
import ru.sberbank.syncserver2.service.log.DbLogServiceTestBase;
import ru.sberbank.syncserver2.service.log.LogMsg;
import ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger;
import ru.sberbank.syncserver2.util.FormatHelper;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/qlik-view-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class QlikViewTest1 extends DbLogServiceTestBase {

    @Autowired
    @Qualifier("spyQlikViewServiceTestBean")
    private QlikViewRequestService service;

    @Autowired
    @Qualifier("qlikViewConectionDao")
    private QlikViewConectionDao qlikViewConectionDao;

    @Autowired
    @Qualifier("infra.bd.navigator.sqlSessionFactory")
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    @Qualifier("spyDatabaseNotificationLoggerTestBean")
    private DatabaseNotificationLogger databaseNotificationLoggerService;

    @Autowired
    @Qualifier("spyServiceManagerTestBeanTestBean")
    private ServiceManager sm;

    private Integer rowCount = 0;
    private Map<Integer, Map<String, String>> mapa = new HashMap<Integer, Map<String, String>>();
    private Boolean missionComplete = false;
    private String errMsg = null;


    private ServiceContainer sc = new ServiceContainer(Mockito.mock(ServiceManager.class, new Answer() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Method m = invocation.getMethod();
            if ("getConfigLoader".equals(m.getName())) {
                return Mockito.mock(ConfigLoader.class);
            }
            if ("findFirstServiceByClassCode".equals(m.getName())) {
                Mockito.doAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        Method m = invocation.getMethod();
                        Object[] args = invocation.getArguments();
                        if ("addError".equals(m.getName())) {
                            errMsg = (String) args[0];
                        }
                        return null;
                    }
                }).when(databaseNotificationLoggerService).addError(Mockito.any(String.class));
                //Mockito.doReturn(null).when(databaseNotificationLoggerService).addError(Mockito.any(String.class));
                Mockito.doReturn(databaseNotificationLoggerService).when(sm).findFirstServiceByClassCode(DatabaseNotificationLogger.class);
                return databaseNotificationLoggerService;
            }
            return null;
        }
    }), "qlikview", Mockito.mock(Bean.class));

    @Before
    @Override
    public void before() throws ComponentException, InterruptedException, SQLException {

        super.before();



        List<QlikViewConectionProperties> cp = new ArrayList<QlikViewConectionProperties>();
        QlikViewConectionProperties p = new QlikViewConectionProperties();
        cp.add(p);
        Mockito.doReturn(cp).when(qlikViewConectionDao).selectQlikViewConectionProperties(Mockito.any(SqlSessionFactory.class));
        Mockito.doReturn("serviceBeanCode").when(service).getJobIdentity();

        Response rspns = new Response();
        rspns.setDocument("document");
        List<ObjectData> lod = new ArrayList<ObjectData>();

        ObjectData od = new ObjectData();
        od.setId("id1");
        List<List<String>> lls = new ArrayList<List<String>>();
        List<String> ls = new ArrayList<String>();
        ls.add("1_1");
        ls.add("1_2");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("2_1");
        ls.add("2_2");
        lls.add(ls);
        od.setMatrix(lls);
        lod.add(od);

        od = new ObjectData();
        od.setId("id2");
        lls = new ArrayList<List<String>>();
        ls = new ArrayList<String>();
        ls.add("1_1_2");
        ls.add("1_2_2");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("2_1_2");
        ls.add("2_2_2");
        lls.add(ls);
        od.setMatrix(lls);
        lod.add(od);

        od = new ObjectData();
        od.setId("id3");
        od.setMatrix(null);
        lod.add(od);

        od = new ObjectData();
        od.setId("id4");
        od.setError(true);
        od.setErrorMessage("error!");
        od.setMatrix(null);
        lod.add(od);

        od = new ObjectData();
        od.setId("id5");
        lls = new ArrayList<List<String>>();
        ls = new ArrayList<String>();
        ls.add("1_1_1");
        lls.add(ls);
        od.setMatrix(lls);
        lod.add(od);

        rspns.setData(lod);
        rspns.setError(true);
        rspns.setErrorMessage("Error making document!");

        Mockito.doReturn(rspns).when(service).getObjectsData(Mockito.any(QlikViewConectionProperties.class));
        Mockito.doReturn(null).when(service).getClusterManager();

        synchronized(mapa) {
            rowCount = 0;
            mapa.clear();
            missionComplete = false;
        }
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                synchronized(mapa) {
                    System.out.println(FormatHelper.stringConcatenator("thread ", Thread.currentThread(), " before ", missionComplete));
                }
                return null;
            }
        }).when(service).doBeforeRun();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                synchronized(mapa) {
                    missionComplete = true;
                    System.out.println(FormatHelper.stringConcatenator("thread ", Thread.currentThread(), " after ", missionComplete));
                }
                return null;
            }
        }).when(service).doAfterRun();

        SqlSession session = Mockito.mock(SqlSession.class, new Answer(){

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                Object[] args = invocation.getArguments();
                if ("insert".equals(m.getName())) {
                    Map<String, String> row = (Map<String, String>) args[1];
                    synchronized(mapa) {
                        if (!missionComplete) {
                            mapa.put(rowCount, row);
                            rowCount++;
                        }
                    }
                    System.out.println(FormatHelper.stringConcatenator("thread ", Thread.currentThread(), " insert ", row));
                    return 0;
                } else if ("delete".equals(m.getName())) {
                    System.out.println(FormatHelper.stringConcatenator("thread ", Thread.currentThread(), " delete"));
                    return 0;
                } else if ("update".equals(m.getName())) {
                    System.out.println(FormatHelper.stringConcatenator("thread ", Thread.currentThread(), " update"));
                    return 0;
                }
                return null;
            }
        });
        Mockito.doReturn(session).when(sqlSessionFactory).openSession();
        service.setServiceContainer(sc);
        sc.setService(service);
        sc.startService();
        service.setStageSqlSessionFactory(sqlSessionFactory);

    }

    @After
    public void after() {
        sc.stopService();
    }

    @Test
    public void qlikViewServiceTest() throws UnsupportedEncodingException, InterruptedException {
        while(!getMissionComplete()) {
            Thread.sleep(10);
        }
        synchronized(mapa) {
            System.out.println(FormatHelper.stringConcatenator("thread ", Thread.currentThread(), " mapa ", mapa));
            Assert.assertEquals(new Integer(0), getMapaRowCount());
        }
        System.out.println(errMsg);
        Assert.assertEquals(
        "Error QlikView request: 'Error making document!'\r\n" +
                "Response{document='document', data=[" +
                    "ObjectData{id='id1', columns=null, matrix=[[1_1, 1_2], [2_1, 2_2]], error=false, errorMessage='null', title='null'}, " +
                    "ObjectData{id='id2', columns=null, matrix=[[1_1_2, 1_2_2], [2_1_2, 2_2_2]], error=false, errorMessage='null', title='null'}, " +
                    "ObjectData{id='id3', columns=null, matrix=null, error=false, errorMessage='null', title='null'}, " +
                    "ObjectData{id='id4', columns=null, matrix=null, error=true, errorMessage='error!', title='null'}, " +
                    "ObjectData{id='id5', columns=null, matrix=[[1_1_1]], error=false, errorMessage='null', title='null'}], error=true, errorMessage='Error making document!'}",
                errMsg);
    }

    private Integer getMapaRowCount() {
        Integer result = 0;
        synchronized(mapa) {
            result = mapa.size();
        }
        return result;
    }

    private Boolean getMissionComplete() {
        Boolean result = false;
        synchronized(mapa) {
            result = missionComplete;
        }
        return result;
    }

}
