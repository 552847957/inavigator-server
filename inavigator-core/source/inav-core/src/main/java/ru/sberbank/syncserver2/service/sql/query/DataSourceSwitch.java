package ru.sberbank.syncserver2.service.sql.query;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sberbank.syncserver2.service.sql.query.SwitchingDataSourceDAO.AbandonedDataSourceListener;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

/*import com.sberbank.vmo.syncserv.web.ConfigManager;
import com.sberbank.vmo.syncserv.web.FileLoader;
import com.sberbank.vmo.syncserv.web.NetworkCopier;
import com.sberbank.vmo.syncserv.web.NetworkCopier.Listener;*/

/**
 * @author Sergey Erin
 *
 */
public class DataSourceSwitch {

    /**
     *
     */
    private static final int WIPE_FILES_INTERVAL_MS = 1000;

    private static final Logger log = Logger.getLogger(DataSourceSwitch.class);

//    protected static final String URL_TEMPLATE_PROPERTY = "urlTemplate";
    protected static final String URL_PROPERTY          = "url";

    @Autowired private SwitchingDataSourceDAO dao;

    private Properties localDataSourceProperties;

    private String networkPath;
    private String syncHomePath;

    private File databaseDirectory;
    private File tempInboxDirectory;
    private File databaseNetworkDirectory;

    private Collection<File> filesToDelete = new ConcurrentLinkedQueue<File>();

