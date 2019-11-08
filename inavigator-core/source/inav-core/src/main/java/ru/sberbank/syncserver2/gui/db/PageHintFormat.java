package ru.sberbank.syncserver2.gui.db;

/**
 * Created by IntelliJ IDEA. User: leo Date: Mar 26, 2006 Time: 8:38:26 PM To
 * change this template use File | Settings | File Templates.
 */
public interface PageHintFormat {

	public String format(int pageIndex, String columnName, String columnDesc, String start, String end);
}
