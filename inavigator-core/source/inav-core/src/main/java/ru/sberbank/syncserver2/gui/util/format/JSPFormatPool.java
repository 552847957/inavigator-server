package ru.sberbank.syncserver2.gui.util.format;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;

public class JSPFormatPool {

	private static LinkedList pool = new LinkedList();
	static {
		for (int i = 0; i < 32; i++) {
			pool.add(new JSPFormat());
		}
	}

	private JSPFormatPool() {
	}

	public static synchronized JSPFormat get() {
		return (JSPFormat) (pool.size() > 0 ? pool.removeFirst() : new JSPFormat());
	}

	public static synchronized void release(JSPFormat format) {
		pool.addLast(format);
	}

    public static String formatDate(Date date) {
        JSPFormat jspFormat = JSPFormatPool.get();
        try {
            return jspFormat.formatDate(date);
        } finally {
            JSPFormatPool.release(jspFormat);
        }
    }
    
    public static String formatDateAndTime(Date date) {
        JSPFormat jspFormat = JSPFormatPool.get();
        try {
            return jspFormat.formatTimestamp(date);
        } finally {
            JSPFormatPool.release(jspFormat);
        }
    }

    public static String formatDateAndTime2(Object date) {
        JSPFormat jspFormat = JSPFormatPool.get();
        try {
            return jspFormat.formatDateTime(date);
        } finally {
            JSPFormatPool.release(jspFormat);
        }
    }

    public static Date parseDate(String date) {
        JSPFormat jspFormat = JSPFormatPool.get();
        try {
            return jspFormat.parseTimeOrDate(date);
        } catch (ParseException e) {
            return null;
        } finally {
            JSPFormatPool.release(jspFormat);
        }
    }

}
