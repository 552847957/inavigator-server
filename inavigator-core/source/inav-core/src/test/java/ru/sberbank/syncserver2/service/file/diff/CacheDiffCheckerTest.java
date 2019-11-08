package ru.sberbank.syncserver2.service.file.diff;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.invocation.InvocationImpl;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.BeanProperty;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.core.config.Folder;
import ru.sberbank.syncserver2.service.file.cache.FileCache;
import ru.sberbank.syncserver2.service.sql.DataPowerMockObject;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/cache-diff-checker-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class CacheDiffCheckerTest extends DataPowerMockObject {

    @Autowired
    @Qualifier("spyDataPowerServiceTestBean")
    private DataPowerService dataPowerService;

    @Autowired
    @Qualifier("spyCacheDiffCheckerTestBean")
    private CacheDiffChecker diffService;

    @Autowired
    @Qualifier("spyServiceManagerTestBean")
    private ServiceManager serviceManager;

    @Autowired
    @Qualifier("spyConfigLoaderTestBean")
    private ConfigLoader configLoader;

    @Autowired
    @Qualifier("spyFileCacheTestBean")
    private FileCache fileCacheService;


    @Before
    public void before() throws Exception {
        super.before();
        List<Folder> lf = new ArrayList<Folder>();
        Folder f1 = new Folder("changeset", 1, "changeset");
        lf.add(f1);
        doReturn(lf).when(configLoader).getFolders();
        doReturn(null).when(configLoader).getSyncConfigProperty("LOGGING_SERVICE");
        List<BeanProperty> lbp = new ArrayList<BeanProperty>();
        doReturn(lbp).when(configLoader).getBeanProperties("admin", "adminDbLogService", false);
        List<Bean> lb = new ArrayList<Bean>();
        Bean spyCacheDiffCheckerBean = new Bean(1L, "changesetFileCacheChecker", "ru.sberbank.syncserver2.service.file.diff.CacheDiffChecker", null,
                null, 1, null, "changesetFileCacheChecker desc");
        lb.add(spyCacheDiffCheckerBean);
        Bean spyDataPowerServiceBean = new Bean(2L, "misDataPowerService", "ru.sberbank.syncserver2.service.sql.DataPowerService", null,
                null, 2, null, "misDataPowerService desc");
        lb.add(spyDataPowerServiceBean);
        Bean spyFileCacheServiceBean = new Bean(3L, "changesetFileCache", "ru.sberbank.syncserver2.service.file.cache.FileCache", null,
                null, 3, null, "changesetFileCache desc");
        lb.add(spyFileCacheServiceBean);
        doReturn(lb).when(configLoader).getBeans("changeset");
        List<BeanProperty> fileCacheCheckerProperty = new ArrayList<BeanProperty>();
        BeanProperty bp1 = new BeanProperty(1L, "service", "finik2-new", "desc");
        fileCacheCheckerProperty.add(bp1);
        doReturn(fileCacheCheckerProperty).when(configLoader).getBeanProperties("changeset", "changesetFileCacheChecker", false);
        doReturn(fileCacheCheckerProperty).when(configLoader).getBeanProperties("changeset", "changesetFileCacheChecker", true);
        List<BeanProperty> dataPowerServiceProperty = new ArrayList<BeanProperty>();
        BeanProperty bpd1 = new BeanProperty(1L, "conversion", "CONVERT_TO_NEW_SIGMA", "desc");
        dataPowerServiceProperty.add(bpd1);
        doReturn(dataPowerServiceProperty).when(configLoader).getBeanProperties("changeset", "misDataPowerService", false);
        doReturn(dataPowerServiceProperty).when(configLoader).getBeanProperties("changeset", "misDataPowerService", true);
        List<BeanProperty> fileCacheProperty = new ArrayList<BeanProperty>();
        BeanProperty bpfc1 = new BeanProperty(1L, "debugModeWithoutLoadToMemory", "false", "desc");
        fileCacheProperty.add(bpfc1);
        doReturn(fileCacheProperty).when(configLoader).getBeanProperties("changeset", "changesetFileCache", false);
        doReturn(fileCacheProperty).when(configLoader).getBeanProperties("changeset", "changesetFileCache", true);
        serviceManager.startAll();
        Map<String, ServiceContainer> changesetServiceContainer = serviceManager.getServiceContainerMap("changeset");
        ServiceContainer changesetFileCacheCheckerServiceContainer = changesetServiceContainer.get("changesetFileCacheChecker");
        changesetFileCacheCheckerServiceContainer.setService(diffService);
        ServiceContainer misDataPowerServiceServiceContainer = changesetServiceContainer.get("misDataPowerService");
        misDataPowerServiceServiceContainer.setService(dataPowerService);
        ServiceContainer changesetFileCacheServiceContainer = changesetServiceContainer.get("changesetFileCache");
        changesetFileCacheServiceContainer.setService(fileCacheService);
    }

    @After
    public void after() throws ComponentException, IOException {
        serviceManager.stopAll();
        super.after();
    }

    @Test
    public void test1() throws IOException {
        final FileDiffList[] fd = new FileDiffList[1];
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream sqs = Mockito.mock(ServletOutputStream.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = ((InvocationImpl) invocation).getMethod();
                Object[] arguments = ((InvocationImpl) invocation).getArguments();
                if ("write".equals(m.getName())) {
                    FileDiffList fileDiffList = (FileDiffList)XMLHelper.readXMLFromByteArray((byte[])arguments[0], FileDiffList.class);
                    fd[0] = fileDiffList;
                }
                return null;
            }
        });
        doReturn(sqs).when(response).getOutputStream();
        diffService.request(request, response);
        String responseObject = fd[0].toString();
        Assert.assertTrue(
        "FileDiffList{diffs=[FileDiff{caption='/file #1!', status=3}, FileDiff{caption='/file #2!', status=3}]}".equals(responseObject) ||
                 "FileDiffList{diffs=[FileDiff{caption='/file #2!', status=3}, FileDiff{caption='/file #1!', status=3}]}".equals(responseObject));
        System.out.println(responseObject);
    }

}
