package ru.sberbank.syncserver2.service.core;

import ru.sberbank.syncserver2.service.log.TagLogger;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.BeanProperty;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by sbt-kozhinsky-lb on 21.02.14.
 */
public class ServiceContainer {
    private Bean config;
    private ServiceState            state = ServiceState.STOPPED;
    private AbstractService         service;
    private ServiceManager          serviceManager;
    private ReentrantReadWriteLock  stateLock = new ReentrantReadWriteLock();
    private static TagLogger tagLogger = TagLogger.getTagLogger(ServiceContainer.class);
    private char[] stateDesc;
	private String folder;


    public ServiceContainer(ServiceManager serviceManager, String folder, Bean containerConfig) {
        this.serviceManager = serviceManager;
        this.folder = folder;
        this.config = containerConfig;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public Bean getConfig() {
        return config;
    }

    public AbstractService getService() {
        return service;
    }

    public void setService(AbstractService service) {
        this.service = service;
    }

    /**
     * @return true if service was successfully restarted
     */
    public  boolean createAndConfigure() throws ComponentException {
        try {
            //1. Locking of finish
            if(!stateLock.writeLock().tryLock()){
                return false;
            }

            //2. Stopping service if necessary
            this.state = ServiceState.STOPPING;
            stopService();
            this.state = ServiceState.STOPPED;

            //3. Creating service
            this.service = createAndConfigure(config);
            this.service.setServiceContainer(this);
            this.state = ServiceState.CREATED;
        } finally {
            stateLock.writeLock().unlock();
        }
        return true;
    }

    public AbstractService createAndConfigure(Bean theConfig) throws ComponentException {
        AbstractService         theService;
        theService = (AbstractService) ServiceManagerHelper.createAndConfigure(theConfig);
        autowireBySpringWebContext(theService);
        return theService;
    }

    /**
     * Запустить процесс автоподстановки полей
     * @param bean
     */
    public void autowireBySpringWebContext(Object bean) {
        getServiceManager().getWebApplicationContext().getAutowireCapableBeanFactory().  autowireBean(bean);
    }
    
    /**
     * @return true if service was successfully stopped
     */
    public  boolean stopService() {
        try {
            //1. Loocking of finish
            stateLock.writeLock().lock();

            //2. Start stopping
            if(service!=null && this.state != ServiceState.STOPPED){
                this.state = ServiceState.STOPPING;
                service.doStop();
            } else {
                tagLogger.log(new String[]{config.getCode()},"Skip stopping service "+config.getClazz());
            }
            this.state = ServiceState.STOPPED;
        } finally {
            stateLock.writeLock().unlock();
        }
        return true;
    }

    /**
     * @return true if service was successfully started
     * @throws ComponentException
     */
    public  boolean startService() throws ComponentException {
        try {
            //1. Locking
            stateLock.writeLock().lock();

            //2. Stopping
            stopService();

            //3. Starting
            if(service!=null){
            	// reload properties with configLoader (from database)
            	ConfigLoader configLoader = serviceManager.getConfigLoader();
            	List<BeanProperty> props = configLoader.getBeanProperties(folder, config.getCode(), true);
            	config.setBeanProperties(props);

            	// reconfig service
            	ServiceManagerHelper.configure(config, service);

            	// starting service
                service.doStart();
                this.state = ServiceState.STARTED;
            } else {
                tagLogger.log(new String[]{config.getCode()},"Failed to start service "+config.getClazz()+" . Please check constructor access modifier.");
                return false;
            }
        } finally {
            stateLock.writeLock().unlock();
        }
        return true;
    }

    public ServiceState getState() {
        return state;
    }

    void setState(ServiceState state) {
        this.state = state;
    }
}
