package ru.sberbank.syncserver2.gui.util;

import java.util.Date;

import ru.sberbank.syncserver2.service.generator.single.data.ActionState;

public class TimeIntervalHelper {
	
	public static String getFormatedTimeInterval(long from, long to) {		
		return getFormatedTimeInterval(to-from);
	}
	
	public static String getFormatedTimeInterval(long interval) {
		long d = interval/(24*60*60*1000);
		long h = interval/(60*60*1000)%24;
		long m = interval/(60*1000)%60;
		long s = interval/(1000)%60;
		String str = "";
		if (d==0 && h==0 && m==0 && s==0 && interval>0) 
			str = "менее 1 секунды";
		else {
			if (d>0) str +=" "+d+"д";
			if (h>0) str +=" "+h+"ч";
			if (m>0) str +=" "+m+"м";
			if (s>0) str +=" "+s+"c";			
		}
		return str;
	}
	
	public static String getFormatedTimeInterval(Date from, Date to) {
		if (from==null || to==null)
			return "";
		return getFormatedTimeInterval(from.getTime(),to.getTime());
	}
	
	public static String getFormatedTimeInterval(String info, Date from, Date to) {
		String result = getFormatedTimeInterval(from, to);
		return result.equals("")?"":info+result;
	}
	
	public static String getFormatedTimeInterval(String info, Date from) {
		return getFormatedTimeInterval(info, from, new Date());
	}

}
