package ru.sberbank.syncserver2.service.file.cache.data;

import ru.sberbank.syncserver2.service.core.ResponseError;
import ru.sberbank.syncserver2.util.XMLHelper;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 25.01.12
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */
public class FileCacheConstants {
//    private static String HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public static final String UNKNOWN_ERROR            = XMLHelper.writeXMLToString(new ResponseError("401", "Unknown Error"), false, ResponseError.class);
    public static final String NO_SUCH_COMMAND          = XMLHelper.writeXMLToString(new ResponseError("402","Invalid command")        , false, ResponseError.class);
    public static final String NO_SUCH_APP              = XMLHelper.writeXMLToString(new ResponseError("403","Invalid application: ''{0}''") , false, ResponseError.class);
    public static final String NO_SUCH_REPORT           = XMLHelper.writeXMLToString(new ResponseError("404","Invalid report type ''{0}'' for app ''{1}''") , false, ResponseError.class);
    public static final String NO_SUCH_CHUNK            = XMLHelper.writeXMLToString(new ResponseError("405","Internal error - chunk ''{0}''  was not found for report ''{1}'' for app ''{2}'' ") , false, ResponseError.class);
    public static final String NOT_ALLOWED_TO_USE_APP   = XMLHelper.writeXMLToString(new ResponseError("406","No permission to use app ''{0}''") , false, ResponseError.class);
    public static final String NOT_ALLOWED_TO_LOAD_FILE = XMLHelper.writeXMLToString(new ResponseError("407","No permission to load file ''{0}'' for app ''{1}'' ") , false, ResponseError.class);
    public static final String NO_PREVIEW               = XMLHelper.writeXMLToString(new ResponseError("404","File preview.png not found ''{0}'' for app ''{1}''") , false, ResponseError.class);

/*
    public static final String UNKNOWN_ERROR   = HEAD+"<error><code>401</code><description>Unknown Error</description></error>";
    public static final String NO_SUCH_LICENSE = HEAD+"<error><code>402</code><description>Invalid user license: <b>?</b> </description></error>";
    public static final String NO_SUCH_REPORT  = HEAD+"<error><code>403</code><description>Invalid report type: <b>?</b> </description></error>";
 */
}
