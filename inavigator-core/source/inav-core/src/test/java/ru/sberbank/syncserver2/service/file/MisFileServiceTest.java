package ru.sberbank.syncserver2.service.file;

import org.apache.commons.io.FileUtils;
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
import ru.sberbank.inav.test.util.TestUtils;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.*;
import ru.sberbank.syncserver2.service.file.cache.FileCacheDraftSupported;
import ru.sberbank.syncserver2.service.file.cache.SingleFileLoader;
import ru.sberbank.syncserver2.service.file.cache.SingleFileStatusCacheService;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;
import ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister;
import ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger;
import ru.sberbank.syncserver2.service.sql.DataPowerMockObject;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/single-file-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class MisFileServiceTest extends DataPowerMockObject {

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
    @Qualifier("spyMisFileServiceTestBean")
    private FileService fileService;

    @Autowired
    @Qualifier("spyFileCacheDraftTestBean")
    private FileCacheDraftSupported fileCache;

    @Autowired
    @Qualifier("spyMisFileLoaderTestBean")
    private SingleFileLoader misFileLoader;

    @Autowired
    @Qualifier("spyDataPowerNotificationLogger")
    private DataPowerNotificationLogger dataPowerNotificationLogger;

    @Autowired
    @Qualifier("spyMisDbFileListerTestBean")
    private DatabaseFileLister databaseFileLister;

    @Autowired
    @Qualifier("spyMisFileStatusCacheServerTestBean")
    private SingleFileStatusCacheService singleFileStatusCacheService;

    @Before
    public void before() throws Exception {
        super.before();
        //
        //
        File f = new File("test/tmp/directory/MisFileServiceTest/archiveFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "MisFileServiceTest", "archiveFolder");
        f = new File("test/tmp/directory/MisFileServiceTest/cacheFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "MisFileServiceTest", "cacheFolder");
        f = new File("test/tmp/directory/MisFileServiceTest/inboxFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "MisFileServiceTest", "inboxFolder");
        f = new File("test/tmp/directory/MisFileServiceTest/tempFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "MisFileServiceTest", "tempFolder");
        f = new File("test/tmp/directory/MisFileServiceTest/mbrList");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "MisFileServiceTest", "mbrList");
        //

        List<Folder> lf = new ArrayList<Folder>();
        Folder f1 = new Folder("single", 1, "single");
        lf.add(f1);
        doReturn(lf).when(configLoader).getFolders();
        doReturn(null).when(configLoader).getSyncConfigProperty("LOGGING_SERVICE");

        List<BeanProperty> lbp = new ArrayList<BeanProperty>();
        doReturn(lbp).when(configLoader).getBeanProperties("admin", "adminDbLogService", false);

        List<Bean> lb = new ArrayList<Bean>();

        Bean spyMisFileStatusCacheServerBean = new Bean(1L, "misFileStatusCacheServer", "ru.sberbank.syncserver2.service.file.cache.SingleFileStatusCacheService", null,
                null, 6, null, "misFileStatusCacheServer desc");
        lb.add(spyMisFileStatusCacheServerBean);

        Bean spyMisDbFileListerBean = new Bean(2L, "misDbFileLister", "ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister", null,
                null, 5, null, "misDbFileLister desc");
        lb.add(spyMisDbFileListerBean);

        Bean spyMisFileLoaderBean = new Bean(3L, "misFileLoader", "ru.sberbank.syncserver2.service.file.cache.SingleFileLoader", null,
                null, 4, null, "misFileLoader desc");
        lb.add(spyMisFileLoaderBean);

        Bean spyMisFileCacheBean = new Bean(4L, "misFileCache", "ru.sberbank.syncserver2.service.file.cache.FileCacheDraftSupported", null,
                null, 3, null, "misFileCache desc");
        lb.add(spyMisFileCacheBean);

        Bean spyMisFileServiceBean = new Bean(5L, "misFileService", "ru.sberbank.syncserver2.service.file.FileService", null,
                null, 2, "file.do", "misFileService desc");
        lb.add(spyMisFileServiceBean);

        Bean spyDataPowerService = new Bean(6L, "misDataPowerService", "ru.sberbank.syncserver2.service.sql.DataPowerService", null,
                null, 1, null, "misDataPowerService desc");
        lb.add(spyDataPowerService);

        Bean spyDataPowerNotificationLogger = new Bean(7L, "dataPowerNotificationLogger", "ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger", null,
                null, 7, null, "dataPowerNotificationLogger desc");
        lb.add(spyDataPowerNotificationLogger);

        doReturn(lb).when(configLoader).getBeans("single");

        List<BeanProperty> misFileStatusCacheServerProperty = new ArrayList<BeanProperty>();
        doReturn(misFileStatusCacheServerProperty).when(configLoader).getBeanProperties("single", "misFileStatusCacheServer", false);
        doReturn(misFileStatusCacheServerProperty).when(configLoader).getBeanProperties("single", "misFileStatusCacheServer", true);

        List<StaticFileInfo> files = new ArrayList<StaticFileInfo>();
        StaticFileInfo sfi = new StaticFileInfo("word", "1", "Детальная архитектура v-7.docx");
        files.add(sfi);
        doReturn(files).when(configLoader).getStaticFileList();

        List<BeanProperty> misDbFileListerProperty = new ArrayList<BeanProperty>();
        doReturn(misDbFileListerProperty).when(configLoader).getBeanProperties("single", "misDbFileLister", false);
        doReturn(misDbFileListerProperty).when(configLoader).getBeanProperties("single", "misDbFileLister", true);

        List<BeanProperty> misFileLoaderProperty = new ArrayList<BeanProperty>();
        doReturn(misFileLoaderProperty).when(configLoader).getBeanProperties("single", "misFileLoader", false);
        doReturn(misFileLoaderProperty).when(configLoader).getBeanProperties("single", "misFileLoader", true);

        List<BeanProperty> misFileCacheProperty = new ArrayList<BeanProperty>();
        doReturn(misFileCacheProperty).when(configLoader).getBeanProperties("single", "misFileCache", false);
        doReturn(misFileCacheProperty).when(configLoader).getBeanProperties("single", "misFileCache", true);

        List<BeanProperty> misFileServiceProperty = new ArrayList<BeanProperty>();
        doReturn(misFileServiceProperty).when(configLoader).getBeanProperties("single", "misFileService", false);
        doReturn(misFileServiceProperty).when(configLoader).getBeanProperties("single", "misFileService", true);

        List<BeanProperty> dataPowerServiceProperty = new ArrayList<BeanProperty>();
        doReturn(dataPowerServiceProperty).when(configLoader).getBeanProperties("single", "dataPowerService", false);
        doReturn(dataPowerServiceProperty).when(configLoader).getBeanProperties("single", "dataPowerService", true);

        List<BeanProperty> misDataPowerServiceProperty = new ArrayList<BeanProperty>();
        doReturn(misDataPowerServiceProperty).when(configLoader).getBeanProperties("single", "misDataPowerService", false);
        doReturn(misDataPowerServiceProperty).when(configLoader).getBeanProperties("single", "misDataPowerService", true);

        List<BeanProperty> dataPowerNotificationLoggerProperty = new ArrayList<BeanProperty>();
        doReturn(dataPowerNotificationLoggerProperty).when(configLoader).getBeanProperties("single", "dataPowerNotificationLogger", false);
        doReturn(dataPowerNotificationLoggerProperty).when(configLoader).getBeanProperties("single", "dataPowerNotificationLogger", true);


        serviceManager.startAll();

        Map<String, ServiceContainer> singleServiceContainer = serviceManager.getServiceContainerMap("single");

        ServiceContainer dataPowerNotificationLoggerServiceContainer = singleServiceContainer.get("dataPowerNotificationLogger");
        dataPowerNotificationLoggerServiceContainer.stopService();
        dataPowerNotificationLogger.setServiceContainer(dataPowerNotificationLoggerServiceContainer);
        dataPowerNotificationLoggerServiceContainer.setService(dataPowerNotificationLogger);

        ServiceContainer dataPowerServiceServiceContainer = singleServiceContainer.get("misDataPowerService");
        dataPowerServiceServiceContainer.stopService();
        dataPowerService.setServiceContainer(dataPowerServiceServiceContainer);
        dataPowerServiceServiceContainer.setService(dataPowerService);

        ServiceContainer misFileStatusCacheServerServiceContainer = singleServiceContainer.get("misFileStatusCacheServer");
        misFileStatusCacheServerServiceContainer.stopService();
        singleFileStatusCacheService.setServiceContainer(misFileStatusCacheServerServiceContainer);
        misFileStatusCacheServerServiceContainer.setService(singleFileStatusCacheService);

        ServiceContainer misDbFileListerServiceContainer = singleServiceContainer.get("misDbFileLister");
        misDbFileListerServiceContainer.stopService();
        databaseFileLister.setServiceContainer(misDbFileListerServiceContainer);
        misDbFileListerServiceContainer.setService(databaseFileLister);

        ServiceContainer misFileLoaderServiceContainer = singleServiceContainer.get("misFileLoader");
        misFileLoaderServiceContainer.stopService();
        misFileLoader.setServiceContainer(misFileLoaderServiceContainer);
        misFileLoaderServiceContainer.setService(misFileLoader);

        fileCache.addLoader(misFileLoader);

        ServiceContainer misFileCacheServiceContainer = singleServiceContainer.get("misFileCache");
        misFileCacheServiceContainer.stopService();
        fileCache.setServiceContainer(misFileCacheServiceContainer);
        misFileCacheServiceContainer.setService(fileCache);

        ServiceContainer misFileServiceServiceContainer = singleServiceContainer.get("misFileService");
        misFileServiceServiceContainer.stopService();
        fileService.setServiceContainer(misFileServiceServiceContainer);
        misFileServiceServiceContainer.setService(fileService);

        //
        FileCopyHelper.reliableCopy(new File("src/test/resources/data/Детальная архитектура v-7.docx"), new File("test/tmp/directory/MisFileServiceTest/mbrList/Детальная архитектура v-7.docx"));
        //

        File inboxFolder = new File("test/tmp/directory/MisFileServiceTest/inboxFolder");
        File etalonFolder = new File("test/tmp/directory/MisFileServiceTest/mbrList");
        File[] etalonFiles = etalonFolder.listFiles();
        for (int j = 0; j < etalonFiles.length; j++) {
            File etalonFile = etalonFiles[j];
            FileCopyHelper.reliableCopy(etalonFile, new File(inboxFolder, etalonFile.getName()));
        }
        //

        dataPowerNotificationLoggerServiceContainer.startService();
        dataPowerServiceServiceContainer.startService();
        misFileStatusCacheServerServiceContainer.startService();
        misDbFileListerServiceContainer.startService();
        misFileLoaderServiceContainer.startService();
        misFileCacheServiceContainer.startService();
        misFileServiceServiceContainer.startService();

    }

    @After
    public void after() throws ComponentException, IOException {
        serviceManager.stopAll();
        super.after();
        //
        File f = new File("test/tmp/directory/MisFileServiceTest/archiveFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        f = new File("test/tmp/directory/MisFileServiceTest/cacheFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        f = new File("test/tmp/directory/MisFileServiceTest/inboxFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        f = new File("test/tmp/directory/MisFileServiceTest/tempFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        f = new File("test/tmp/directory/MisFileServiceTest/mbrList");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        //

    }

    @Test
    public void test1() throws InterruptedException, IOException {
        final List<String> setContentType = new ArrayList<String>();
        final Map<String, String> addHeader = new HashMap<String, String>();

        final FileInfoList[] fd = new FileInfoList[1];
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = ((InvocationImpl) invocation).getMethod();
                Object[] arguments = ((InvocationImpl) invocation).getArguments();
                if ("setContentType".equals(m.getName())) {
                    setContentType.add(arguments[0].toString());
                }
                if ("addHeader".equals(m.getName())) {
                    addHeader.put(arguments[0].toString(), arguments[1].toString());
                }
                return null;
            }
        });

        ChangesetFileServiceTest.waitForReady("test/tmp/directory/MisFileServiceTest/cacheFolder/1/fileinfo.ready");

        ServletOutputStream sqs = Mockito.mock(ServletOutputStream.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = ((InvocationImpl) invocation).getMethod();
                Object[] arguments = ((InvocationImpl) invocation).getArguments();
                if ("write".equals(m.getName())) {
                    FileInfoList fileDiffList = (FileInfoList) XMLHelper.readXMLFromByteArray((byte[]) arguments[0], FileInfoList.class, FileInfo.class);
                    fd[0] = fileDiffList;
                    if (fileDiffList.getReportStatuses() != null) {
                        if (fileDiffList.getReportStatuses().size() > 0)
                            fd[0].getReportStatuses().get(0).setLastModified("");
                        if (fileDiffList.getReportStatuses().size() > 1)
                            fd[0].getReportStatuses().get(1).setLastModified("");
                    }
                }
                return null;
            }
        });
        doReturn("list").when(request).getParameter("command");
        doReturn("word").when(request).getParameter("app");
        doReturn("1").when(request).getParameter("id");
        doReturn("1").when(request).getParameter("reportId");
        doReturn("0").when(request).getParameter("chunkIndex");
        doReturn("deviceId: # three").when(request).getParameter("deviceId");
        doReturn("name@gmail.ru").when(request).getAttribute("SYNC_USER_NAME");
        doReturn("localhost").when(request).getRemoteAddr();
        doReturn(sqs).when(response).getOutputStream();
        setContentType.clear();
        addHeader.clear();
        fileService.request(request, response);
        String responseObject = fd[0].toString();
        System.out.println(responseObject);
        Assert.assertTrue("Выходные объеты не идентичны!",
                "FileInfoList{statuses=[FileInfo{app='word', id='1', name='Детальная архитектура v-7.docx', caption='null', group='null', dataMD5='0f942a05b305b2ab0558f956579fa12e', lastModified='', chunkCount='3473'}]}".equals(responseObject)
        );

        ServletOutputStream sqs2 = Mockito.mock(ServletOutputStream.class);
        doReturn("data").when(request).getParameter("command");
        doReturn("1").when(request).getParameter("id");
        doReturn(sqs2).when(response).getOutputStream();
        setContentType.clear();
        addHeader.clear();
        fileService.request(request, response);
        String responseHeader = addHeader.get("Content-Disposition");
        System.out.println(responseHeader);
        Assert.assertEquals("attachment; chunkMeta=YXBwPSJ3b3JkIiBpZD0iMSIgY2h1bmtJbmRleD0iMCIgbWQ1PSIwZjk0MmEwNWIzMDViMmFiMDU1OGY5NTY1NzlmYTEyZSIgY2h1bmtNZDU9IjEyOGQ0NzIyMTY3OWQ4ZTllOTZlMDk2NDI4MzIyZjUxIiB1bnppcHBlZENodW5rTWQ1PSIwM2Q1MGM4YzZhZGFmN2VhYTViNjZhNTA0Njk5ZDljMCIgdW56aXBwZWRMZW5ndGg9IjEwMjQiIHVuemlwcGVkT2Zmc2V0PSIwIg==", responseHeader);

    }

}
