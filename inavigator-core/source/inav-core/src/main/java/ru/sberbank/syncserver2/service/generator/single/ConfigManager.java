package ru.sberbank.syncserver2.service.generator.single;

import org.apache.log4j.Logger;

import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.core.config.StaticFileInfo;
import ru.sberbank.syncserver2.service.generator.single.data.*;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by sbt-kozhinsky-lb on 31.03.14.
 */
public class ConfigManager {
    private static final Logger log = Logger.getLogger(ConfigManager.class);

    private String inboxFolder;
    private String workFolder;
    private TreeMap<String,ETLAction> actions = new TreeMap<String, ETLAction>();
    private Map<String,Long> lastPeriodicalRuns = new HashMap<String, Long>();
    private TaskScheduler taskScheduler;
    private ConfigLoader configLoader;

    public ConfigManager() {
    }

    protected void init(ConfigLoader configLoader) {
        //1. Create missing folders
        FileHelper.createMissingFolders(inboxFolder, workFolder);

        this.configLoader = configLoader;
        
        //2. Getting access to the database and create taskScheduler
        taskScheduler = createNewTaskScheduler(configLoader);
    }

    public TaskScheduler createNewTaskScheduler(ConfigLoader configLoader) {
        return new TaskScheduler(configLoader);
    }

    protected void doStop() {
    }

    public String getInboxFolder() {
        return inboxFolder;
    }

    public void setInboxFolder(String inboxFolder) {
        this.inboxFolder = inboxFolder;
    }

    public String getWorkFolder() {
        return workFolder;
    }

    public void setWorkFolder(String workFolder) {
        this.workFolder = workFolder;
    }
    
    

    public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public void loadConfig(){
        //1. Checking inbox for new files
        File[] files = new File(inboxFolder).listFiles();
        if(files!=null){
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if(file.lastModified()>System.currentTimeMillis()-60*1000){
                    continue;
                } else {
                    File dst = new File(workFolder,file.getName());
                    FileCopyHelper.reliableMove(file, dst);
                }
            }
        }

