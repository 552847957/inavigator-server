package ru.sberbank.syncserver2.service.generator.single.data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 17.01.12
 * Time: 15:28
 * To change this template use File | Settings | File Templates.
 */
public class FormatHelper {
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public synchronized static String formatDate(Date d){
        return d==null ? "":dateFormatter.format(d);
    }

    public synchronized static String formatTime(Date d){
        return d==null ? "":timeFormatter.format(d);
    }

    public synchronized static String formatDateTime(Date d){
        return d==null ? "":dateTimeFormatter.format(d);
    }
}
