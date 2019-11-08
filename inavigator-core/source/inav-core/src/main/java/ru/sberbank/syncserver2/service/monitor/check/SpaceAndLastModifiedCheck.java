package ru.sberbank.syncserver2.service.monitor.check;

import ru.sberbank.syncserver2.service.log.LogEventType;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 25.04.14.
 */
public class SpaceAndLastModifiedCheck extends AbstractCheckAction {
    public static final long ONE_HOUR_MILLIS = 60*60*1000;

    public static final String CHECK_RESULT_CODE_VALUE_INCLMAXMB_WRONG = "VALUE_INCLMAXMB_WRONG";
    public static final String CHECK_RESULT_CODE_VALUE_INCLMAXHOURS_WRONG = "VALUE_INCLMAXHOURS_WRONG";  
    public static final String CHECK_RESULT_CODE_VALUE_TEMPMAXHOURS_WRONG = "VALUE_TEMPMAXHOURS_WRONG";    
    public static final String CHECK_RESULT_CODE_FILE_TRANSPORTER_FULL = "FILE_TRANSPORTER_FULL";    
    public static final String CHECK_RESULT_CODE_FILE_TRANSPORTER_FOUND_OLD_FILE = "FILE_TRANSPORTER_FOUND_OLD_FILE";    
    
    private String includeFolders  = "";
    private String includeMaxMB    = "8196";
    private String includeMaxHours = "24";

    private String excludeFolders = "";

    private String tempFolders  = "";
    private String tempMaxMB    = "2048";
    private String tempMaxHours = "24";


    public SpaceAndLastModifiedCheck() {
        super();
    }

    public String getIncludeFolders() {
        return includeFolders;
    }

    public void setIncludeFolders(String includeFolders) {
        this.includeFolders = includeFolders;
    }

    public String getIncludeMaxMB() {
        return includeMaxMB;
    }

    public void setIncludeMaxMB(String includeMaxMB) {
        this.includeMaxMB = includeMaxMB;
    }

    public String getIncludeMaxHours() {
        return includeMaxHours;
    }

    public void setIncludeMaxHours(String includeMaxHours) {
        this.includeMaxHours = includeMaxHours;
    }

    public String getExcludeFolders() {
        return excludeFolders;
    }

    public void setExcludeFolders(String excludeFolders) {
        this.excludeFolders = excludeFolders;
    }

    public String getTempFolders() {
        return tempFolders;
    }

    public void setTempFolders(String tempFolders) {
        this.tempFolders = tempFolders;
    }

    public String getTempMaxMB() {
        return tempMaxMB;
    }

    public void setTempMaxMB(String tempMaxMB) {
        this.tempMaxMB = tempMaxMB;
    }

    public String getTempMaxHours() {
        return tempMaxHours;
    }

    public void setTempMaxHours(String tempMaxHours) {
        this.tempMaxHours = tempMaxHours;
    }

