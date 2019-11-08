package ru.sberbank.syncserver2.service.file.transport;

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
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.BeanProperty;
import ru.sberbank.syncserver2.service.core.config.Folder;
import ru.sberbank.syncserver2.service.core.config.MSSQLConfigLoader;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.log.DbLogServiceTestBase;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/alpha-network-file-mover-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class AlphaNetworkFileMoverTest extends DbLogServiceTestBase {

    @Autowired
    @Qualifier("alphaNetworkFileMoverTestBean")
    private AlphaNetworkFileMover fileMover;

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

    @Before
    @Override
    public void before() throws ComponentException, InterruptedException, SQLException {

        FileCopyHelper.reliableDeleteFolderAndSubFolders(new File("test/tmp/directory/AlphaNetworkFileMoverTest/tst"));

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
                        if ("ru.sberbank.syncserver2.service.file.transport.AlphaNetworkFileMover".equals(bean.getClazz())) {
                            return fileMover;
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
        changesetGeneratorServiceBean.setCode("changesetNetworkMover");
        changesetGeneratorServiceBean.setClazz("ru.sberbank.syncserver2.service.file.transport.AlphaNetworkFileMover");
        listBean.add(changesetGeneratorServiceBean);
        Bean singleClusterManagerBean = new Bean();
        singleClusterManagerBean.setCode("singleClusterManager");
        singleClusterManagerBean.setClazz("ru.sberbank.syncserver2.service.generator.ClusterManager");
        listBean.add(singleClusterManagerBean);
        Mockito.doReturn(listBean).when(configLoader).getBeans("changeset");

        Mockito.doReturn(new ArrayList<BeanProperty>()).when(configLoader).getBeanProperties("changeset", "changesetNetworkMover", true);
        Mockito.doReturn(new ArrayList<BeanProperty>()).when(configLoader).getBeanProperties("changeset", "singleClusterManager", true);

        Mockito.doReturn(true).when(сlusterManager).isActive();

        Mockito.doReturn("ALPHA_SOURCE_DB").when(configLoader).getSyncConfigProperty("ALPHA_SOURCE_DB");
        Mockito.doReturn(new ClusterManager.ActiveInfo(true, "localhost")).when(configLoader).<ClusterManager.ActiveInfo>getValue(Mockito.eq("exec SP_IS_HOST_ACTIVE ?"), Mockito.any(ResultSetExtractor.class), Matchers.anyVararg());

        serviceManager.startAll();

        FileHelper.createMissingFolders(fileMover.getNetworkTempFolder(), fileMover.getNetworkTargetFolder());
        FileCopyHelper.reliableCopy(new File("src/test/resources/data/Детальная архитектура v-7.docx"), new File("test/tmp/directory/AlphaNetworkFileMoverTest/tst/local/source/Детальная архитектура v-7.docx"));

    }

    @After
    public void after() throws ComponentException {
        serviceManager.stopAll();
        FileCopyHelper.reliableDeleteFolderAndSubFolders(new File("test/tmp/directory/AlphaNetworkFileMoverTest/tst"));
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
        File f = new File(fileMover.getNetworkTargetFolder() + "Детальная архитектура v-7.docx");
        return f.exists();
    }


}
