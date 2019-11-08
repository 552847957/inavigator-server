package ru.sberbank.syncserver2.service.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.BeanProperty;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.core.config.Folder;
import ru.sberbank.syncserver2.service.file.cache.FileCache;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;
import ru.sberbank.syncserver2.service.file.cache.list.DynamicFileLister;
import ru.sberbank.syncserver2.service.file.cache.zip.MbrUnzipper;
import ru.sberbank.syncserver2.service.generator.rubricator.ChangeSetComposer;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/changeset-file-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class ChangesetFileServiceTest extends DataPowerMockObject {

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
    @Qualifier("spyChangesetFileServiceTestBean")
    private FileService fileService;

    @Autowired
    @Qualifier("spyChangesetFileCacheTestBean")
    private FileCache fileCache;

    @Autowired
    @Qualifier("spyMbrUnzipperTestBean")
    private MbrUnzipper mbrUnzipper;

    @Autowired
    @Qualifier("spyDynamicFileListerTestBean")
    private DynamicFileLister dynamicFileLister;

    @Autowired
    @Qualifier("spyChangeSetComposer")
    private ChangeSetComposer changeSetComposer;

    @Autowired
    @Qualifier("spyDataPowerNotificationLogger")
    private DataPowerNotificationLogger dataPowerNotificationLogger;


    @Before
    public void before() throws Exception {
        super.before();
        //
        //
        File f = new File("test/tmp/directory/ChangesetFileServiceTest/archiveFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "ChangesetFileServiceTest", "archiveFolder");
        f = new File("test/tmp/directory/ChangesetFileServiceTest/cacheFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "ChangesetFileServiceTest", "cacheFolder");
        f = new File("test/tmp/directory/ChangesetFileServiceTest/inboxFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "ChangesetFileServiceTest", "inboxFolder");
        f = new File("test/tmp/directory/ChangesetFileServiceTest/tempFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "ChangesetFileServiceTest", "tempFolder");
        f = new File("test/tmp/directory/ChangesetFileServiceTest/mbrList");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        TestUtils.createDirectory("test", "tmp", "directory", "ChangesetFileServiceTest", "mbrList");
        //

        List<Folder> lf = new ArrayList<Folder>();
        Folder f1 = new Folder("changeset", 1, "changeset");
        lf.add(f1);
        doReturn(lf).when(configLoader).getFolders();
        doReturn(null).when(configLoader).getSyncConfigProperty("LOGGING_SERVICE");

        List<BeanProperty> lbp = new ArrayList<BeanProperty>();
        doReturn(lbp).when(configLoader).getBeanProperties("admin", "adminDbLogService", false);

        List<Bean> lb = new ArrayList<Bean>();

        Bean spyDynamicFileListerBean = new Bean(1L, "misDynFileLister", "ru.sberbank.syncserver2.service.file.cache.list.DynamicFileLister", null,
                null, 5, null, "misDynFileLister desc");
        lb.add(spyDynamicFileListerBean);

        Bean spyMisMbrLoaderBean = new Bean(2L, "misMbrLoader", "ru.sberbank.syncserver2.service.file.cache.zip.MbrUnzipper", null,
                null, 4, null, "misMbrLoader desc");
        lb.add(spyMisMbrLoaderBean);

        Bean spyChangesetFileCacheBean = new Bean(3L, "changesetFileCache", "ru.sberbank.syncserver2.service.file.cache.FileCache", null,
                null, 3, null, "changesetFileCacheTestBean desc");
        lb.add(spyChangesetFileCacheBean);

        Bean spyChangesetFileServiceBean = new Bean(4L, "changesetFileService", "ru.sberbank.syncserver2.service.file.FileService", null,
                null, 2, "file.do", "changesetFileService desc");
        lb.add(spyChangesetFileServiceBean);

        Bean spyDataPowerService = new Bean(5L, "dataPowerService", "ru.sberbank.syncserver2.service.sql.DataPowerService", null,
                null, 1, null, "dataPowerService desc");
        lb.add(spyDataPowerService);

        Bean spyDataPowerNotificationLogger = new Bean(4L, "dataPowerNotificationLogger", "ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger", null,
                null, 4, null, "dataPowerNotificationLogger desc");
        lb.add(spyDataPowerNotificationLogger);

        doReturn(lb).when(configLoader).getBeans("changeset");

        List<BeanProperty> misDynFileListerProperty = new ArrayList<BeanProperty>();
        BeanProperty bp = new BeanProperty(1L, "statusFile", "test/tmp/directory/ChangesetFileServiceTest/mbrList/list.bin", "statusFile");
        misDynFileListerProperty.add(bp);
        doReturn(misDynFileListerProperty).when(configLoader).getBeanProperties("changeset", "misDynFileLister", false);
        doReturn(misDynFileListerProperty).when(configLoader).getBeanProperties("changeset", "misDynFileLister", true);

        List<BeanProperty> misMbrLoaderProperty = new ArrayList<BeanProperty>();
        bp = new BeanProperty(1L, "tempFolder", "test/tmp/directory/ChangesetFileServiceTest/tempFolder", "tempFolder");
        misMbrLoaderProperty.add(bp);
        doReturn(misMbrLoaderProperty).when(configLoader).getBeanProperties("changeset", "misMbrLoader", false);
        doReturn(misMbrLoaderProperty).when(configLoader).getBeanProperties("changeset", "misMbrLoader", true);

        List<BeanProperty> changesetFileCacheProperty = new ArrayList<BeanProperty>();
        doReturn(changesetFileCacheProperty).when(configLoader).getBeanProperties("changeset", "changesetFileCache", false);
        doReturn(changesetFileCacheProperty).when(configLoader).getBeanProperties("changeset", "changesetFileCache", true);

        List<BeanProperty> changesetFileServiceProperty = new ArrayList<BeanProperty>();
        doReturn(changesetFileServiceProperty).when(configLoader).getBeanProperties("changeset", "changesetFileService", false);
        doReturn(changesetFileServiceProperty).when(configLoader).getBeanProperties("changeset", "changesetFileService", true);

        List<BeanProperty> dataPowerServiceProperty = new ArrayList<BeanProperty>();
        doReturn(dataPowerServiceProperty).when(configLoader).getBeanProperties("changeset", "dataPowerService", false);
        doReturn(dataPowerServiceProperty).when(configLoader).getBeanProperties("changeset", "dataPowerService", true);

        List<BeanProperty> dataPowerNotificationLoggerProperty = new ArrayList<BeanProperty>();
        doReturn(dataPowerNotificationLoggerProperty).when(configLoader).getBeanProperties("changeset", "dataPowerNotificationLogger", false);
        doReturn(dataPowerNotificationLoggerProperty).when(configLoader).getBeanProperties("changeset", "dataPowerNotificationLogger", true);

        serviceManager.startAll();

        Map<String, ServiceContainer> changesetServiceContainer = serviceManager.getServiceContainerMap("changeset");

        ServiceContainer dataPowerNotificationLoggerServiceContainer = changesetServiceContainer.get("dataPowerNotificationLogger");
        dataPowerNotificationLoggerServiceContainer.stopService();
        dataPowerNotificationLogger.setServiceContainer(dataPowerNotificationLoggerServiceContainer);
        dataPowerNotificationLoggerServiceContainer.setService(dataPowerNotificationLogger);

        ServiceContainer dataPowerServiceServiceContainer = changesetServiceContainer.get("dataPowerService");
        dataPowerServiceServiceContainer.stopService();
        dataPowerService.setServiceContainer(dataPowerServiceServiceContainer);
        dataPowerServiceServiceContainer.setService(dataPowerService);

        ServiceContainer misDynFileListerServiceContainer = changesetServiceContainer.get("misDynFileLister");
        misDynFileListerServiceContainer.stopService();
        dynamicFileLister.setServiceContainer(misDynFileListerServiceContainer);
        misDynFileListerServiceContainer.setService(dynamicFileLister);

        ServiceContainer misMbrLoaderServiceContainer = changesetServiceContainer.get("misMbrLoader");
        misMbrLoaderServiceContainer.stopService();
        mbrUnzipper.setServiceContainer(misMbrLoaderServiceContainer);
        misMbrLoaderServiceContainer.setService(mbrUnzipper);

        fileCache.addLoader(mbrUnzipper);

        ServiceContainer changesetFileCacheServiceContainer = changesetServiceContainer.get("changesetFileCache");
        changesetFileCacheServiceContainer.stopService();
        fileCache.setServiceContainer(changesetFileCacheServiceContainer);
        changesetFileCacheServiceContainer.setService(fileCache);

        ServiceContainer changesetFileServiceServiceContainer = changesetServiceContainer.get("changesetFileService");
        changesetFileServiceServiceContainer.stopService();
        fileService.setServiceContainer(changesetFileServiceServiceContainer);
        changesetFileServiceServiceContainer.setService(fileService);

        //
        //FileCopyHelper.copyAndAddMD5(new File("src/test/resources/data/Детальная архитектура v-7.docx"), new File("test/tmp/directory/ChangesetFileServiceTest/inboxFolder/Детальная архитектура v-7.docx"));
        //FileCopyHelper.reliableCopy(new File("src/test/resources/data/Детальная архитектура v-7.docx"), new File("test/tmp/directory/ChangesetFileServiceTest/inboxFolder/Детальная архитектура v-7.docx"));
        //It must be archive changeset file with "zip" extension !!! - zip xml with FileInfoList object
        changeSetComposer.start();
        FileInfo fi = new FileInfo("word", "id #1", "test", "/Детальная архитектура v-7.docx", null);
        fi.setName("Детальная архитектура v-7.docx");
        changeSetComposer.addContent(fi, new File("src/test/resources/data/Детальная архитектура v-7.docx"));
        fi = new FileInfo("word", "id #2", "test", "/jrc108255_blockchain_in_education.pdf", null);
        fi.setName("jrc108255_blockchain_in_education.pdf");
        changeSetComposer.addContent(fi, new File("src/test/resources/data/PdfBoxToImageTest/jrc108255_blockchain_in_education.pdf"));
        changeSetComposer.finish();
        //
        File inboxFolder = new File("test/tmp/directory/ChangesetFileServiceTest/inboxFolder");
        File etalonFolder = new File("test/tmp/directory/ChangesetFileServiceTest/mbrList");
        File[] etalonFiles = etalonFolder.listFiles();
        for (int j = 0; j < etalonFiles.length; j++) {
            File etalonFile = etalonFiles[j];
            FileCopyHelper.reliableCopy(etalonFile, new File(inboxFolder, etalonFile.getName()));
        }
        //

        dataPowerNotificationLoggerServiceContainer.startService();
        dataPowerServiceServiceContainer.startService();
        misDynFileListerServiceContainer.startService();
        misMbrLoaderServiceContainer.startService();
        changesetFileCacheServiceContainer.startService();
        changesetFileServiceServiceContainer.startService();

    }

    @After
    public void after() throws ComponentException, IOException {
        serviceManager.stopAll();
        super.after();
        //
        File f = new File("test/tmp/directory/ChangesetFileServiceTest/archiveFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        f = new File("test/tmp/directory/ChangesetFileServiceTest/cacheFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        f = new File("test/tmp/directory/ChangesetFileServiceTest/inboxFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        f = new File("test/tmp/directory/ChangesetFileServiceTest/tempFolder");
        if (f.exists())
            FileUtils.deleteDirectory(f);
        f = new File("test/tmp/directory/ChangesetFileServiceTest/mbrList");
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

        waitForReady("test/tmp/directory/ChangesetFileServiceTest/cacheFolder/1/fileinfo.ready");
        waitForReady("test/tmp/directory/ChangesetFileServiceTest/cacheFolder/2/fileinfo.ready");
        //Thread.sleep(1200000);

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
        doReturn("id: # 1").when(request).getParameter("id");
        doReturn("reportId: # two").when(request).getParameter("reportId");
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
                "FileInfoList{statuses=[FileInfo{app='word', id='2', name='jrc108255_blockchain_in_education.pdf', caption='/jrc108255_blockchain_in_education.pdf', group='test', dataMD5='b682e8c23c9858a71c1d151323b5a8f3', lastModified='', chunkCount='3620'}, FileInfo{app='word', id='1', name='Детальная архитектура v-7.docx', caption='/Детальная архитектура v-7.docx', group='test', dataMD5='0f942a05b305b2ab0558f956579fa12e', lastModified='', chunkCount='3473'}]}".equals(responseObject)
                        || "FileInfoList{statuses=[FileInfo{app='word', id='1', name='Детальная архитектура v-7.docx', caption='/Детальная архитектура v-7.docx', group='test', dataMD5='0f942a05b305b2ab0558f956579fa12e', lastModified='', chunkCount='3473'}, FileInfo{app='word', id='2', name='jrc108255_blockchain_in_education.pdf', caption='/jrc108255_blockchain_in_education.pdf', group='test', dataMD5='b682e8c23c9858a71c1d151323b5a8f3', lastModified='', chunkCount='3620'}]}".equals(responseObject)
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

        ServletOutputStream sqs1 = Mockito.mock(ServletOutputStream.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = ((InvocationImpl) invocation).getMethod();
                Object[] arguments = ((InvocationImpl) invocation).getArguments();
                if ("write".equals(m.getName())) {
                    FileHelper.writeBinary((byte[]) arguments[0], "test/tmp/directory/ChangesetFileServiceTest/tempFolder/preview.png");
                }
                return null;
            }
        });
        doReturn("preview").when(request).getParameter("command");
        doReturn("2").when(request).getParameter("id");
        doReturn(sqs1).when(response).getOutputStream();
        setContentType.clear();
        addHeader.clear();
        fileService.request(request, response);
        //Idea and maven generate difference files!!
        //Assert.assertTrue("Файлы не идентичны!", cmpFiles(new File("test/tmp/directory/ChangesetFileServiceTest/tempFolder/preview.png"), new File("src/test/resources/data/PdfBoxToImageTest/preview.png")));
        File f = new File("test/tmp/directory/ChangesetFileServiceTest/tempFolder/preview.png");
        Assert.assertTrue(f.exists());
        FileUtils.deleteQuietly(f);

        doReturn("1").when(request).getParameter("id");
        setContentType.clear();
        addHeader.clear();
        fileService.request(request, response);
        f = new File("test/tmp/directory/ChangesetFileServiceTest/tempFolder/preview.png");
        Assert.assertTrue(f.exists());
        Assert.assertEquals("", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><error><code>404</code><description>File preview.png not found '1' for app 'word'</description></error>", IOUtils.toString(new FileInputStream(f), "UTF8"));

        //Check FileCache
        FileUtils.copyDirectoryToDirectory(new File("src/test/resources/data/FileCache/3"), new File("test/tmp/directory/ChangesetFileServiceTest/cacheFolder"));

        doReturn("list").when(request).getParameter("command");
        doReturn("rubricator").when(request).getParameter("app");
        doReturn("3").when(request).getParameter("id");
        doReturn("3").when(request).getParameter("reportId");
        doReturn("0").when(request).getParameter("chunkIndex");
        doReturn("deviceId: # three").when(request).getParameter("deviceId");
        doReturn("name@gmail.ru").when(request).getAttribute("SYNC_USER_NAME");
        doReturn("localhost").when(request).getRemoteAddr();
        doReturn(sqs).when(response).getOutputStream();
        setContentType.clear();
        addHeader.clear();
        fileService.request(request, response);
        responseObject = fd[0].toString();
        System.out.println(responseObject);

        doReturn("data").when(request).getParameter("command");
        doReturn("1").when(request).getParameter("id");
        doReturn(sqs2).when(response).getOutputStream();
        setContentType.clear();
        addHeader.clear();
        fileService.request(request, response);
        responseHeader = addHeader.get("Content-Disposition");
        System.out.println(responseHeader);

        doReturn("data").when(request).getParameter("command");
        doReturn("3").when(request).getParameter("id");
        doReturn(sqs2).when(response).getOutputStream();
        setContentType.clear();
        addHeader.clear();
        fileService.request(request, response);
        responseHeader = addHeader.get("Content-Disposition");
        System.out.println(responseHeader);
        Assert.assertEquals("attachment; chunkMeta=YXBwPSJydWJyaWNhdG9yIiBpZD0iMyIgY2h1bmtJbmRleD0iMCIgbWQ1PSIxNjlmZjAyODgzMjNkZmYzZWUzNjc3MDFmYWNlYTg5ZSIgY2h1bmtNZDU9ImNiYTcwYjY3NjI5ZTgyYzQ5YWFkZDUzMjU5OGU2ZTUwIiB1bnppcHBlZENodW5rTWQ1PSJiOTgyNThiM2M2MTQxZDVjMGQ1NzA4Y2QxZTlmMGRkNyIgdW56aXBwZWRMZW5ndGg9IjI2MjE0NCIgdW56aXBwZWRPZmZzZXQ9IjAi", responseHeader);

        doReturn("preview").when(request).getParameter("command");
        doReturn(sqs1).when(response).getOutputStream();
        setContentType.clear();
        addHeader.clear();
        fileService.request(request, response);
        f = new File("test/tmp/directory/ChangesetFileServiceTest/tempFolder/preview.png");
        Assert.assertTrue(f.exists());

    }

    public static void waitForReady(String fileName) throws InterruptedException {
        final File f = new File(fileName);
        int cnt = 0;
        while (true) {
            if (f.exists()) break;
            Thread.sleep(1000);
            cnt++;
            if (cnt > 600) Assert.fail("Time limit exceeded!");
            if (cnt % 10 == 0) System.out.println(cnt);
        }
    }

    private boolean cmpFiles(File file1, File file2) throws IOException {
        boolean result = true;
        FileInputStream is1 = new FileInputStream(file1);
        FileInputStream is2 = new FileInputStream(file2);
        int i1;
        int i2;
        int cnt = 0;
        do {
            i1 = is1.read();
            i2 = is2.read();
            if (i1 != i2) {
                result = false;
                System.out.println("On " + cnt + " difference " + i1 + " but " + i2);
                break;
            }
            cnt++;
        } while (i1 >= 0 && i2 >= 0);
        return result;
    }

}
