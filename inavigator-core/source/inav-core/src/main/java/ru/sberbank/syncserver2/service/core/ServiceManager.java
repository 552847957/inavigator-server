package ru.sberbank.syncserver2.service.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.core.config.Folder;
import ru.sberbank.syncserver2.service.core.event.AbstractSystemEventHandler;
import ru.sberbank.syncserver2.service.core.event.ThrowExceptionSystemEventHandler;
import ru.sberbank.syncserver2.service.log.DbLogService;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by sbt-kozhinsky-lb on 21.02.14.
 */
public class ServiceManager {
    private static volatile ServiceManager instance = null;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String, Map<String, ServiceContainer>> serviceContainers = new ConcurrentHashMap<String, Map<String, ServiceContainer>>();

    private Map<String, Map<String, ServiceContainer>> publicContainers = new ConcurrentHashMap<String, Map<String, ServiceContainer>>();
    private Logger logger = Logger.getLogger(ServiceManager.class);
    private ConfigLoader configLoader;
    private DataSource configDataSource;
    private DbLogService dbLogService = null;

    private ServletContext servletContext = null;

    /**
     * true - в случае, если указана неверная папка, но сам публичный сервис найден в единственном экземпляре - не вызывать ошибку
     * false - если папка не найдена выдавать ошибку об отсутсвие сервиса
     */
    private static boolean IGNORE_FOLDER_FOR_PUBLIC_SERVICES = true;

    public static ServiceManager getInstance() {
        return instance;
    }

