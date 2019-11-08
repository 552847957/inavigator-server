package ru.sberbank.syncserver2.service.generator.single;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;
import ru.sberbank.syncserver2.service.generator.single.data.ETLAction;
import ru.sberbank.syncserver2.service.generator.single.data.ETLCheckError;
import ru.sberbank.syncserver2.service.log.DbLogService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.log.TagLogger;
import ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;

import java.io.File;
import java.sql.SQLException;

/**
 * Created by sbt-kozhinsky-lb on 31.03.14.
 */
public abstract class AbstractGenerator {
    private String localFolder;
    protected DbLogService dbLogService;
    protected DatabaseNotificationLogger dbNotificationLogger;
    protected TagLogger tagLogger;
    private AbstractService parentService;

    private static final String GENERATED_FOLDER = "generated";
    private static final String TEMP_FOLDER      = "temp";
    
    public static final String FORCE_PUBLISH_EXT = ".frc";

    public AbstractGenerator() {
        super();
    }

    protected void init() {
        //1. Folder creation
        File localTempFolder = new File(localFolder, TEMP_FOLDER);
        File localGeneratedFolder = new File(localFolder, GENERATED_FOLDER);
        FileHelper.createMissingFolders(localTempFolder.getAbsolutePath(), localGeneratedFolder.getAbsolutePath());

        //2. Dropping all temp files if found
        FileCopyHelper.reliableDeleteFolderContent(localTempFolder);
    }

    public String getLocalFolder() {
        return localFolder;
    }

    public void setLocalFolder(String localFolder) {
        this.localFolder = localFolder;
    }

    public DbLogService getDbLogService() {
        return dbLogService;
    }

    public void setDbLogService(DbLogService dbLogService) {
        //System.out.println("SETTING SERVICE "+dbLogService);
        this.dbLogService = dbLogService;
    }
    
    public TagLogger getTagLogger() {
		return tagLogger;
	}

	public void setTagLogger(TagLogger tagLogger) {
		this.tagLogger = tagLogger;
	}

	public AbstractService getParentService() {
		return parentService;
	}

	public void setParentService(AbstractService parentService) {
		this.parentService = parentService;
	}

	public DatabaseNotificationLogger getDbNotificationLogger() {
		return dbNotificationLogger;
	}

	public void setDbNotificationLogger(
			DatabaseNotificationLogger dbNotificationLogger) {
		this.dbNotificationLogger = dbNotificationLogger;
	}

	public boolean generate(ETLAction action) throws GeneratorException{
        //1. Dropping temporary file name
        File tempFolder = new File(localFolder, TEMP_FOLDER);
        File localDestFolder = new File(localFolder, GENERATED_FOLDER);
        String dataFileName = action.getDataFileName();
        File tempFile = new File(tempFolder, dataFileName);
        FileCopyHelper.reliableDelete(tempFile);
        
        // сбрасываем все собщения для текущего dataFileName
        getDbNotificationLogger().startGenStaticFileEvent(dataFileName);
        
        //2. Generating
        boolean success = false;
        boolean isCanceled = false;
        logEvent(LogEventType.GEN_GENERATION_START, dataFileName,"Starting generation of "+dataFileName,-1,0);
        try {
            success = generateFile(action, tempFile);
        } catch (GeneratorException ge){
            success = false;
            // Добавляем сигнал об ошибке в фазе генерации файла
            if (!ge.isCancelled()) {
            	// Если не было отменой, то сигналим об ошибке
                getDbNotificationLogger().addGenStaticFileEvent(dataFileName, ActionState.PHASE_FILE_GENERATING, ActionState.STATUS_COMPLETED_ERROR);
	        	if (ge.getErrors() != null && tagLogger != null) {
	        		for(ETLCheckError error:ge.getErrors()) {
	        			dbLogService.logObjectEventWithTags(LogEventType.ERROR,dataFileName,new String[]{action.getDataFileName(),"generatorCheckErrors"},error.getErrorDescription());
	        		}
	        	}
            } else {
            	// Если было отменой, то сигналим об отмене
                getDbNotificationLogger().addGenStaticFileEvent(dataFileName, ActionState.PHASE_FILE_GENERATING, ActionState.STATUS_CANCELED_BY_USER);
                isCanceled = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            String text = "Error at generating " + dataFileName+" : "+ExceptionUtils.getStackTrace(e);
            logEvent(LogEventType.ERROR, dataFileName, text,-1,0);
            FileCopyHelper.reliableDelete(tempFile);
            throw new GeneratorException(e, false);
        } finally {
            //3. Moving to local destination
            logEvent(LogEventType.GEN_GENERATION_FINISH, dataFileName, "Finish generating of " + dataFileName,-1,0);
            if(success){
                getDbNotificationLogger().addGenStaticFileEvent(dataFileName, ActionState.PHASE_SENDING_TO_SIGMA, ActionState.STATUS_PERFORM);
                logEvent(LogEventType.GEN_DEBUG, dataFileName, "Start moving " + dataFileName + " to " + localDestFolder,-1,0);
                // В случае если генеарция в чистовик принудительная, то добавляем к расширению информацию об этом.
                File destFile = new File(localDestFolder, dataFileName + (action.isForcePublish()?FORCE_PUBLISH_EXT:""));
                FileCopyHelper.reliableMove(tempFile,destFile);
                logEvent(LogEventType.GEN_DEBUG, dataFileName, "Finish moving " + dataFileName + " to " + localDestFolder,-1,0);
            } else {
                logEvent(LogEventType.GEN_GENERATION_FINISH, dataFileName, "Start deleting temporary file", -1, 0);
                FileCopyHelper.reliableDelete(tempFile);
                logEvent(LogEventType.ERROR, dataFileName, "File " + dataFileName + " was deleted", -1, 0);
                
	        	// В случае любой неудачи, кроме отмены уведомляем об ошибке
            	if (!isCanceled)
            		getDbNotificationLogger().addError("Generating of database \"" + action.getDataFileName() + "\" failed. Please look at generation logs.");
            }
        }
        return success;
    }

    protected abstract boolean generateFile(ETLAction action, File tempFile) throws GeneratorException, SQLException;

    protected void logProgress(String dataFileName, int queryIndex, int queryCount){
        double progress = (queryIndex+1)*1.0/queryCount;
        int percent = (int) (progress*100);
        dbLogService.logObjectEvent(LogEventType.GEN_GENERATION_PROGRESS, dataFileName, "Completed "+percent+"% generation tasks");
    }

    protected void logEvent(LogEventType eventType, String dataFileName, String eventDescription, int queryIndex, int queryCount){
        if(queryIndex>=0){
            eventDescription = "Step #"+(queryIndex+1)+" of "+queryCount+" . "+eventDescription;
        }
        dbLogService.logObjectEvent(eventType, dataFileName, eventDescription);
    }

    public static interface CancelIndicator {
        public boolean isCancelled();
    }
}
