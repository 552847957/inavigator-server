package ru.sberbank.syncserver2.gui.db;

import java.util.List;

public interface IPagingContext {

	public static final String ASCENDING = "asc";

	public static final String DESCENDING = "desc";

	public static final String NONE = "0";

	public static final String EQUALS_NUMBER = "1";

	public static final String EQUALS_STRING = "2";

	public static final String CONTAINS_STRING = "3";

	public static final int NUMBER_PAGING = 1;

	public static final int ALPHARANGE_PAGING = 2;

	public static final int ALPHABET_PAGING = 3;

	public static final int PAGES_ON_SCREEN = 25;

	public int getPageIndex();

	public int getPageCount();

	public int getPageSize();

	public List getSortableColumns();

	public List getSortedColumns();

	public List getSortDirections();

	public List getFilterableColumns();

	public List getFilteredColumns();

	public List getFilterTypes();

	public List getFilteringValues();

	public boolean isFilteredColumn(String column);

	public String getFilteringValue(String column);

	public String composePageIndexQuery(int pageIndex);

	public String composeSortQuery(String column, String order);

	public String composeFilterQuery(String column, String type);

	public String composeFilterQuery();

	public String getUrl();

	public void dump();

	public boolean isEnabled();

	public String composePaginate(Boolean flag);

	public String composeActiveSorting();

	public String composePageSizeQuery(Integer size);

}