    public ServiceManager() {
        synchronized (ServiceManager.class) {
            instance = this;
            try {
                ServiceManager.class.notifyAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    @Qualifier("configLoader")
    public void setConfigLoader(ConfigLoader configLoader) {
        //System.out.println("setConfigLoader CALL");
        this.configLoader = configLoader;
    }


    public DataSource getConfigSource() {
        return configDataSource;
    }

    @Qualifier("configSource")
    public void setConfigSource(DataSource configDataSource) {
        this.configDataSource = configDataSource;
    }

    /**
     * Получить доступ к spring dt, контексту
     *
     * @param beanId
     * @return
     */
    public WebApplicationContext getWebApplicationContext() {
        return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    }

    public synchronized void startAll() throws ComponentException {
        //1. Creating core services in admin area
        try {
            //1.1. Creating log service
            String folderCode = "admin";
            logger.info("Start creating beans for " + folderCode);
            String loggingService = configLoader.getSyncConfigProperty("LOGGING_SERVICE");
            String loggingServiceDesc = "Synchronous Logging for Sync Generator";
            if (StringUtils.isBlank(loggingService)) {
                loggingService = "DbLogService";
                loggingServiceDesc = "Asynchronous logging (default logging service)";
            }
            String serviceCode = folderCode + loggingService;
            Bean beanConfig = new Bean(new Long(-3), serviceCode, "ru.sberbank.syncserver2.service.log." + loggingService, null, null, new Integer(0), null, loggingServiceDesc);
            beanConfig.setBeanProperties(configLoader.getBeanProperties(folderCode, serviceCode, false));
            createAndConfigure(folderCode, beanConfig, true);
            ServiceContainer logServiceContainer = getServiceContainer(folderCode, serviceCode);
            this.dbLogService = (DbLogService) logServiceContainer.getService();
            this.dbLogService.doStart();
            logServiceContainer.setState(ServiceState.STARTED);

            /*
            //1.2. Creating version service
            beanConfig = new Bean(new Long(-2),folderCode+"VersionService", "ru.sberbank.syncserver2.service.common.VersionService",null,null,new Integer(1),"version.do");
            createAndConfigure(folderCode, beanConfig, false);
            ServiceContainer versionContainer = getServiceContainer(folderCode,beanConfig.getCode());
            versionContainer.setState(ServiceState.STARTED);
            */

            //1.3. Creating ping service
            String serviceDesc = "Use this service to check that server is up. This service will tell PONG on request";
            beanConfig = new Bean(new Long(-1), folderCode + "PingService", "ru.sberbank.syncserver2.service.common.PingService", null, null, new Integer(2), "ping.do", serviceDesc);
            createAndConfigure(folderCode, beanConfig, false);
            ServiceContainer pingContainer = getServiceContainer(folderCode, beanConfig.getCode());
            pingContainer.setState(ServiceState.STARTED);
            logger.info("Finish creating beans for " + folderCode);

        } catch (ComponentException e) {
            e.printStackTrace();
        } finally {
        }

        //2. Listing folders and create and configure and bind and start
        //2.1. Creating
        List<Folder> folders = configLoader.getFolders();
        for (int i = 0; i < folders.size(); i++) {
            //2.1. Create and configure
            Folder folder = folders.get(i);
            try {
                logger.info("Start creating beans for " + folder.getCode());
                createAndConfigure(folder.getCode());
            } catch (ComponentException ce) {
                logger.error("Finish creating beans for " + folder.getCode() + " with error ", ce);
            } finally {
                logger.info("Finish creating beans for " + folder.getCode() + " successfully ");
            }
        }

        //2.2. Bind folder
        for (int i = 0; i < folders.size(); i++) {
            //2.1. Create and configure
            Folder folder = folders.get(i);
            try {
                logger.info("Start binding beans for " + folder.getCode());
                bindFolder(folder.getCode());
            } catch (Exception ce) {
                logger.error("Finish binding beans for " + folder.getCode() + " with error ", ce);
            } finally {
                logger.info("Finish binding beans for " + folder.getCode());
            }
        }

        //2.3. Start folder
        for (int i = 0; i < folders.size(); i++) {
            //2.1. Create and configure
            Folder folder = folders.get(i);
            try {
                logger.info("Start starting beans for " + folder.getCode());
                startFolder(folder.getCode());
            } catch (Exception ce) {
                ce.printStackTrace();
                logger.error("Error at starting beans for " + folder.getCode() + " with error ", ce);
            } finally {
                logger.info("Finish starting beans for " + folder.getCode());
            }
        }
    }

    private void createAndConfigure(String folderCode) throws ComponentException {
        //1. Loading bean by bean and creating container by container
        List<Bean> beans = configLoader.getBeans(folderCode);
        for (int i = 0; i < beans.size(); i++) {
            Bean beanConfig = beans.get(i);
            createAndConfigure(folderCode, beanConfig, false);
        }
    }

    private void createAndConfigure(String folderCode, Bean beanConfig, boolean isLogService) throws ComponentException {
        //1. Creating container
        ServiceContainer container = null;
        try {
            logger.info("Start creating bean " + beanConfig.getCode() + " in folder " + folderCode);
            lock.writeLock().lock();
            container = getServiceContainerInsideWriteLock(folderCode, beanConfig.getCode());
            if (container == null) {
                container = createNewServiceContainer(this, folderCode, beanConfig);
                container.setServiceManager(this);
                putServiceContainer(folderCode, beanConfig, container);
            }
        } finally {
            lock.writeLock().unlock();
            logger.info("Finish creating bean " + beanConfig.getCode() + " in folder " + folderCode);
        }

        //2. Create and configure service
        container.createAndConfigure();
        if (!isLogService) {
            AbstractService service = container.getService();
            service.setDbLogger(dbLogService);
        }
    }

    public ServiceContainer createNewServiceContainer(ServiceManager serviceManager, String folder, Bean containerConfig) {
        return new ServiceContainer(serviceManager, folder, containerConfig);
    }


    private void bindFolder(String folderCode) {
        //1. Listing folder beans
        try {
            lock.writeLock().lock();
            Map folderContainers = serviceContainers.get(folderCode);
            for (Iterator iterator = folderContainers.values().iterator(); iterator.hasNext(); ) {
                ServiceContainer container = (ServiceContainer) iterator.next();
                AbstractService service = container.getService();
                Bean bean = container.getConfig();
                String parentCode = bean.getParentCode();
                String parentProperty = bean.getParentProperty();
                if (parentCode != null && parentCode.trim().length() > 0) {
                    try {
                        ServiceContainer parent = findServiceByBeanCode(parentCode);
                        AbstractService parentService = parent.getService();
                        ServiceManagerHelper.setProperty(parentService, parentProperty, service);
                    } catch (Exception e) {
                        logger.info("Failed to bind " + bean.getCode() + " TO " + parentCode + "." + parentProperty);
                        e.printStackTrace();
                    } finally {
                        logger.info("Successfully bound " + bean.getCode() + " TO " + parentCode + "." + parentProperty);
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void startFolder(String folderCode) throws ComponentException {
        //1. Listing folder beans
        try {
            lock.writeLock().lock();
            Map folderContainers = serviceContainers.get(folderCode);
            for (Iterator iterator = folderContainers.values().iterator(); iterator.hasNext(); ) {
                ServiceContainer container = (ServiceContainer) iterator.next();
                if (!container.getConfig().isStopped())
                    container.startService();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void stopFolder(String folderCode) throws ComponentException {
        //1. Listing folder beans
        try {
            lock.writeLock().lock();
            Map folderContainers = serviceContainers.get(folderCode);
            for (Iterator iterator = folderContainers.values().iterator(); iterator.hasNext(); ) {
                ServiceContainer container = (ServiceContainer) iterator.next();
                System.out.println("Start stopping " + container.getService().getServiceBeanCode());
                container.stopService();
                System.out.println("Finish stopping " + container.getService().getServiceBeanCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void waitUntilStopped(String folderCode) {
        try {
            lock.writeLock().lock();
            Map folderContainers = serviceContainers.get(folderCode);
            for (Iterator iterator = folderContainers.values().iterator(); iterator.hasNext(); ) {
                ServiceContainer container = (ServiceContainer) iterator.next();
                AbstractService service = container.getService();
                if (service != null) {
                    System.out.println("Start waiting until stopped for " + service.getServiceBeanCode());
                    service.waitUntilStopped();
                    System.out.println("Finish waiting until stopped for " + service.getServiceBeanCode());
                } else {
                    Bean bean = container.getConfig();
                    String beanId = bean == null ? "null" : bean.getCode();
                    System.out.println("Skip waiting until stopped for missing service for " + beanId + " in folder " + folderCode);
                }
                Thread.yield();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }


    public boolean startService(String folder, String beanCode) throws ComponentException {
        try {
            lock.writeLock().lock();
            ServiceContainer container = getServiceContainer(folder, beanCode);
            if (container == null || !container.startService())
                return false;
        } finally {
            lock.writeLock().unlock();
        }
        try {
            configLoader.setStoppedState(folder, beanCode, false);
        } catch (Exception e) {
            logger.error("Can't update service state", e);
        }
        return true;
    }

    public boolean stopService(String folder, String beanCode) throws ComponentException {
        try {
            lock.writeLock().lock();
            ServiceContainer container = getServiceContainer(folder, beanCode);
            if (container == null || !container.stopService())
                return false;
        } finally {
            lock.writeLock().unlock();
        }
        try {
            configLoader.setStoppedState(folder, beanCode, true);
        } catch (Exception e) {
            logger.error("Can't update service state", e);
        }
        return true;
    }

    public synchronized void stopAll() throws ComponentException {
        //1. Listing folders and stop folder by folder
        final List<Folder> folders = configLoader.getFolders();
        for (int i = 0; i < folders.size(); i++) {
            Folder folder = folders.get(i);
            System.out.println("Start stopping folder " + folder.getCode());
            stopFolder(folder.getCode());
            System.out.println("Finish stopping folder " + folder.getCode());
        }

        //2. Stopping services in admin folder
        String folderCode = "admin";
        System.out.println("Start stopping folder " + folderCode);
        stopFolder(folderCode);
        System.out.println("Finish stopping folder " + folderCode);

        //3. Waiting for 5 minutes or until all services in all folders are stopped
        ExecutorService fixedPool = Executors.newFixedThreadPool(1);
        final AtomicBoolean finished = new AtomicBoolean(false);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < folders.size(); i++) {
                    Folder folder = folders.get(i);
                    System.out.println("Start waiting until stopped folder " + folder.getCode());
                    waitUntilStopped(folder.getCode());
                    System.out.println("Finish waiting until stopped folder " + folder.getCode());
                }
                finished.set(true);
            }
        };
        fixedPool.execute(task);
        System.out.println("Start waiting for all stops for 5 minutes");
        for (int i = 0; i < 150; i++) {
            if (finished.get()) {
                System.out.println("Finish waiting for all stops with success");
                fixedPool.shutdown();
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fixedPool.shutdown();
        System.out.println("Finish waiting for all stops with error");
    }

    public ServiceState getServiceState(String folder, String beanCode) {
        try {
            lock.readLock().lock();
            ServiceContainer container = getServiceContainer(folder, beanCode);
            return container == null ? ServiceState.UNKNOWN : container.getState();
        } finally {
            lock.readLock().unlock();
        }
    }

    private synchronized ServiceContainer getServiceContainer(String folderCode, String beanCode) {
        Map<String, ServiceContainer> containers = serviceContainers.get(folderCode);
        ServiceContainer container = containers == null ? null : containers.get(beanCode);
        return container;
    }

    public ServiceContainer getServiceContainerExt(String folderCode, String beanCode) {
        try {
            lock.readLock().lock();
            return getServiceContainer(folderCode, beanCode);
        } finally {
            lock.readLock().unlock();
        }
    }

    public PublicService getPublicService(String folderCode, String servletCode) {
        try {
            lock.readLock().lock();
            Map<String, ServiceContainer> containers = publicContainers.get(folderCode);
            if (containers != null) {
                ServiceContainer container = containers.get(servletCode);
                if (container != null)
                    return (PublicService) container.getService();
            }
            if (IGNORE_FOLDER_FOR_PUBLIC_SERVICES) {
                List<ServiceContainer> serviceContainers = new ArrayList<ServiceContainer>();
                for (Iterator<String> iterator = publicContainers.keySet().iterator(); iterator.hasNext(); ) {
                    ServiceContainer serviceContainer = publicContainers.get(iterator.next()).get(servletCode);
                    if (serviceContainer != null)
                        serviceContainers.add(serviceContainer);
                }
                if (serviceContainers.size() == 1)
                    return (PublicService) serviceContainers.get(0).getService();
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public AbstractSystemEventHandler getSystemEventHandler() {
        ServiceContainer sc = findServiceByBeanCode(StandartBeanCodes.SYSTEM_EVENT_HANDLER.getBeanCode());
        if (sc != null)
            return (AbstractSystemEventHandler) sc.getService();
        else
            return new ThrowExceptionSystemEventHandler();
    }

    public List<String> getPublicFolders() {
        return new ArrayList<String>(publicContainers.keySet());
    }

    public List<String> getServiceFolders() {
        return new ArrayList<String>(serviceContainers.keySet());
    }

    private ServiceContainer getServiceContainerInsideWriteLock(String folderCode, String beanCode) {
        Map<String, ServiceContainer> containers = serviceContainers.get(folderCode);
        ServiceContainer container = containers == null ? null : containers.get(beanCode);
        return container;
    }

    private void putServiceContainer(String folderCode, Bean beanConfig, ServiceContainer container) {
        //1. Put container to public containers
        Map<String, ServiceContainer> containers = serviceContainers.get(folderCode);
        if (containers == null) {
            containers = new ConcurrentHashMap<String, ServiceContainer>();
            serviceContainers.put(folderCode, containers);
        }
        containers.put(beanConfig.getCode(), container);

        //2. Changing public containers
        if (!beanConfig.isPublic()) {
            return;
        }
        containers = publicContainers.get(folderCode);
        if (containers == null) {
            containers = new ConcurrentHashMap<String, ServiceContainer>();
            publicContainers.put(folderCode, containers);
        }
        containers.put(beanConfig.getPublicServletPath(), container);
    }


    public List<ServiceContainer> getServiceContainerList(String folder) {
        try {
            lock.readLock().lock();
            //System.out.println("SERVICE CONTAINERS = "+serviceContainers);
            Map<String, ServiceContainer> containers = serviceContainers.get(folder);
            //System.out.println("CONTAINERS = "+containers);
            if (containers == null) {
                return Collections.EMPTY_LIST;
            } else {
                return new ArrayList(containers.values());
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, ServiceContainer> getServiceContainerMap(String folder) {
        Map<String, ServiceContainer> containers;
        try {
            lock.readLock().lock();
            containers = serviceContainers.get(folder);
            if (containers == null) {
                return Collections.EMPTY_MAP;
            } else {
                return containers;
            }
        } finally {
            lock.readLock().unlock();
        }
    }


    public List<String> getAllServiceCodes() {
        ArrayList<String> codes = new ArrayList<String>();
        try {
            lock.readLock().lock();
            for (Iterator<Map<String, ServiceContainer>> iterator = serviceContainers.values().iterator(); iterator.hasNext(); ) {
                Map<String, ServiceContainer> containers = (Map<String, ServiceContainer>) iterator.next();
                codes.addAll(containers.keySet());
            }
            return codes;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @param beanCode
     * @return
     */
    public ServiceContainer findServiceByBeanCode(String beanCode) {
        //System.out.println("Start finding bean "+beanCode+" in all folders");
        ServiceContainer result = null;
        try {
            lock.readLock().lock();
            for (Iterator<Map.Entry<String, Map<String, ServiceContainer>>> iterator = serviceContainers.entrySet().iterator(); iterator.hasNext(); ) {
                //1. Get folder map of bean and name of folder
                Map.Entry<String, Map<String, ServiceContainer>> entry = iterator.next();
                String folder = entry.getKey();
                Map<String, ServiceContainer> folderContainers = entry.getValue();

                //2. Finding bean in a folder
                //System.out.println("Start finding bean "+beanCode+" in folder "+folder);
                result = folderContainers.get(beanCode);
                if (result != null) {
                    System.out.println("Found bean " + beanCode);
                    return result;
                }
            }
            System.out.println("Did not find bean " + beanCode);
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }

    /**
     * @param beanClass
     * @return
     */
    public AbstractService findFirstServiceByClassCode(Class beanClass) {
        ServiceContainer result = null;
        String className = beanClass.getName();
        try {
            lock.readLock().lock();
            for (Iterator<Map<String, ServiceContainer>> iterator = serviceContainers.values().iterator(); iterator.hasNext(); ) {
                Map<String, ServiceContainer> folderContainers = iterator.next();
                for (Iterator<ServiceContainer> iterator2 = folderContainers.values().iterator(); iterator2.hasNext(); ) {
                    ServiceContainer container = iterator2.next();
                    Bean bean = container.getConfig();
                    if (StringUtils.equals(bean.getClazz(), className)) {
                        return container.getService();
                    }
                }
            }

        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    /**
     * @param beanClass
     * @return
     */
    public AbstractService findFirstServiceByClassCode(Class beanClass, ThreadLocal cachedInstance) {
        //1. Check if it was found before
        AbstractService previous = (AbstractService) cachedInstance.get();
        if (previous != null) {
            return previous;
        }

        //2. Finding a new one and updated cachedInstance
        AbstractService service = findFirstServiceByClassCode(beanClass);
        cachedInstance.set(service);
        return service;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

}
