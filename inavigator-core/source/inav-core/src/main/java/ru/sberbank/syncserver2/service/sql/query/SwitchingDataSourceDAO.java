package ru.sberbank.syncserver2.service.sql.query;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author  Sergey Erin
 */
public class SwitchingDataSourceDAO {

    static final Logger log = Logger.getLogger(SwitchingDataSourceDAO.class);

    private AbandonedDataSourceListener abandonedListener;

    private AtomicReference<DataSourceDescriptor> dataSourceDescriptor = new AtomicReference<DataSourceDescriptor>();

    public SwitchingDataSourceDAO() {
    }

    public void setDataSourceDescriptor(DataSourceDescriptor newDescriptor) {
        log.info("Register new data source: " + newDescriptor);

        DataSourceDescriptor old = dataSourceDescriptor.getAndSet(newDescriptor);

        notifyAbandoned(old);
    }

    public DataResponse query(final OnlineRequest request) {

        DataSourceDescriptor descriptor = dataSourceDescriptor.get();
        if (descriptor == null) {
            log.error("Datasource couldn't be found");
            DataResponse response = new DataResponse();
            response.setError("Datasource couln't be found");
            response.setResult(Result.FAIL);
            return response;
        }

        descriptor.readLock().lock();
        try {
            // FIXME OnlinerRequestDBExecutor.initSqlTypes() shouldn't be invoked on every request
            return new OnlineRequestDBExecutor(new JdbcTemplate(descriptor.getDataSource())).query(request);
        } finally {
            descriptor.readLock().unlock();
        }
    }

    /**
     * @param descriptor
     */
    private void
    notifyAbandoned(DataSourceDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }

        log.debug("Data source is not used any more: " + descriptor);
        closeDataSource(descriptor);
        if (abandonedListener != null) {
            descriptor.writeLock().lock();
            try {
                abandonedListener.release(descriptor);
            } finally {
                descriptor.writeLock().unlock();
            }
        }
    }

    private void closeDataSource(DataSourceDescriptor descriptor) {
        DataSource ds = descriptor.getDataSource();
        if (ds instanceof BasicDataSource) {
            try {
                ((BasicDataSource) ds).close();
            } catch (SQLException e) {
                log.warn("Couldn't close dataSource " + ds);
            }
        }
    }

    public interface AbandonedDataSourceListener {

        void release(DataSourceDescriptor descriptor);

    }

    public void setAbandonedListener(AbandonedDataSourceListener abandonedListener) {
        this.abandonedListener = abandonedListener;
    }
}
