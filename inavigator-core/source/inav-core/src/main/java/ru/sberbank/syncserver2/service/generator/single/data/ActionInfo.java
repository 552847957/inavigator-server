package ru.sberbank.syncserver2.service.generator.single.data;

import java.util.HashMap;
import java.util.Map;

import ru.sberbank.syncserver2.service.generator.single.OneCallablePerTagThreadPool;

/**
 * Created by Admin on 06.04.14.
 */
public class ActionInfo implements Comparable{
    private String application;
    private String dataFileName;
    private String jndi;
    private boolean autoRun;
    private int  runStatus;
    private ETLAction action;
    private boolean hasArchiveFile;
    private boolean cancelled;
    private boolean autoGenEnabled;
    
    private boolean isGenerationModeDraft;
    private PublishStatus publishedStatus;
    

    private Map<String,ActionState> actionGenStates;
    private Map<String,ActionState> actionLoadStates;
    
    public ActionInfo(String jndi, String application, String dataFileName, boolean automatic, int runStatus, boolean hasArchiveFile) {
        this.application = application;
        this.jndi = jndi;
        this.dataFileName = dataFileName;
        this.autoRun = automatic;
        this.runStatus = runStatus;
        this.hasArchiveFile = hasArchiveFile;
        actionGenStates = new HashMap<String,ActionState>();
        actionLoadStates = new HashMap<String,ActionState>();
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getJndi() {
        return jndi;
    }

    public void setJndi(String jndi) {
        this.jndi = jndi;
    }

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public boolean isAutoRun() {
        return autoRun;
    }

    public void setAutoRun(boolean autoRun) {
        this.autoRun = autoRun;
    }

    public int getRunStatus() {
        return runStatus;
    }

    public String getRunStatusDesc(){
        switch (runStatus){
            case OneCallablePerTagThreadPool.RUN_STATUS.NONE:       return "Ожидание работы";
            case OneCallablePerTagThreadPool.RUN_STATUS.IN_QUEUE:   return "В очереди";
            case OneCallablePerTagThreadPool.RUN_STATUS.RUNNING:    return "Выполняется";
            case OneCallablePerTagThreadPool.RUN_STATUS.CANCELLING: return "Отменяется";
        }
        return "";
    }

    public boolean isHasArchiveFile() {
        return hasArchiveFile;
    }

    public ETLAction getAction() {
        return action;
    }

    public void setAction(ETLAction action) {
        this.action = action;
    }

    @Override
    public int compareTo(Object o) {
        //1. Casting
        ActionInfo another = (ActionInfo) o;

        //2. Compare by app
        String anotherApplication = another.getApplication()==null ? "":another.getApplication();
        String thisApplication = application==null ? "":application;
        int compareApp = thisApplication.compareTo(anotherApplication);
        if(compareApp!=0){
            return compareApp;
        }

        //3. Compare by file name
        String thisFileName = this.dataFileName;
        String anotherFileName = another.dataFileName;
        return thisFileName.compareTo(anotherFileName);
    }

    public String getJndiLastComponent() {
        if(jndi==null){
            return "";
        }
        int lastIndex = jndi.lastIndexOf('/');
        if(lastIndex==-1){
            return jndi;
        }
        return jndi.substring(lastIndex+1);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

	public boolean isAutoGenEnabled() {
		return autoGenEnabled;
	}

	public void setAutoGenEnabled(boolean autoGenEnabled) {
		this.autoGenEnabled = autoGenEnabled;
	}
    
    public Map<String, ActionState> getActionGenStates() {
		return actionGenStates;
	}

	public void setActionGenStates(Map<String, ActionState> actionGenStates) {
		this.actionGenStates = actionGenStates;
	}

	public Map<String, ActionState> getActionLoadStates() {
		return actionLoadStates;
	}

	public void setActionLoadStates(Map<String, ActionState> actionLoadStates) {
		this.actionLoadStates = actionLoadStates;
	}
    
    public boolean isGenerationModeDraft() {
		return isGenerationModeDraft;
	}

	public void setGenerationModeDraft(boolean isGenerationModeDraft) {
		this.isGenerationModeDraft = isGenerationModeDraft;
	}

	public PublishStatus getPublishedStatus() {
		return publishedStatus;
	}

	public void setPublishedStatus(PublishStatus publishedStatus) {
		this.publishedStatus = publishedStatus;
	}	
	
	
}
