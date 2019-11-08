package ru.sberbank.syncserver2.gui.util.format;

import org.apache.log4j.Logger;

import java.text.*;
import java.util.*;
/**
 * Created by IntelliJ IDEA. User: leo Date: 27-Aug-2005 Time: 09:33:23 To
 * change this template use File | Settings | File Templates.
 */
public class JSPFormat {

    private static final Logger log = Logger.getLogger(JSPFormat.class);

    public static String[] VALID_DATE_FORMAT = {"yyyy-MM-dd",
                            "dd.MM.yyyy",
                            "yyyyMMdd",
                            "dd-MM-yyyy"};
    public static String DATE_FORMAT = "dd.MM.yyyy";
    public static String ORACLE_DATE_FORMAT = "yyyy-MM-dd";
    public static String TIME_FORMAT = "HH:mm:ss";
    public static String TIME_FORMAT_HHMM = "HH:mm";
    public static String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
	public static String TIMESTAMP_FORMAT = "dd.MM.yyyy HH:mm";

    public static String WEEK_NUMBER_FORMAT = "w yyyy";

    public static String TIVOLI_DATE_FORMAT = "1yyMMdd";


    private SimpleDateFormat timestampFormater = new SimpleDateFormat(TIMESTAMP_FORMAT);
    private SimpleDateFormat multyDateFormater = new SimpleDateFormat(DATE_FORMAT);
    private SimpleDateFormat dateFormater      = new SimpleDateFormat(DATE_FORMAT);
    private SimpleDateFormat timeFormater      = new SimpleDateFormat(TIME_FORMAT);
    private SimpleDateFormat dateTimeFormater  = new SimpleDateFormat(DATE_TIME_FORMAT);

    public static String MONEY_FORMAT = "0.00";

    private DecimalFormat moneyFormatter = new DecimalFormat(MONEY_FORMAT);
    private char decimalSeparator = moneyFormatter.getDecimalFormatSymbols().getDecimalSeparator();

    public static String MONTH_FORMAT = "yyyy, MMM";
    public static String[] MONTHS = new DateFormatSymbols(Locale.getDefault()).getMonths();
    private SimpleDateFormat monthFormatter = new SimpleDateFormat(MONTH_FORMAT);

    public static String MONTH_FORMAT_EXT = "MMMMM yyyy";
    private SimpleDateFormat monthFormatterExt = new SimpleDateFormat(MONTH_FORMAT_EXT);

    public static String MONTH_FORMAT_SMALL = "MM.yyyy";
    private SimpleDateFormat monthFormatterSmall = new SimpleDateFormat(MONTH_FORMAT_SMALL);

    public static String PERC_FORMAT = "#.00";
    private NumberFormat decimalFormat;

    private static final Map<String, String> int2days = new HashMap<String,String>() {
        {
            put("1","Пн");
            put("2","Вт");
            put("3","Ср");
            put("4","Чт");
            put("5","Пт");
            put("6","Сб");
            put("7","Вс");
        }
    };

    public static String getDayOfWeek(String s) {
        return int2days.get(s);
    }

    JSPFormat() {
	}

    public String formatDate(Date date) {
        return date == null ? "" : dateFormater.format(date);
    }

    public String formatWeek(Date date) {
        if (date == null) {
            return "";
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int weekNumber = c.get(Calendar.WEEK_OF_YEAR);
        
        c.add(Calendar.DAY_OF_YEAR, Calendar.MONDAY - dayOfWeek);
        Date weekStart = c.getTime();
        
        c.add(Calendar.DAY_OF_YEAR, 6);
        Date weekEnd = c.getTime();

        return weekNumber + " (" + dateFormater.format(weekStart) + " - " + dateFormater.format(weekEnd) + ")";
    }

    public String formatTime(Date date) {
        return date == null ? "" : timeFormater.format(date);
    }

    public String formatDateTime(Object date) {
        return date == null ? "" : dateTimeFormater.format(date);
    }

	public String formatTimestamp(Date date) {
		return date == null ? "" : timestampFormater.format(date);
	}

    public String formatMonthSmall(Date date) {
        if(date==null){
            return "";
        }
        Calendar cal = monthFormatterSmall.getCalendar();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        return String.valueOf(100+month+1).substring(1)+"."+year;
    }

    public String formatMonth(Date date) {
        return date == null ? "" : monthFormatter.format(date);
    }

    public String formatMonthExt(Date date) {
        return date == null ? "" : monthFormatterExt.format(date);
    }

    public Date parseMonthExt(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }

        Date result = null;
        try {
            result = monthFormatterExt.parse(date);
        } catch (ParseException e) {
            log.debug("Date can't be parsed from string '" + date + "'");
        }

        return result;
    }

	public String formatMoney(double money) {
		return moneyFormatter.format(money).replace(decimalSeparator, '.');
	}

    private String removeZeros(String result) {
        if(result.indexOf(".")>0){
            while(result.endsWith("0")){
                result=result.substring(0, result.length()-1);
            }
            if(result.endsWith(".")){
                result=result.substring(0, result.length()-1);
            }
        }
        return result;
    }

    public double parseMoney(String cost) throws ParseException {
		return moneyFormatter.parse(cost.replace('.', decimalSeparator).replace(',', decimalSeparator)).doubleValue();
	}

