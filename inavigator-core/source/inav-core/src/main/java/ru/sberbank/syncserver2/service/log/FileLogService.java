package ru.sberbank.syncserver2.service.log;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.XMLLayout;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 08.05.14.
 */
public class FileLogService extends DbLogService {

    private final static Logger log = Logger.getLogger(FileLogService.class);
    private String logFileName;

    @Override
    public void doInit() {
        super.doInit();
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) throws IOException {
        this.logFileName = logFileName;
        FileAppender appender = (FileAppender) log.getAppender("PASSPORT");
        Layout layout;
        if (appender != null) {
            layout = appender.getLayout();
            log.removeAppender(appender);
            appender.close();
        } else {
            layout = new XMLLayout();
        }
        if (logFileName != null) {
            FileAppender fileAppender = new FileAppender(layout, logFileName, true);
            fileAppender.setName("PASSPORT");
            log.addAppender(fileAppender);
        }
    }

    @Override
    public void doRun() {
        try {
            int currSize = queue.size();
            if (currSize > 0) {
                //tagLogger.log("DbLogService has found "+currSize+" records for writing to database");
                List<LogMsg> msgs = new ArrayList<LogMsg>(currSize);
                for (int i = 0; i < currSize; i++) {
                    msgs.add(queue.poll());
                }

                //TODO - Here we should write down log records to file instead of System.out
                for (int i = 0; i < msgs.size(); i++) {
                    LogMsg logMsg = msgs.get(i);
                    log.info(logMsg);
                }
            }

        } catch (Throwable th) {
            logger.error("Exception during storing log data in database", th);
        }
    }

    @Override
    public void log(LogMsg... logMsgs) {
        ServiceContainer container = getServiceContainer();
        if (container.getState() == ServiceState.STARTED) {
            queue.addAll(Arrays.asList(logMsgs));
        }
    }

    @Override
    public void scheduleSQL(String sql) {
        throw new UnsupportedOperationException();
    }
}