    private static ThreadLocal<DateFormat> UNIQUE_NAME_PART_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyMMddHHmmssSSS");
        };
    };

    public DataSourceSwitch() {
    }

    public void init() {
        dao.setAbandonedListener(new AbandonedDataSourceListener() {

            @Override
            public void release(DataSourceDescriptor descriptor) {

                File database = new File(databaseDirectory, descriptor.getName());

                log.debug("Deleting database file: " + database.getAbsolutePath());
                if (!database.delete()) {
                    log.error("Database file couldn't be deleted: " + database.getAbsolutePath());
                    filesToDelete.add(database);
                }
            }
        });

/*        initDirectories();

        createNetworkCopier();*/

        registerExistingDatabases();

        startWipeManager();
    }

    private void startWipeManager() {
        new Thread("Wipe manager") {
            @Override
            public void run() {
                boolean run = true;
                while (run) {
                    for (;filesToDelete.iterator().hasNext();) {
                        File file = filesToDelete.iterator().next();
                        log.debug("Deleting " + file.getAbsolutePath());
                        if (file.delete()) {
                            filesToDelete.iterator().remove();
                        } else {
                            log.warn("Database file couldn't be deleted: " + file.getAbsolutePath());
                        }
                    }

                    try {
                        Thread.sleep(WIPE_FILES_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        log.info("Thread has been interrupted");
                        run = false;
                    }
                }
            }
        };
    }

    /**
     *
     */
    private void registerExistingDatabases() {
        log.info("Checking existing databases in " + databaseDirectory.getAbsolutePath());
        File[] databases = databaseDirectory.listFiles();

        if (databases != null && databases.length > 0) {
            Arrays.sort(databases, new Comparator<File>() {

                @Override
                public int compare(File o1, File o2) {
                    if (o1.lastModified() < o2.lastModified()) {
                        return 1;
                    } else if (o1.lastModified() > o2.lastModified()) {
                        return -1;
                    }
                    return 0;
                }
            });
            for (File database : databases) {
                if (database.isDirectory()) {
                    continue;
                }

                // TODO data source key is incorrect here
                onDataSourceUpdated(database.getName(), database);
                break;
            }
        }
    }

/*    private void createNetworkCopier() {
        log.info("Creating database network copier");
        NetworkCopier copier = new NetworkCopier(
                SyncLoggerFactory.network_db,
                databaseNetworkDirectory.getAbsolutePath(),
                databaseDirectory.getAbsolutePath(),
                tempInboxDirectory.getAbsolutePath());

        copier.setListener(new Listener() {

            @Override
            public void onCopyFinish(File file) {
                // rename file: add unique suffix
                Date lastModified = new Date(file.lastModified());
                String suffix = UNIQUE_NAME_PART_FORMAT.get().format(lastModified);
                File renamedFile = new File(file.getParentFile(), file.getName() + "-" + suffix);

                log.info("Rename " + file.getAbsolutePath() + " to " + renamedFile.getAbsolutePath());

                if (renamedFile.exists()) {
                    log.error("Already exists " + renamedFile.getAbsolutePath());
                    return;
                }

                boolean renamed = file.renameTo(renamedFile);
                if (!renamed) {
                    log.error("Can't rename " + file.getAbsolutePath() + " to " + renamedFile.getAbsolutePath());
                    return;
                }

                onDataSourceUpdated(file.getName(), renamedFile);
            }

        });

        copier.start();
    }

    private void initDirectories() {
        networkPath = ConfigManager.getNetworkDatabaseInbox();
        syncHomePath = ConfigManager.getSyncHome();

        if (networkPath == null || networkPath.isEmpty()) {
            log.info("Database network path is not set");
            throw new IllegalStateException("Database network path is not set");
        }

        if (syncHomePath == null || syncHomePath.isEmpty()) {
            log.info("Sync home path is not set");
            throw new IllegalStateException("Sync home path is not set");
        }

        databaseNetworkDirectory = new File(networkPath);
        databaseDirectory = FileLoader.getDatabaseFolder(syncHomePath);
        tempInboxDirectory = FileLoader.getTempDatabaseFolder(syncHomePath);

        createDirectory(databaseDirectory);
        createDirectory(databaseNetworkDirectory);
        createDirectory(tempInboxDirectory);

        log.info("Database directory:         " + databaseDirectory.getAbsolutePath());
        log.info("Database network directory: " + databaseNetworkDirectory.getAbsolutePath());
        log.info("Temp inbox directory:       " + tempInboxDirectory.getAbsolutePath());
    }*/

    private void onDataSourceUpdated(String key, File database) {
        // create new data source
        Properties properties = getPropertiesFromTemplate(database.getAbsolutePath());

        DataSource dataSource = createDataSource(properties);
        if (dataSource == null) {
            return;
        }

        DataSourceDescriptor descriptor = new DataSourceDescriptor();
        descriptor.setDataSource(dataSource);
        descriptor.setKey(key);
        descriptor.setName(database.getName());

        dao.setDataSourceDescriptor(descriptor);
    }

    private void createDirectory(File directory) {
        log.info("Checking existance/creating directory: " + directory.getAbsolutePath());
        if (!directory.exists() && !directory.mkdirs()) {
            log.info("Directory couldn't be created: " + directory.getAbsolutePath());
            throw new IllegalStateException("Directory couldn't be created: " + directory.getAbsolutePath());
        }
    }

    private DataSource createDataSource(Properties properties) {
        DataSource dataSource = null;
        try {
//            dataSource = new DriverManagerDataSource(properties.getProperty(URL_PROPERTY), properties);
            dataSource = BasicDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            log.error("Can't create data source with properties " + properties + ": " + e.getMessage());
        }
        return dataSource;
    }

    private Properties getPropertiesFromTemplate(String... parameters) {
        String urlTemplate = localDataSourceProperties.getProperty(URL_PROPERTY);
        if (urlTemplate == null || urlTemplate.trim().isEmpty()) {
            log.error("Property '" + URL_PROPERTY + "' is not set");
            throw new IllegalStateException("Property '" + URL_PROPERTY + "' is not set");
        }

        String url = String.format(urlTemplate, parameters);

        Properties properties = new Properties(localDataSourceProperties);
        properties.put(URL_PROPERTY, url);
        return properties;
    }

    public String getNetworkPath() {
        return networkPath;
    }

    public void setNetworkPath(String networkPath) {
        this.networkPath = networkPath;
    }

    public String getSyncHomePath() {
        return syncHomePath;
    }

    public void setSyncHomePath(String syncHomePath) {
        this.syncHomePath = syncHomePath;
    }

    public Properties getLocalDataSourceProperties() {
        return localDataSourceProperties;
    }

    public void setLocalDataSourceProperties(Properties localDataSourceProperties) {
        this.localDataSourceProperties = localDataSourceProperties;
    }


}
