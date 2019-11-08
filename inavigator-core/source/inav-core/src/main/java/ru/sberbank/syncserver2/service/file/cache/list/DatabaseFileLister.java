package ru.sberbank.syncserver2.service.file.cache.list;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.core.config.StaticFileInfo;

/**
 * Created by sbt-kozhinsky-lb on 11.03.14.
 */
public class DatabaseFileLister extends FileLister {

    @Override
    protected Map doLoadAll() {
        //1. Loading from database
        Map<String,StaticFileInfo> localFileMap = new HashMap<String, StaticFileInfo>();
        ServiceContainer container = getServiceContainer();
        ServiceManager manager = container.getServiceManager();
        ConfigLoader configLoader = manager.getConfigLoader();
        List<StaticFileInfo> files = configLoader.getStaticFileList();
        for (int i = 0; i < files.size(); i++) {
            StaticFileInfo staticFileInfo =  files.get(i);
            localFileMap.put(staticFileInfo.getFileName(), staticFileInfo);
        }
        return localFileMap;
    }

}
