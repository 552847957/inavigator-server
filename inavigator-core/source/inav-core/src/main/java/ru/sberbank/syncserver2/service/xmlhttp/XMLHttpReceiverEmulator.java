package ru.sberbank.syncserver2.service.xmlhttp;

import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

import java.io.File;

/**
 * Created by sbt-kozhinsky-lb on 02.03.15.
 */
public class XMLHttpReceiverEmulator extends SingleThreadBackgroundService {
    private String networkFile;
    private String localFolder;

    private String listenerBeanCode;
    private XMLHttpReceiverListener listener;
    private String configBeanCode;
    private XMLHttpJAXBConfig config;

    public XMLHttpReceiverEmulator() {
        super(15);
    }

    public String getNetworkFile() {
        return networkFile;
    }

    public void setNetworkFile(String networkFile) {
        this.networkFile = networkFile;
    }

    public String getLocalFolder() {
        return localFolder;
    }

    public void setLocalFolder(String localFolder) {
        this.localFolder = localFolder;
    }

    public String getListenerBeanCode() {
        return listenerBeanCode;
    }

    public void setListenerBeanCode(String listenerBeanCode) {
        this.listenerBeanCode = listenerBeanCode;
    }

    public String getConfigBeanCode() {
        return configBeanCode;
    }

    public void setConfigBeanCode(String configBeanCode) {
        this.configBeanCode = configBeanCode;
    }

    @Override
    public void doInit() {
        File parent = new File(networkFile).getParentFile();
        FileHelper.createMissingFolders(parent.getAbsolutePath(),localFolder);
    }

    @Override
    public void doRun() {
        //2. Copy file to local folder
        File networkFileObject = new File(networkFile);
        String name = networkFileObject.getName();
        if(!networkFileObject.exists()){
            tagLogger.log("File "+networkFile+" was NOT found");
            return;
        } else {
            tagLogger.log(name,"File "+networkFile+" was found");
        }
        File localFileObject = new File(localFolder, networkFileObject.getName());
        FileCopyHelper.reliableMove(networkFileObject, localFileObject);
        tagLogger.log(name,"Moved "+networkFile+" to "+localFileObject);

        //2. Finding receiver objects
        if(listener==null){
            ServiceManager sm = ServiceManager.getInstance();
            ServiceContainer sc  = sm.findServiceByBeanCode(listenerBeanCode);
            listener = (XMLHttpReceiverListener) sc.getService();
        }
        if(config==null){
            ServiceManager sm = ServiceManager.getInstance();
            ServiceContainer sc  = sm.findServiceByBeanCode(configBeanCode);
            config = (XMLHttpJAXBConfig) sc.getService();
        }


        //3. Reading file
        tagLogger.log(name,"Start loading file "+networkFile);
        Object data = XMLHelper.readXML(localFileObject, config.getRequestClasses());
        tagLogger.log(name, "Finish loading file "+networkFile);
        listener.receive(data);
        tagLogger.log(name, "Finish calling receiver with "+networkFile);
    }
}
