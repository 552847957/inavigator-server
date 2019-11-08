package ru.sberbank.syncserver2.service.generator;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.util.ClusterHookProvider;

import static ru.sberbank.syncserver2.util.constants.INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_GENERATOR_CLUSTERMANAGER_DORUN;

/**
 * Created by sbt-kozhinsky-lb on 01.07.14.
 */
public class ClusterManager extends SingleThreadBackgroundService {
    private ConfigLoader configLoader;
    private ActiveInfo activeInfo = null;

    public ClusterManager() {
        super(60);
    }
    
    private String localName = LOCAL_HOST_NAME;

    @Override
    public void doInit() {
        if (ClusterHookProvider.isClusterHooked()) {
        	localName += "_"+ClusterHookProvider.getSuffixForHook();
        	tagLogger.log("Вместо имени хоста используется значение "+localName);
        }
        getConfigLoader();
        doRun();
    }

    @Override
    public void doRun() {
        //1. Prepare for quering
        ConfigLoader localConfigLoader = getConfigLoader();
        String sql = RU_SBERBANK_SYNCSERVER2_SERVICE_GENERATOR_CLUSTERMANAGER_DORUN;
        
        //2. Querying
        ActiveInfo localActiveInfo = localConfigLoader.getValue(sql, new ResultSetExtractor<ActiveInfo>() {
            @Override
            public ActiveInfo extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                synchronized (ClusterManager.this){
                	ActiveInfo info = null;
                	 if(resultSet.next()){
                		 boolean active = "true".equalsIgnoreCase(resultSet.getString(1));
                		 String activeHostName = resultSet.getString(2);
                		 info = new ActiveInfo(active,activeHostName);
                	 }	 
                return info;
                }
            }
        }, localName);

        //3. Parsing
        synchronized (this){
            activeInfo = localActiveInfo;
        }
    }

    public synchronized boolean isActive(){
        return activeInfo==null ? false : activeInfo.active;
    }

    /**
     * The idea of this method in synchronization
     * @return
     */
    private synchronized ConfigLoader getConfigLoader(){
        if(this.configLoader!=null){
            return configLoader;
        }
        configLoader = ServiceManager.getInstance().getConfigLoader();
        return configLoader;
    }

    public String getActiveHostName() {
        return activeInfo==null ? null : activeInfo.activeHostName;
    }

    public static class ActiveInfo {
        private boolean active;
        private String activeHostName;

        public ActiveInfo(boolean active, String activeHostName) {
            this.active = active;
            this.activeHostName = activeHostName;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getActiveHostName() {
            return activeHostName;
        }

        public void setActiveHostName(String activeHostName) {
            this.activeHostName = activeHostName;
        }
    }
}
