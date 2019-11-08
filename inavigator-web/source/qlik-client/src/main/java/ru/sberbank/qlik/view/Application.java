package ru.sberbank.qlik.view;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;

public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);

    private ActiveXComponent application;;
    public static final String QV_PROCESS_NAME = "Qv.exe";
    public static final Charset CONSOLE_ENCODING = Charset.forName("IBM866");

    public Application() {
        log.debug("New QlikView application client created");
    }

    /**
     * Пытается завершить {@link #QV_PROCESS_NAME}
     **/
    public boolean destroyQViewProcess() throws Exception {
        log.debug("Try to destroy " + QV_PROCESS_NAME);
        Process killProcess = Runtime.getRuntime().exec("taskkill /f /t /im \""+ QV_PROCESS_NAME + "\"");
        String s1 = IOUtils.toString(killProcess.getInputStream(), CONSOLE_ENCODING);
        String e1 = IOUtils.toString(killProcess.getErrorStream(), CONSOLE_ENCODING);
        killProcess.waitFor();
        if(e1.trim().length() == 0) {
            log.debug(s1);
            return true;
        } else {
            log.error(e1);
            return false;
        }
    }

    /**
     * Проверяет имеется ли  {@link #QV_PROCESS_NAME} в списке процессов
     **/
    public boolean checkQViewProcess() throws Exception {
        log.debug("Check QlikView is active...");
        Process tasklist = Runtime.getRuntime().exec("tasklist");
        String s1 = IOUtils.toString(tasklist.getInputStream(), CONSOLE_ENCODING);
        String e1 = IOUtils.toString(tasklist.getInputStream(), CONSOLE_ENCODING);
        boolean qvRunned = s1.trim().contains(QV_PROCESS_NAME);
        boolean noError = e1.trim().length() == 0;
        tasklist.waitFor();
        if(noError) {
            log.debug("QlikView is " + (qvRunned ? "" : "not ") + " active");
            return qvRunned;
        } else {
            throw new RuntimeException(e1);
        }
    }

    public void connect() {
        log.debug("Connecting to QlikView Desktop...");
        this.application = ActiveXComponent.createNewInstance("QlikTech.QlikView");
        log.debug("Connected to QlikView Desktop");
    }

    public void quit() {
        log.debug("Disconnecting from QlikView Desktop...");
        Dispatch.call(application, "Quit");
        log.debug("Disconnected from QlikView Desktop");
    }

    public Document openDocument(String documentUri, String user, String password) {
        //Dispatch documentDispatch = Dispatch.call(application, "OpenDoc", documentUri, user, password).getDispatch();
        log.debug("Try to open: " + documentUri);
        Dispatch documentDispatch = Dispatch.call(application, "OpenDocEx", documentUri, 2, false, user, password, "", false, false).getDispatch();
        Document document = new Document(documentDispatch);
        String documentName = document.getName();
        log.debug("Document opened: " + documentName);
        return document;
    }
}