        //2. Loading all tasks from all other files to temporary storage
        files = new File(workFolder).listFiles();
        TreeMap<String,ETLAction> sortedActions = new TreeMap();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            ETLConfig config = ETLConfig.loadConfiFile(file.getAbsolutePath());
            List<ETLAction> actions = config.getActions();
            for (int j = 0; j < actions.size(); j++) {
                ETLAction etlAction = actions.get(j);
                String dataFileName = etlAction.getDataFileName();
                ETLAction previous = sortedActions.put(dataFileName, etlAction);
                if (previous != null) {
                    try {
                        String txt = "Action with datafile dublicated in files " + previous.getConfigFileName() + " and " + etlAction.getConfigFileName()
                                + " . Using action from file " + etlAction.getConfigFileName();
                        throw new RuntimeException(txt);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //3. Copy from temporary storage to permanent storage
        synchronized (this){
            actions = sortedActions;
        }
    }

    public List listActions(){
        ArrayList result = new ArrayList<String>();
        synchronized (this){
            result.addAll(actions.keySet());
        }
        return result;
    }

    public ETLAction getAction(String dataFileName){
        synchronized (this){
            return actions.get(dataFileName);
        }
    }

    public void completeAction(ETLExecutionInfo info){
        ETLAction action = info.getAction();
        String dataFileName = action.getDataFileName();
        //log.debug("trying set lastStartParams for " + dataFileName);
        if(action.getGenerationMode()==GenerationMode.ON_CONDITION){
            taskScheduler.setJobRefreshTimes(action, info.getActualTimes());
        } else if(action.getGenerationMode()==GenerationMode.PERIODICALLY){
            lastPeriodicalRuns.put(dataFileName, info.getLastStartTime());
        }
    }

    
    
    
    public List<ETLExecutionInfo> listActionsForAutomaticRun() {
        //1. Getting copy of actions
        /**
         * It is essential that actions could not be changed during call listActionsForAutomaticRun
         * They could be loaded before, see SingleGeneratorService.doR
         */
        TreeMap<String,ETLAction>  localActions = null;
        synchronized (this){
            localActions = actions;
        }
        List<ETLExecutionInfo> result = new ArrayList<ETLExecutionInfo>();
        Collection<ETLAction> actionColl = localActions.values();
        
        // обновляем информацию из таблицы SYNC_CACHED_STATIC_FILES для определение статуса задания
        // Создаем временное множество для быстрой проверки каждого таска на активность
        Map<String,StaticFileInfo> cachedStaticFileInfo = getEnabledAutoGenStaticFileInfoSet(); 
        
        //2. For every action we check whether it should be automatically started
        for (Iterator<ETLAction> iterator = actionColl.iterator(); iterator.hasNext(); ) {
            //1. Skip action if it is not automatic run
            ETLAction action = iterator.next();
            if(!action.isAutoRun()){
                continue;
            }
            
            // если текущее действие не содержится во множетсве разрешенных действий, то пропускаем автоматический запуск
            if (!cachedStaticFileInfo.containsKey(action.getFullName()) || !cachedStaticFileInfo.get(action.getFullName()).isAutoGenEnabled() )
            	continue;
            
            //2. Check if action should be started
            long now = System.currentTimeMillis();
            switch (action.getGenerationMode()){
                case ON_CONDITION:
                    //2.1. Check if task run checker created
                    if(taskScheduler ==null){
                        //TODO: There should be logging about missing db task run checker
                        continue; //can't check
                    }

                    //2.2. Check it condition pass
                    List<Timestamp> lastRunActualTimes  = taskScheduler.getLastJobDataTimes(action);
                    List<Timestamp> actualTimesInSource = taskScheduler.getDataActualTimes(action);

                    boolean isReady = false;
                    if (actualTimesInSource != null && lastRunActualTimes != null && actualTimesInSource.size() == lastRunActualTimes.size()) {
                        for (int i = 0; i < actualTimesInSource.size(); i++) {
                            Timestamp actualTime = actualTimesInSource.get(i);
                            Timestamp reportTime = lastRunActualTimes.get(i);
                            if (actualTime != null && reportTime != null) {
                                if (actualTime.getTime() > reportTime.getTime()) {
                                    isReady = true;
                                    break;
                                }
                            } else if (actualTime != null && reportTime == null) {
                                isReady = true;
                                break;
                            }
                        }
                    }

                    if (isReady) {
                        ETLExecutionInfo info = new ETLExecutionInfo(action, actualTimesInSource);
                        result.add(info);
                    }

                    break;
                case PERIODICALLY:
                    if (action.getFrequencySeconds() <= 0) {
                        //TODO: There should be logging about invalid frequency
                        continue;
                    }
                    Long lastPeriodicalRunTime = lastPeriodicalRuns.get(action.getDataFileName());
                    if (lastPeriodicalRunTime==null || now > lastPeriodicalRunTime.longValue() + action.getFrequencySeconds() * 1000) {
                        ETLExecutionInfo info = new ETLExecutionInfo(action,new Long(now));
                        result.add(info);
                    }
            }
        }
        return result;
    }
    
    /**
     * Получить множество всех заданий для которых активна автогенерация в SYNC_CACHE_STATIC_FILE  
     * @return
     */
    public Map<String,StaticFileInfo> getEnabledAutoGenStaticFileInfoSet() {
        List<StaticFileInfo> staticFileInfos = configLoader.getStaticFileList();
        HashMap<String,StaticFileInfo> result = new HashMap<String, StaticFileInfo>();
        
        for (StaticFileInfo sfi:staticFileInfos) {
        	result.put(sfi.getFullName(), sfi);
        }
    	
        return result;
    	
    }
}

 