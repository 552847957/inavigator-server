package ru.sberbank.syncserver2.service.file.transport;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.inav.test.util.TestUtils;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.*;
import ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister;
import ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger;
import ru.sberbank.syncserver2.service.sql.DataPowerMockObject;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.util.FileCopyHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/shared-sigma-network-file-mover-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class SharedSigmaNetworkFileMoverTest extends DataPowerMockObject {

    @Autowired
    @Qualifier("spyDatabaseFileListerTestBean")
    private DatabaseFileLister lister;

    @Autowired
    @Qualifier("spySharedSigmaNetworkFileMoverTestBean")
    private SharedSigmaNetworkFileMover sharedSigmaNetworkFileMoverService;

    @Autowired
    @Qualifier("spyServiceManagerTestBean")
    private ServiceManager serviceManager;

    @Autowired
    @Qualifier("spyConfigLoaderTestBean")
    private ConfigLoader configLoader;

    @Autowired
    @Qualifier("spyDataPowerServiceTestBean")
    private DataPowerService dataPowerService;

    @Autowired
    @Qualifier("spyDataPowerNotificationLogger")
    private DataPowerNotificationLogger dataPowerNotificationLogger;


    @Before
    public void before() throws Exception {
        super.before();
        //
        File f = new File("test/tmp/directory/SharedSigmaNetworkFileMoverTest/networkSourceFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "SharedSigmaNetworkFileMoverTest", "networkSourceFolder");
        f = new File("test/tmp/directory/SharedSigmaNetworkFileMoverTest/localTempFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "SharedSigmaNetworkFileMoverTest", "localTempFolder");
        f = new File("test/tmp/directory/SharedSigmaNetworkFileMoverTest/localDestFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "SharedSigmaNetworkFileMoverTest", "localDestFolder");
        f = new File("test/tmp/directory/SharedSigmaNetworkFileMoverTest/networkSharedFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "SharedSigmaNetworkFileMoverTest", "networkSharedFolder");
        //
        List<Folder> lf = new ArrayList<Folder>();
        Folder f1 = new Folder("changeset", 1, "changeset");
        lf.add(f1);
        doReturn(lf).when(configLoader).getFolders();
        doReturn(null).when(configLoader).getSyncConfigProperty("LOGGING_SERVICE");

        List<BeanProperty> lbp = new ArrayList<BeanProperty>();
        doReturn(lbp).when(configLoader).getBeanProperties("admin", "adminDbLogService", false);

        List<Bean> lb = new ArrayList<Bean>();
        Bean spyDatabaseFileListerBean = new Bean(1L, "changesetDatabaseFileLister", "ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister", null,
                null, 2, null, "changesetDatabaseFileLister desc");
        lb.add(spyDatabaseFileListerBean);
        Bean spySharedSigmaNetworkFileMoverBean = new Bean(2L, "sharedSigmaNetworkFileMover", "ru.sberbank.syncserver2.service.file.transport.SharedSigmaNetworkFileMover", null,
                null, 3, null, "sharedSigmaNetworkFileMover desc");
        lb.add(spySharedSigmaNetworkFileMoverBean);
        Bean spyDataPowerService = new Bean(3L, "dataPowerService", "ru.sberbank.syncserver2.service.sql.DataPowerService", null,
                null, 1, null, "dataPowerService desc");
        lb.add(spyDataPowerService);
        Bean spyDataPowerNotificationLogger = new Bean(4L, "dataPowerNotificationLogger", "ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger", null,
                null, 4, null, "dataPowerNotificationLogger desc");
        lb.add(spyDataPowerNotificationLogger);
        doReturn(lb).when(configLoader).getBeans("changeset");

        List<BeanProperty> databaseFileListerProperty = new ArrayList<BeanProperty>();
        doReturn(databaseFileListerProperty).when(configLoader).getBeanProperties("changeset", "changesetDatabaseFileLister", false);
        doReturn(databaseFileListerProperty).when(configLoader).getBeanProperties("changeset", "changesetDatabaseFileLister", true);

        List<BeanProperty> sharedSigmaNetworkFileMoverProperty = new ArrayList<BeanProperty>();
        BeanProperty bpd1 = new BeanProperty(1L, "networkSharedFolder", "test/tmp/directory/SharedSigmaNetworkFileMoverTest/networkSharedFolder/", "desc");
        sharedSigmaNetworkFileMoverProperty.add(bpd1);
        bpd1 = new BeanProperty(1L, "networkSourceFolder", "test/tmp/directory/SharedSigmaNetworkFileMoverTest/networkSourceFolder/", "desc");
        sharedSigmaNetworkFileMoverProperty.add(bpd1);
        bpd1 = new BeanProperty(1L, "localTempFolder", "test/tmp/directory/SharedSigmaNetworkFileMoverTest/localTempFolder/", "desc");
        sharedSigmaNetworkFileMoverProperty.add(bpd1);
        bpd1 = new BeanProperty(1L, "localDestFolder", "test/tmp/directory/SharedSigmaNetworkFileMoverTest/localDestFolder/", "desc");
        sharedSigmaNetworkFileMoverProperty.add(bpd1);
        doReturn(sharedSigmaNetworkFileMoverProperty).when(configLoader).getBeanProperties("changeset", "sharedSigmaNetworkFileMover", false);
        doReturn(sharedSigmaNetworkFileMoverProperty).when(configLoader).getBeanProperties("changeset", "sharedSigmaNetworkFileMover", true);

        List<BeanProperty> fileCacheProperty = new ArrayList<BeanProperty>();
        BeanProperty bpfc1 = new BeanProperty(1L, "debugModeWithoutLoadToMemory", "false", "desc");
        fileCacheProperty.add(bpfc1);
        doReturn(fileCacheProperty).when(configLoader).getBeanProperties("changeset", "changesetFileCache", false);
        doReturn(fileCacheProperty).when(configLoader).getBeanProperties("changeset", "changesetFileCache", true);

        List<BeanProperty> dataPowerServiceProperty = new ArrayList<BeanProperty>();
        doReturn(dataPowerServiceProperty).when(configLoader).getBeanProperties("changeset", "dataPowerService", false);
        doReturn(dataPowerServiceProperty).when(configLoader).getBeanProperties("changeset", "dataPowerService", true);

        List<BeanProperty> dataPowerNotificationLoggerProperty = new ArrayList<BeanProperty>();
        doReturn(dataPowerNotificationLoggerProperty).when(configLoader).getBeanProperties("changeset", "dataPowerNotificationLogger", false);
        doReturn(dataPowerNotificationLoggerProperty).when(configLoader).getBeanProperties("changeset", "dataPowerNotificationLogger", true);

        serviceManager.startAll();

        List<StaticFileInfo> staticFileList = new ArrayList<StaticFileInfo>();
        StaticFileInfo sfi = new StaticFileInfo("world", "fileId # 1", "Детальная архитектура v-7.docx", "localhost", true);
        staticFileList.add(sfi);
        sfi = new StaticFileInfo("world", "fileId # 2", "Детальная архитектура v-7.zip", "localhost", true);
        staticFileList.add(sfi);
        doReturn(staticFileList).when(configLoader).getStaticFileList();

        Map<String, ServiceContainer> changesetServiceContainer = serviceManager.getServiceContainerMap("changeset");

        ServiceContainer dataPowerServiceServiceContainer = changesetServiceContainer.get("dataPowerService");
        dataPowerServiceServiceContainer.stopService();
        dataPowerService.setServiceContainer(dataPowerServiceServiceContainer);
        dataPowerServiceServiceContainer.setService(dataPowerService);

        ServiceContainer dataPowerNotificationLoggerServiceContainer = changesetServiceContainer.get("dataPowerNotificationLogger");
        dataPowerNotificationLoggerServiceContainer.stopService();
        dataPowerNotificationLogger.setServiceContainer(dataPowerNotificationLoggerServiceContainer);
        dataPowerNotificationLoggerServiceContainer.setService(dataPowerNotificationLogger);

        ServiceContainer changesetDatabaseFileListerServiceContainer = changesetServiceContainer.get("changesetDatabaseFileLister");
        changesetDatabaseFileListerServiceContainer.stopService();
        lister.setServiceContainer(changesetDatabaseFileListerServiceContainer);
        changesetDatabaseFileListerServiceContainer.setService(lister);

        ServiceContainer sharedSigmaNetworkFileMoverServiceContainer = changesetServiceContainer.get("sharedSigmaNetworkFileMover");
        sharedSigmaNetworkFileMoverServiceContainer.stopService();
        sharedSigmaNetworkFileMoverService.setServiceContainer(sharedSigmaNetworkFileMoverServiceContainer);
        sharedSigmaNetworkFileMoverServiceContainer.setService(sharedSigmaNetworkFileMoverService);

        //
        FileCopyHelper.copyAndAddMD5(new File("src/test/resources/data/Детальная архитектура v-7.docx"), new File("test/tmp/directory/SharedSigmaNetworkFileMoverTest/networkSourceFolder/Детальная архитектура v-7.docx"));
        FileCopyHelper.copyAndAddMD5(new File("src/test/resources/data/Детальная архитектура v-7.zip"), new File("test/tmp/directory/SharedSigmaNetworkFileMoverTest/networkSourceFolder/Детальная архитектура v-7.zip"));
        //

        dataPowerServiceServiceContainer.startService();
        dataPowerNotificationLoggerServiceContainer.startService();
        changesetDatabaseFileListerServiceContainer.startService();
        sharedSigmaNetworkFileMoverServiceContainer.startService();
    }

    @After
    public void after() throws ComponentException, IOException {
        serviceManager.stopAll();
        super.after();
    }

    @Test
    public void test1() throws InterruptedException, IOException {
        File localDestFile = checkMissionComplete("test/tmp/directory/SharedSigmaNetworkFileMoverTest/localDestFolder/Детальная архитектура v-7.docx");
        checkFiles(localDestFile, new File("src/test/resources/data/Детальная архитектура v-7.docx"));
        localDestFile = checkMissionComplete("test/tmp/directory/SharedSigmaNetworkFileMoverTest/localDestFolder/Детальная архитектура v-7.zip");
        checkFiles(localDestFile, new File("src/test/resources/data/Детальная архитектура v-7.zip"));
    }

    private File checkMissionComplete(String theFileName) throws InterruptedException {
        File localDestFile;
        int cnt = 0;
        while (true) {
            localDestFile = new File(theFileName);
            if (localDestFile.exists()) break;
            Thread.sleep(1000L);
            cnt++;
            if (cnt > 600) Assert.fail("Exceed waiting time in 10 minutes!");
        }
        return localDestFile;
    }

    private void checkFiles(File localDestFile, File localOriginalFile) throws IOException {
        InputStream is1 = new FileInputStream(localDestFile);
        InputStream is2 = new FileInputStream(localOriginalFile);
        while (true) {
            int r1 = is1.read();
            int r2 = is2.read();
            Assert.assertEquals("Files not the same!", r1, r2);
            if (r1 == -1 || r2 == -1) break;
        }
    }

}
