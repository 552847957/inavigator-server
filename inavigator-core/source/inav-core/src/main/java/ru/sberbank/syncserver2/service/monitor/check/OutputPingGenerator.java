package ru.sberbank.syncserver2.service.monitor.check;

import org.apache.commons.io.FileUtils;

import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 26.04.14.
 */
public class OutputPingGenerator extends AbstractCheckAction {
    private String folder;

    public OutputPingGenerator() {
        super();
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    @Override
    protected void doStart() {
        super.doStart();
        FileHelper.createMissingFolders(folder);
    }

    @Override
    public List<? extends ICheckResult> doCheck() {
        //1. Creating a folder
        FileCopyHelper.loggableMkdirs(new File(folder));

        //2. Parsing properties
        //String folder  = config.getPropertyValue(prefix+".folder"); //"Z:/OUT/prod/monitoring/ping/"; //
        String hostname  = LOCAL_HOST_NAME;

        //3. Generating pong file
        File file = new File(folder, hostname);
        if(!file.exists()){
            try {
                FileUtils.writeStringToFile(file, hostname);
                return Arrays.asList(new CheckResult(true,""));
            } catch (IOException e) {
                e.printStackTrace();
                return Arrays.asList(new CheckResult(false,"Failed to created a ping file "+file.getAbsolutePath()));
            }
        } else {
            return Arrays.asList(getLastCheckResult());
        }
    }
    
    @Override
    public String getDescription() {
    	return "Чекер для пинга с помощью файла через ФП";
    }
}