    @Override
    public List<CheckResult> doCheck() {
    	List<CheckResult> results = new ArrayList<CheckResult>();
        //1. Reading properties
        int includeMaxMB      = 8196;
        int includeMaxHours   = 24;
        int tempMaxHours      = 24;
        try {
            includeMaxMB      = Integer.parseInt(this.includeMaxMB);
            addSuccessfullCheckResultIfPreviousFailed(results, CHECK_RESULT_CODE_VALUE_INCLMAXMB_WRONG,"The value for property includeMaxMB is correct");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            results.add(new CheckResult(CHECK_RESULT_CODE_VALUE_INCLMAXMB_WRONG,false,"The value for property includeMaxMB is wrong"));
        }
        try {
            includeMaxHours   = Integer.parseInt(this.includeMaxHours);
            addSuccessfullCheckResultIfPreviousFailed(results, CHECK_RESULT_CODE_VALUE_INCLMAXHOURS_WRONG,"The value for property includeMaxHours is correct");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            results.add(new CheckResult(CHECK_RESULT_CODE_VALUE_INCLMAXHOURS_WRONG,false,"The value for property includeMaxHours is wrong"));
        }
        try {
            tempMaxHours      = Integer.parseInt(this.tempMaxHours);
            addSuccessfullCheckResultIfPreviousFailed(results, CHECK_RESULT_CODE_VALUE_TEMPMAXHOURS_WRONG,"The value for property tempMaxHours is correct");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            results.add(new CheckResult(CHECK_RESULT_CODE_VALUE_TEMPMAXHOURS_WRONG,false,"The value for property tempMaxHours is wrong"));
        }
        /*
        String includeFolders = config.getPropertyValue(prefix+".includes.folders"); //Z:/
        int includeMaxMB      = config.getPropertyValueAsInteger(prefix+".maxMB"   ,8196);
        int includeMaxHours   = config.getPropertyValueAsInteger(prefix+".maxHours" ,24);      //24

        String excludeFolders = config.getPropertyValue(prefix+".excludes.folders"); //"Z:/IN/prod/mbr/mbr_december_for_mis;z:\\OUT\\temp;z:\\IN\\temp";

        String tempFolders = config.getPropertyValue(prefix+".temps.folders"); //"Z:/IN/temp;Z:/OUT/temp"; //
        int tempMaxMB      = config.getPropertyValueAsInteger(prefix+".temps.maxMB", 2028);   //"5000"; //
        int tempMaxHours   = config.getPropertyValueAsInteger(prefix+".temps.maxHours", 24);   //"5000"; //
        */

        //2. Parsing properties
        //2.1. Parsing folders - includes, excludes and temps
        String[] includeNames = includeFolders.split(";");
        File[] includes = new File[includeNames.length];
        for (int i = 0; i < includeNames.length; i++) {
            includeNames[i] = includeNames[i].toLowerCase();
            includes[i]=new File(includeNames[i]);
        }
        String[] excludeNames = excludeFolders.split(";");
        File[] excludes = new File[excludeNames.length];
        for (int i = 0; i < excludes.length; i++) {
            excludes[i] = new File(excludeNames[i]);
        }
        String[] tempNames = tempFolders.split(";");
        File[] temps = new File[tempNames.length];
        for (int i = 0; i < tempNames.length; i++) {
            temps[i] = new File(tempNames[i]);
        }

        //3. Removing old temp files
        long minLastModified = System.currentTimeMillis()-tempMaxHours*ONE_HOUR_MILLIS;
        for (int i = 0; i < temps.length; i++) {
            File temp = temps[i];
            removeOldTempFiles(temp,minLastModified);
        }

        //4. Calculating space used, minimum date and make a check
        //4.1. Calculate
        FolderInfo totalInfo = new FolderInfo();
        for (int i = 0; i < includes.length; i++) {
            FolderInfo folderInfo = getFolderInfo(includes[i], excludes);
            totalInfo.add(folderInfo);
        }

        //4.2 Make a check
        tagLogger.log("Результат проверки: "+totalInfo);
        /*if(totalInfo.isEmpty()){
        	// очищаем все результаты проверок, т.к. считается что все старые ошибки уже неактуальны. 
        	clearAllCheckResults();
        	results.add(new CheckResult(true,""));
        }*/
        int totalMB = (int) (totalInfo.length / 1024 / 1024);
        if(totalMB > includeMaxMB){
        	String txt = "File transporter is full: total file size is "+totalMB+"Mb and it is more than "+includeMaxMB+"Mb";
        	results.add(new CheckResult(CHECK_RESULT_CODE_FILE_TRANSPORTER_FULL,false,LOCAL_HOST_NAME+" says at "+new Date()+": "+txt));
        	writeFailedToLog(CHECK_RESULT_CODE_FILE_TRANSPORTER_FULL, "Файлоперекладчик заполнен: общий размер папки "+totalMB+"Mb и это больше максимального "+includeMaxMB+"Mb");
        } else {
        	String mess = "File Transporter is not full: total file size is "+totalMB+" Mb and it is less then allowed "+includeMaxMB+"Mb";
            if (addSuccessfullCheckResultIfPreviousFailed(results, CHECK_RESULT_CODE_FILE_TRANSPORTER_FULL,LOCAL_HOST_NAME+" says at "+new Date()+": "+mess)) {
            	tagLogger.log("На файлоперекладчике есть свободное место: общий размер папки "+totalMB+"Mb и это меньше, чем "+includeMaxMB+"Mb");
            }
        }
        if(totalInfo.minLastModified<System.currentTimeMillis()-includeMaxHours*ONE_HOUR_MILLIS){
            String fileName = totalInfo.oldest==null ? "":": "+totalInfo.oldest.getAbsolutePath();
            String mess = "Found file older than "+includeMaxHours+" hours before, see "+fileName;
            results.add(new CheckResult(CHECK_RESULT_CODE_FILE_TRANSPORTER_FOUND_OLD_FILE,false, LOCAL_HOST_NAME+" says at "+new Date()+": "+mess));
            tagLogger.log("Найден файл старше "+includeMaxHours+" часов: "+fileName);
        } else
            if (addSuccessfullCheckResultIfPreviousFailed(results, CHECK_RESULT_CODE_FILE_TRANSPORTER_FOUND_OLD_FILE,LOCAL_HOST_NAME+" says at "+new Date()+": No files found older than "+includeMaxHours+" hours")) {
            	tagLogger.log("Нет файлов старше "+includeMaxHours+" часов");
            }
        
        
        return results;
    }

