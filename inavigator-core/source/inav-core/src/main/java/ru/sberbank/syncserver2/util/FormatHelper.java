package ru.sberbank.syncserver2.util;

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
    private static SimpleDateFormat dateTimeFormatterWithTimeZone = new SimpleDateFormat("dd.MM.yyyy HH:mm Z");

    public synchronized static String formatDate(Date d){
        return d==null ? "":dateFormatter.format(d);
    }

    public synchronized static String formatTime(Date d){
        return d==null ? "":timeFormatter.format(d);
    }

    public synchronized static String formatDateTime(Date d){
        return d==null ? "":dateTimeFormatter.format(d);
    }

    public synchronized static String formatDateTimeWithTimeZone(Date d){
        return d==null ? "":dateTimeFormatterWithTimeZone.format(d);
    }

    public static void main(String[] args) {
        System.out.println(formatDateTimeWithTimeZone(new Date()));
    }

    public static String stringConcatenator(Object ... args) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : args) {
            if (obj != null) {
                sb.append(obj.toString());
            } else {
                sb.append("null");
            }
        }
        return sb.toString();
    }
}
