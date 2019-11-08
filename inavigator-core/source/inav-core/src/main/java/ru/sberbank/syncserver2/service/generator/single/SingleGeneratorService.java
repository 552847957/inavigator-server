package ru.sberbank.syncserver2.service.generator.single;

import org.apache.commons.io.FileUtils;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.core.config.StaticFileInfo;
import ru.sberbank.syncserver2.service.file.fragments.FileFragmentsTransportHelper;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.generator.single.data.*;
import ru.sberbank.syncserver2.service.log.DbLogService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by sbt-kozhinsky-lb on 31.03.14.
 */
public class SingleGeneratorService extends SingleThreadBackgroundService {
    private ConfigManager configManager;
    private SQLiteGenerator sqliteGenerator;
    private OneCallablePerTagThreadPool threadPool;
    private ThreadLocal notificationLogger = new ThreadLocal();

    private int threadCount;
    private String configHome;
    private String localFileHome;
    private String localArchiveFolder;
    private String networkTempFolder;
    private String networkTargetFolder;
    private String logsStorageTimes;

    public SingleGeneratorService() {
        super(60);
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(String threadCount) {
        this.threadCount = Integer.parseInt(threadCount);
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public String getLocalFileHome() {
        return localFileHome;
    }

    public void setLocalFileHome(String localFileHome) {
        this.localFileHome = localFileHome;
    }

    public String getLocalArchiveFolder() {
        return localArchiveFolder;
    }

    public void setLocalArchiveFolder(String localArchiveFolder) {
        this.localArchiveFolder = localArchiveFolder;
    }

    public String getNetworkTempFolder() {
        return networkTempFolder;
    }

    public void setNetworkTempFolder(String networkTempFolder) {
        this.networkTempFolder = networkTempFolder;
    }

    public String getNetworkTargetFolder() {
        return networkTargetFolder;
    }

    public void setNetworkTargetFolder(String networkTargetFolder) {
        this.networkTargetFolder = networkTargetFolder;
    }

    public String getConfigHome() {
        return configHome;
    }

    public void setConfigHome(String configHome) {
        this.configHome = configHome;
    }


    public String getLogsStorageTimes() {
        return logsStorageTimes;
    }

    public void setLogsStorageTimes(String logsStorageTimes) {
        this.logsStorageTimes = logsStorageTimes;
    }


    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void doInit() {
        //1. Creating config manager
        configManager = createNewConfigManager();
        File inbox = new File(configHome, "inbox");
        File work = new File(configHome, "work");
        configManager.setInboxFolder(inbox.getAbsolutePath());
        configManager.setWorkFolder(work.getAbsolutePath());

        ServiceContainer serviceContainer = super.getServiceContainer();
        ServiceManager serviceManager = serviceContainer.getServiceManager();
        ConfigLoader configLoader = serviceManager.getConfigLoader();
        configManager.init(configLoader); //folders are created here
        configManager.loadConfig();

        //2. Creating sqlite manager
        sqliteGenerator = createSQLiteGenerator();
        sqliteGenerator.setLocalFolder(localFileHome);
        sqliteGenerator.init(); //folders are created here

        DbLogService logService = super.getDbLogger();
        sqliteGenerator.setDbLogService(logService);
        sqliteGenerator.setDbNotificationLogger((DatabaseNotificationLogger) serviceManager.findFirstServiceByClassCode(DatabaseNotificationLogger.class, notificationLogger));
        sqliteGenerator.setTagLogger(tagLogger);
        sqliteGenerator.setParentService(this);

        //3. Creating dest folder
        FileHelper.createMissingFolders(localArchiveFolder);

        //3. Creating thread pool
        threadPool = new OneCallablePerTagThreadPool(threadCount);
    }

    public ConfigManager createNewConfigManager() {
        return new ConfigManager();
    }

    public SQLiteGenerator createSQLiteGenerator() {
        return new SQLiteGenerator();
    }

    @Override
    public void doRun() {
        //1. Check if node is active
        ClusterManager clusterManager = (ClusterManager) ServiceManager.getInstance().findFirstServiceByClassCode(ClusterManager.class);
        if (clusterManager != null && !clusterManager.isActive()) {
            return;
        }

        //2. Reload configuration
        configManager.loadConfig();

        //3. Set log service to generator
        DbLogService logService = super.getDbLogger();
        sqliteGenerator.setDbLogService(logService);

        //4. Listing actions to generate files
        List<ETLExecutionInfo> actions = configManager.listActionsForAutomaticRun();

        //5. Schedule execution
        for (int i = 0; i < actions.size(); i++) {
            ETLExecutionInfo info = actions.get(i);
            ETLAction action = info.getAction();
            String dataFileName = action.getDataFileName();
            SQLiteTask task = new SQLiteTask(info);

            if (threadPool.submit(dataFileName, task)) {
                int id = AuditHelper.writeSystemGen("Запуск генерации (автоматическая)", dataFileName);
                if (id > -1)
                    logObjectEvent(LogEventType.OTHER, dataFileName, "Adding audit record with id " + id);
                logObjectEvent(LogEventType.GEN_QUEUED, dataFileName, "Adding task for generation of " + dataFileName + " in queue");
            }
        }
    }

    @Override
    protected void doStop() {
        super.doStop();
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

    public void manualRun(String dataFileName) {
        manualRun(dataFileName, false);
    }

    public void manualRun(String dataFileName, boolean isForcePublish) {
        //1. Check run status - if generation is not running then we drop old logs
        //   TODO: it is APROXIMATELY OK - it should be fixed.
       /* int runStatus = threadPool.getRunStatus(dataFileName);
        if(runStatus!=OneCallablePerTagThreadPool.RUN_STATUS.RUNNING){
        	synchronized (this) {
	            String dropOldLogsSQL = "DELETE FROM SYNC_LOGS WHERE EVENT_INFO='"+dataFileName+"'";
	            DbLogService dbLogService = getDbLogger();
	            dbLogService.executeSQL(dropOldLogsSQL);
        	}
        } */

        //2. Starting
        ETLAction action = configManager.getAction(dataFileName);
        action.setForcePublish(isForcePublish);
        SQLiteTask task = new SQLiteTask(action);
        if (!threadPool.submit(dataFileName, task)) {
            logObjectEvent(LogEventType.ERROR, dataFileName, "Generation for " + dataFileName + " is already running. Failed to start second generation ");
        }

        //3. Wait a second for a start if there is no other tasks
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopGeneration(String dataFileName) {
        threadPool.cancel(dataFileName);
    }

    public void copyLastGeneratedAgain(String dataFileName) {
        logObjectEvent(LogEventType.GEN_TRANSFER_START, dataFileName, "Start moving " + dataFileName + " from " + localArchiveFolder + " to " + networkTempFolder);
        File src = new File(localArchiveFolder, dataFileName);
        File tmp = new File(networkTempFolder, dataFileName);
        File dst = new File(networkTargetFolder, dataFileName);
        if (!src.exists()) {
            logObjectEvent(LogEventType.ERROR, dataFileName, "File " + dataFileName + " was not found in " + localArchiveFolder);
        } else {
            if (!src.isDirectory()) {
                try {
                    FileCopyHelper.copyAndAddMD5(src, tmp);
                    tmp.renameTo(dst);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                FileFragmentsTransportHelper.copyFileFragmentToNetworkFolder(src, tmp, dst);
            }
            logObjectEvent(LogEventType.GEN_TRANSFER_START, dataFileName, "Finish moving " + dataFileName + " from " + localArchiveFolder + " to " + networkTargetFolder);
        }
    }

    public void redeployConfigFiles(HttpServletRequest request) {
        //1. Prepare folders
        String rootPath = request.getRealPath("/");
        File sourceFolder = new File(new File(rootPath, "WEB-INF"), "etl");
        File targetFolder = new File(configManager.getInboxFolder());

        //2. Copy content
        tagLogger.log("Start to copy ETLs from " + sourceFolder + " to " + targetFolder);
        try {
            FileUtils.copyDirectory(sourceFolder, targetFolder);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tagLogger.log("Finish to copy ETLs from " + sourceFolder + " to " + targetFolder);
        }
    }

    public int getRunStatus(String dataFileName) {
        return threadPool.getRunStatus(dataFileName);
    }

    public List<String> getDataFileNames() {
        return new ArrayList<String>(configManager.listActions());
    }

    public List<ActionInfo> listActionInfo() {
        //1. Preparing list of action infos

        Map<String, StaticFileInfo> staticFileInfoSet = configManager.getEnabledAutoGenStaticFileInfoSet();

        System.out.println("Listing listActionInfo");
        List<String> dataFileNames = configManager.listActions();
        List<ActionInfo> result = new ArrayList<ActionInfo>();
        for (int i = 0; i < dataFileNames.size(); i++) {
            String dataFileName = dataFileNames.get(i);
            System.out.println("Listing information for " + dataFileName);
            int runStatus = getRunStatus(dataFileName);
            System.out.println("Getting run status for " + dataFileName + " : " + runStatus);
            ETLAction action = configManager.getAction(dataFileName);
            System.out.println("Getting action for " + dataFileName + ": " + action);
            ETLActionPattern pattern = action.getPatternObject();
            String jndi = pattern.getJndi();
            String application = pattern.getApplication();
            boolean isAutoRun = action.isAutoRun();
            System.out.println("Getting autorun for " + dataFileName + ": " + isAutoRun);

            // проверяем - можно ли повторно копировать(файл существует и не заблокирован)
            boolean hasArchiveFile = new File(localArchiveFolder, dataFileName).exists() && !FileFragmentsTransportHelper.isLockFileExists(new File(localArchiveFolder), dataFileName);
            System.out.println("Getting hasArchiveFile for " + dataFileName + ": " + hasArchiveFile);
            ActionInfo info = new ActionInfo(jndi, application, dataFileName, isAutoRun, runStatus, hasArchiveFile);
            StaticFileInfo staticFileInfo = staticFileInfoSet.get(action.getFullName());

            if (staticFileInfo != null) {
                info.setAutoGenEnabled(staticFileInfo.isAutoGenEnabled());

                info.setGenerationModeDraft(staticFileInfo.isGenerationModeDraft());

                if (staticFileInfo.isDraft() && staticFileInfo.isCleanStatus())
                    info.setPublishedStatus(PublishStatus.PUBLISHED_NOT_ACTUAL);
                else if (!staticFileInfo.isDraft() && staticFileInfo.isCleanStatus())
                    info.setPublishedStatus(PublishStatus.PUBLISHED_ACTUAL);
                else if (staticFileInfo.isDraft() && !staticFileInfo.isCleanStatus())
                    info.setPublishedStatus(PublishStatus.DRAFT_ONLY);
                else
                    info.setPublishedStatus(PublishStatus.UNKNOWN);
            } else {
                info.setAutoGenEnabled(false);
                info.setGenerationModeDraft(false);
                info.setPublishedStatus(PublishStatus.UNKNOWN);
            }

            info.setAction(action);
            result.add(info);
        }

        //2. Sorting action infos by application (natural ordering)
        Collections.sort(result);
        return result;
    }

    private void dropOldLogs(String fileName) {
        int storageTimes = 5;
        try {
            storageTimes = Integer.parseInt(logsStorageTimes);
            if (storageTimes < 1)
                storageTimes = 5;
        } catch (NumberFormatException e) {
        }
        DbLogService dbLogService = getDbLogger();
        try {
            dbLogService.dropOldLogs(fileName, storageTimes);
        } catch (Exception e) {
            tagLogger.log("Can't delete old logs");
        }
    }

    private class SQLiteTask implements Callable {
        private ETLExecutionInfo executionInfo;
        private ETLAction action;
        private boolean manual;

        /**
         * Used for manual run
         *
         * @param executionInfo
         */
        private SQLiteTask(ETLExecutionInfo executionInfo) {
            this.executionInfo = executionInfo;
            this.action = executionInfo.getAction();
            this.manual = false;
        }

        /**
         * Used for automatic run
         *
         * @param action
         */
        private SQLiteTask(ETLAction action) {
            this.action = action;
            this.manual = true;
        }

        @Override
        public Object call() throws Exception {
            DatabaseNotificationLogger databaseNotificationLogger = null;
            boolean cancelled = false;
            try {
                ServiceManager serviceManager = getServiceContainer().getServiceManager();
                databaseNotificationLogger = (DatabaseNotificationLogger) serviceManager.findFirstServiceByClassCode(DatabaseNotificationLogger.class, notificationLogger);
                databaseNotificationLogger.addGeneration(action.getDataFileName());
                dropOldLogs(action.getDataFileName());
                sqliteGenerator.generate(action);
            } catch (GeneratorException ge) {
                if (!ge.isCancelled()) {
                    ge.printStackTrace();
                }
                cancelled = true;
            } catch (Exception e) {
                e.printStackTrace();
                cancelled = true;
            } finally {
                if (databaseNotificationLogger != null) {
                    databaseNotificationLogger.delGeneration(action.getDataFileName());
                }
                if (!manual && !cancelled) {
                    configManager.completeAction(executionInfo);
                }
            }
            return null;
        }

    }

    private class SingleCancelIndicator implements AbstractGenerator.CancelIndicator {
        private boolean cancelledFlag = false;
        private String dataFileName;

        private SingleCancelIndicator(String dataFileName) {
            this.dataFileName = dataFileName;
        }

        @Override
        public boolean isCancelled() {
            //logObjectEvent(LogEventType.DEBUG, dataFileName, "Check for cancellation of "+dataFileName);
            if (cancelledFlag) {
                return true;
            } else {
                int runStatus = SingleGeneratorService.this.threadPool.getRunStatus(dataFileName);
                cancelledFlag = runStatus != OneCallablePerTagThreadPool.RUN_STATUS.RUNNING || shouldInternalTaskStop();
                if (cancelledFlag) {
                    logObjectEvent(LogEventType.DEBUG, dataFileName, "Cancelled generation of " + dataFileName);
                }
                return cancelledFlag;
            }
        }
    }
}