    private FolderInfo getFolderInfo(File root, File[] excludes) {
        //1. Check if folder should be excluded
        FolderInfo folderInfo = new FolderInfo();
        //System.out.println("START CALCULATED "+root.getAbsolutePath());
        for (int i = 0; i < excludes.length; i++) {
            if(root.equals(excludes[i])){
                //System.out.println("EXCLUDED "+excludes[i].getAbsolutePath()+" FROM CALCULATION");
                return folderInfo;
            }
        }

        //2. Calculated file size
        File[] files = root.listFiles();
        if(files!=null){
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if(file.isDirectory()){
                    FolderInfo childInfo = getFolderInfo(file,excludes);
                    folderInfo.add(childInfo);
                } else {
                    folderInfo.add(file);
                }
            }
        }
        return folderInfo;
    }


    private void removeOldTempFiles(File root, long minLastModified) {
        //1. Listing files
        File[] files = root.listFiles();
        if(files==null){
            return;
        }

        //2. Deleting to new files
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file.isDirectory()){
                removeOldTempFiles(file, minLastModified);
            } else {
                if(root.lastModified()<minLastModified){
                    dbLogger.logObjectEvent(LogEventType.DEBUG,getClass().getSimpleName(), "Do not notify about "+String.valueOf(file.getAbsolutePath())+" to be removed");
                    file.delete();
                    //System.out.println("FILE SHOULD BE REMOVED: " + file.getAbsolutePath());
                }
            }
        }
    }

    private static class FolderInfo {
        private long length = 0;
        private long minLastModified = Long.MAX_VALUE;
        private File oldest = null;

        private void add(File file){
            length += file.length();
            long fileLastModified =file.lastModified();
            if(file.exists() && fileLastModified<minLastModified){
                minLastModified = fileLastModified;
                this.oldest = file;
                //System.out.println("FOUND OLDEST "+oldest.getAbsolutePath());
            }
        }

        private void add(FolderInfo folderInfo){
            length +=folderInfo.length;
            if(folderInfo.minLastModified<this.minLastModified){
                this.minLastModified = folderInfo.minLastModified;
                this.oldest = folderInfo.oldest;
                //System.out.println("FOUND OLDEST "+oldest.getAbsolutePath());
            }
        }

        private boolean isEmpty(){
            return length==0;
        }

        @Override
        public String toString() {
            return "FolderInfo{" +
                    "общий размер =" + length/1024/1024 + "Mb"+
                    (oldest==null ? "}" : ", самый старый файл: "+oldest.getAbsolutePath()+" изменен "+ new Date(minLastModified)+ "}");
        }
    }
    
    @Override
    public String getDescription() {
    	return "Чекер для проверки ФП на наличие места и старых файлов";
    }

    public static void main(String[] args) {
        //SpaceAndLastModifiedCheck check = new SpaceAndLastModifiedCheck();
        //String prefix = "check.space-and-last-modified";
        //CheckResult result = check.check(null, prefix);
    }
}