    public Date parseDate(String dateAsString) {
        try {
            return dateFormater.parse(dateAsString);
        } catch (ParseException e) {
            log.debug("Date can't be parsed from string '" + dateAsString + "'");
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public Date parseDateMultyFormat(String dateAsString) {
        try {
            return multyDateFormater.parse(dateAsString);
        } catch (ParseException e) {
        }
        for(String formatStr: VALID_DATE_FORMAT){
            multyDateFormater=new SimpleDateFormat(formatStr);
            try {
                return multyDateFormater.parse(dateAsString);
            } catch (ParseException e) {
            }
        }
        log.debug("Date can't be parsed from string '" + dateAsString + "' with any of following format"+VALID_DATE_FORMAT);
        return null;
    }

    public Date parseDateTime(String dateTimeAsString) {
        try {
            return dateTimeFormater.parse(dateTimeAsString);
        } catch (ParseException e) {
            log.debug("Date can't be parsed from string '" + dateTimeAsString + "'");
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public String formatMoneyWithDot(double money) {
		return moneyFormatter.format(money).replace(decimalSeparator, '.');
	}

    public Date parseTime(String startDateAsString) throws ParseException {
        return timestampFormater.parse(startDateAsString);
    }

    public Date parseTimeOrDate(String startDateAsString) throws ParseException {
        ParseException e0;
        try{
        return timestampFormater.parse(startDateAsString);
        }catch (ParseException e){
            e0=e;
        }
        try{
            return dateFormater.parse(startDateAsString);
        }catch (ParseException e){}
        try{
            return dateTimeFormater.parse(startDateAsString);
        }catch (ParseException e){}
        throw e0;
    }

    public Date parseDateFormatSmall(String dateAsString) {
        Date parsedDate = null;
        try {
            parsedDate = monthFormatterSmall.parse(dateAsString);
        } catch (ParseException e) {
        }

        return parsedDate;
    }

    public Date parseDateFormatExt(String dateAsString) {
        Date parsedDate = null;
        try {
            parsedDate = monthFormatterExt.parse(dateAsString);
        } catch (ParseException e) {
        }

        return parsedDate;
    }

    public String formatFileSize(int fileSize) {
        if(fileSize<1000){
            return fileSize+" bytes";
        } else {
            int k = fileSize/1000;
            return k+" Kb";
        }
    }

    /**
     * Return <code>ROUGH</code> day difference. This approach is a problematic with daylight saving
     * @param start
     * @param end
     * @return
     */
    public int getDayDifference(Date start, Date end){
        //1. If one of dates is undefined then we return 0 - no difference
        if(start==null || end==null){
            return 0;
        }

        //2. Otherwise we return approximate result
        long millis = (end.getTime() - start.getTime());
        long minutes = millis/1000/60;
        long days = minutes / 60 / 24;
        return (int) days;
    }

    public static String formatPercent(double value) {
        return new DecimalFormat("#.00").format(value);
    }

    public static String formatPercentWithDot(double value) {
        String p = new DecimalFormat("#.00").format(value);
        return p.replace(",",".");
    }

    public Date getFirstDayOfMonth(Date date){
        Calendar calendar = dateFormater.getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY , 0);
        calendar.set(Calendar.MINUTE      , 0);
        calendar.set(Calendar.SECOND      , 0);
        calendar.set(Calendar.MILLISECOND , 0);
        Date firstDay = calendar.getTime();
        return firstDay;
    }

    public Date getFirstDayOfYear(Date date){
        Calendar calendar = dateFormater.getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH       , 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY , 0);
        calendar.set(Calendar.MINUTE      , 0);
        calendar.set(Calendar.SECOND      , 0);
        calendar.set(Calendar.MILLISECOND , 0);
        Date firstDay = calendar.getTime();
        return firstDay;
    }

    public Date getMonday(Date date){
        Calendar calendar = dateFormater.getCalendar();
        calendar.setTime(date);
        while(calendar.get(Calendar.DAY_OF_WEEK)!=Calendar.MONDAY){
            calendar.add(Calendar.DATE, -1);
        }
        return calendar.getTime();
    }

    public Calendar getDateFormatCalendar(){
        return dateFormater.getCalendar();
    }

    public NumberFormat getDecimalFormat() {
        if (decimalFormat == null) {
            NumberFormat f;
            try {
                f = NumberFormat.getInstance(Locale.ENGLISH);
                f.setMinimumFractionDigits(2);
                f.setMaximumFractionDigits(2);
                f.setGroupingUsed(false);
                decimalFormat = f;
            } catch (Exception e) {
                log.error("Can't get number format", e);
            }
        }

        return decimalFormat;
    }

    public static void main(String[] args) {
        SimpleDateFormat formatWeek = new SimpleDateFormat(WEEK_NUMBER_FORMAT);
        SimpleDateFormat formatDate = new SimpleDateFormat(DATE_FORMAT);

        Date today = new Date();

        System.out.println("Today: " + formatDate.format(today));
        System.out.println("Week : " + formatWeek.format(today));

        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);

        System.out.println("Year begin: " + formatDate.format(c.getTime()));
        System.out.println("Week : " + formatWeek.format(c.getTime()));

        c.set(Calendar.MONTH, Calendar.DECEMBER);
        c.set(Calendar.DAY_OF_MONTH, 31);

        System.out.println("Year end: " + formatDate.format(c.getTime()));
        System.out.println("Week : " + formatWeek.format(c.getTime()));

    }
}
