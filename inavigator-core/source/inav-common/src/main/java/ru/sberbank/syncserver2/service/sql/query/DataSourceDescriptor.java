package ru.sberbank.syncserver2.service.sql.query;

import javax.sql.DataSource;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * @author Sergey Erin
 *
 */
public class DataSourceDescriptor {

    private String key;
    private String name;
    private DataSource dataSource;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ReadLock readLock() {
        return lock.readLock();
    }

    public WriteLock writeLock() {
        return lock.writeLock();
    }

    @Override
    public String toString() {
        return "DataSourceDescriptor [key=" + key + ", name=" + name
                + ", dataSource=" + dataSource + "]";
    }

}
