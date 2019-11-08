package ru.sbt.utils.backup.util;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Утилитный класс по работе с датами
 */
public class DateUtils {
	
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	/**
	 * Получить дату из строки
	 * @param dateStr
	 * @return
	 */
    public static Date parseDate(String dateStr) {
        Date date = null;
        try {
            date = new SimpleDateFormat(DATE_FORMAT).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

	/**
	 * Получить дату из строки
	 * @param dateStr
	 * @return
	 */
    public static String formatDate(Date date) {
    	if (date == null) return null;
    	String dateString = null;
        dateString = new SimpleDateFormat(DATE_FORMAT).format(date);
        return dateString;
    }
    
    /**
     * Перевести дату в строку заданного формата
     * @param date
     * @param format
     * @return
     */
    public static String getFormattedDate(Date date,String format) {
    	try {
    		return new SimpleDateFormat(format).format(date);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	return null;
    }
    
    /**
     * Получить текущую дату ввиде TIMESTAMP
     * @return
     */
    public static java.sql.Timestamp getCurrentTimeStamp(){
        Calendar cal = Calendar.getInstance();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(cal.getTimeInMillis());
        return  timestamp;
    }
    
    public static String sqlDateToString(java.sql.Date date) {
    	if (date == null) return "";
    	return formatDate(new Date(date.getTime()));
    }

    public static String sqlDateToString(java.sql.Timestamp date) {
    	if (date == null) return "";
    	return formatDate(new Date(date.getTime()));
    }
    
    public static java.sql.Date stringToSqlDate(String s) {
    	if (s == null || "".equals(s)) return null;
    	return new java.sql.Date(parseDate(s).getTime());
    }

    public static java.sql.Timestamp stringToTimestamp(String s) {
    	if (s == null || "".equals(s)) return null;
    	return new java.sql.Timestamp(parseDate(s).getTime());
    }


}
