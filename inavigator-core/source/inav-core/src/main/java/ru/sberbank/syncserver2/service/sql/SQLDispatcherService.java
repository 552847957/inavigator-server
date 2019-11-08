package ru.sberbank.syncserver2.service.sql;

import ru.sberbank.syncserver2.service.core.BackgroundService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by sbt-kozhinsky-lb on 31.03.14.
 */
public class SQLDispatcherService extends BackgroundService implements SQLService {
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String,SQLService> subservices = new HashMap();

    @Override
    public DataResponse request(OnlineRequest request) {
        //1. Check if service is defined
        String service = request.getService();
        if(service==null){
            return null;
        }

        //2. Static mapping
        SQLService subService = null;
        try {
            lock.readLock().lock();
            subService = subservices.get(service);
            if(subService!=null){
                return subService.request(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }

        //3. Dynamic mapping
        //3.1. Check if one of subservices was restarted
        boolean restarted = false;
        Map<String,SQLService> newSubservices = new HashMap();
        try {
            lock.readLock().lock();
            for (Iterator iterator = subservices.values().iterator(); iterator.hasNext(); ) {
                Dispatchable next = (Dispatchable) iterator.next();
                String nextServiceName = next.getServiceName();
                newSubservices.put(nextServiceName, (SQLService) next);
                if(next.isRestarted()){
                    restarted = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }

        //3.2. Apply changes after restart
        if(restarted){
            try {
                lock.writeLock().lock();
                subservices = newSubservices;
                subService = subservices.get(service);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.writeLock().unlock();
            }
        }
        return subService==null ? null:subService.request(request);
    }

    @Override
    protected void doStop() {

    }

    public void setSubService(SQLService service){
        Dispatchable dispatchable = (Dispatchable) service;
        String serviceName = dispatchable.getServiceName();
        subservices.put(serviceName, service);
    }

    @Override
    protected void doStart() {
        /*
        //THIS IS A SPECIAL CASE BECAUSE NORMALLY WE DON"T DO REBIND
        //1. Cleaning old services
        Map existingServices = new HashMap(subservices);

        //2. Migrating services to a new one to process service name change
        Map newSubservices = new HashMap();
        for (Iterator iterator = existingServices.values().iterator(); iterator.hasNext(); ) {
            Dispatchable sqlService = (Dispatchable) iterator.next();
            String serviceName = sqlService.getServiceName();
            newSubservices.put(serviceName, (AbstractSQLService)sqlService);
        }

        //3. Replacing
        BEFORE COMMENTING THIS CODE WAS NOT USED SINCE newSubservices was not used
        THIS CODE IS ALSO REPLACED BY DYNAMIC MAPPING IN <code>request</code>
        */
        super.doStart();
    }
}
