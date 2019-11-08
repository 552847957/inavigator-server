package ru.sberbank.syncserver2.service.file.cache.list;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.core.config.StaticFileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;

/**
 * Created by sbt-kozhinsky-lb on 11.03.14.
 */
public abstract class FileLister extends SingleThreadBackgroundService {
	private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String,StaticFileInfo> fileMap = new HashMap<String, StaticFileInfo>();

    public FileLister() {
    	super(60); //10 seconds to waite between executions
	}

    protected void loadAll(){
        loadAll(null);
    }

    protected void loadAll(Map<String,StaticFileInfo> loadedMap){
        //1. Loading
        if(loadedMap==null){
            loadedMap = doLoadAll();
        }

        //2. Replacing
        try {
            lock.writeLock().lock();
            fileMap = loadedMap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected Map<String,StaticFileInfo> cloneStaticInfoMap(){
        try {
            lock.readLock().lock();
            return new HashMap<String,StaticFileInfo>(fileMap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    protected abstract Map<String,StaticFileInfo> doLoadAll();

	@Override
	public void doInit() {
		loadAll();

	}

	@Override
	public void doRun() {
		loadAll();

	}

    public StaticFileInfo getFileInfo(String fileName){
        try {
            lock.readLock().lock();
            StaticFileInfo result = fileMap.get(fileName);
            if(result==null){
                lock.readLock().unlock();
                loadAll();
                lock.readLock().lock();
                result = fileMap.get(fileName);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }



}
