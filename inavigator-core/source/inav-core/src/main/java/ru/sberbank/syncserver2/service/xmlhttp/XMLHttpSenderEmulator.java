package ru.sberbank.syncserver2.service.xmlhttp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

/**
 * Created by sbt-kozhinsky-lb on 02.03.15.
 * This class allows to emulate work of XMLHttpSender but in reality it works with file transporter
 * IT SHOULD NOT BE USED FOR PRODUCTION BECAUSE THERE IS NO CHECJS OF DATA CONSISTENCY
 * It is intended for temporary use.
 */
public class XMLHttpSenderEmulator extends XMLHttpSender {
    private String localFile;
    private String bufferNetworkFolder;
    private String targetNetworkFolder;

    @Override
    protected void doStart() {
        super.doStart();
        File parent = new File(localFile).getParentFile();
        FileHelper.createMissingFolders(parent.getAbsolutePath(), bufferNetworkFolder, targetNetworkFolder);
    }

    public String getLocalFile() {
        return localFile;
    }

    public void setLocalFile(String localFile) {
        this.localFile = localFile;
    }

    public String getBufferNetworkFolder() {
        return bufferNetworkFolder;
    }

    public void setBufferNetworkFolder(String bufferNetworkFolder) {
        this.bufferNetworkFolder = bufferNetworkFolder;
    }

    public String getTargetNetworkFolder() {
        return targetNetworkFolder;
    }

    public void setTargetNetworkFolder(String targetNetworkFolder) {
        this.targetNetworkFolder = targetNetworkFolder;
    }

    @Override
    public Object send(Object requestData) {
        //1. Saving file to buffer
    	tagLogger.log("Saving data to "+localFile);
        File localFileObject = new File(localFile);
        FileCopyHelper.reliableDelete(localFileObject);
        Class[] classes = super.getConfig().getRequestClasses();
        OutputStream out = null;
        try {
            out = new FileOutputStream(localFileObject);
            XMLHelper.writeXMLWithBuffer(out, requestData, false, classes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            FileHelper.close(out);
        }

        //2. Copy file to local folder
        File bufferFileObject = new File(bufferNetworkFolder, localFileObject.getName());
        FileCopyHelper.reliableMove(localFileObject, bufferFileObject);

        //3. Renaming file
        File networkFileObject = new File(targetNetworkFolder, localFileObject.getName());
        bufferFileObject.renameTo(networkFileObject);
    	tagLogger.log("Copied data to "+networkFileObject.getAbsolutePath());
        return null;
    }
}
