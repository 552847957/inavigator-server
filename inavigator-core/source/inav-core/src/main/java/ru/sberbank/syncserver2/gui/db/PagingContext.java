package ru.sberbank.syncserver2.gui.db;

import ru.sberbank.syncserver2.gui.util.JSPHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class PagingContext implements IPagingContext {

	public static final int DEFAUL_PAGE_SIZE = 10;

	private String contextName;

	private int pageSize;

	private String uniqueColumn;

    private String uniqueColumnSortOrder;

    private List sortableColumns;

	private List filterableColumns;

	private boolean enabled;

	private int pagingType;

	private List pageHintStarts;

	private List pageHintEnds;

	private Map pageRecordCounts;

	private int pageIndex = 1;

	private int recordCount;

	private List sortedColumns;

	private List columnDescriptions;

	private List sortDirections;

	private List filteredColumns;

	private List filterTypes;

	private List filteringValues;

	private DefaultPageHintFormat format;

	private String url;

	public void dump() {
		System.out.println("**********************DUMPING PAGING CONTEXT**********************");
		System.out.println("Context Name = " + contextName);
		System.out.println("Page Size = " + pageSize);
		System.out.println("Page Index = " + pageIndex);
		System.out.println("Record Count = " + recordCount);
		System.out.println("sort columns = " + sortedColumns);
		System.out.println("sort directions = " + sortDirections);
		System.out.println("All Columns = " + sortableColumns);
		for (int i = 0; i < sortableColumns.size(); i++)
			System.out.println(sortableColumns.get(i));
		System.out.println("********************************************");
	}

	public PagingContext(String contextName, int pageSize, String uniqueColumn, String[] sortableColumns,
		String[] filterableColumns) {
		//1. Allocate memory
		this.contextName = contextName;
		this.pageSize = pageSize;

		this.uniqueColumn = uniqueColumn;
        this.uniqueColumnSortOrder = ASCENDING;
        List columns = new ArrayList();
		if (sortableColumns != null && sortableColumns.length > 0) {
			for (String c : sortableColumns) {
				columns.add(c);
			}
		}
		this.sortableColumns = columns;
		columns = new ArrayList();
		if (filterableColumns != null && filterableColumns.length > 0) {
			for (String c : filterableColumns) {
				columns.add(c);
			}
		}
		this.filterableColumns = columns;
		this.enabled = true;
		this.pagingType = IPagingContext.NUMBER_PAGING;
		this.format = new DefaultPageHintFormat();
		this.pageHintStarts = new ArrayList();
		this.pageHintEnds = new ArrayList();
		this.pageRecordCounts = new HashMap();
		this.sortedColumns = new ArrayList();
		this.sortDirections = new ArrayList();
		this.filteredColumns = new ArrayList();
		this.filterTypes = new ArrayList();
		this.filteringValues = new ArrayList();
	}

	public void parseRequest(HttpServletRequest req) {
		//1. Restore paging variables from request
		Object requestPageIndex = req.getParameter(contextName + "_pageIndex");
		if (requestPageIndex != null) {
			int pageIndex = Integer.parseInt(String.valueOf(requestPageIndex));
			setPageIndex(pageIndex);
		}

		Object requestPageSize = req.getParameter(contextName + "_pageSize");
		if (requestPageSize != null) {
			int pageSize = Integer.parseInt(String.valueOf(requestPageSize));
			this.pageSize = pageSize;
		}

		Object requestPaginate = req.getParameter(contextName + "_paginate");
		if (requestPaginate != null) {
			if ("false".equalsIgnoreCase((String) requestPaginate)) {
				setEnabled(false);
			} else if ("true".equalsIgnoreCase((String) requestPaginate)) {
				setEnabled(true);
			}
		}

		//2. Restore sorted columns
		sortedColumns.clear();
		sortDirections.clear();
		for (int i = 0; i < sortableColumns.size(); i++) {
			String columnName = (String) sortableColumns.get(i);
			String sortedColumnName = composeSortedColumnName(columnName);
			String sortDirection = (String) req.getParameter(sortedColumnName);
			if (sortDirection != null) {
				if (ASCENDING.equalsIgnoreCase(sortDirection)) {
					sortedColumns.add(columnName.toUpperCase());
					sortDirections.add(ASCENDING);
				} else if (DESCENDING.equalsIgnoreCase(sortDirection)) {
					sortedColumns.add(columnName.toUpperCase());
					sortDirections.add(DESCENDING);
				}
			}
		}

		if (sortedColumns.size() == 0) {
			sortedColumns.add(uniqueColumn.toUpperCase());
			sortDirections.add(uniqueColumnSortOrder);
		}

		//3. Restore filtered columns
		filteredColumns.clear();
		filterTypes.clear();
		filteringValues.clear();
		for (int i = 0; i < filterableColumns.size(); i++) {
			String columnName = (String) filterableColumns.get(i);
			String filterTypeName = composeFilterTypeName(columnName);
			String filterType = (String) req.getParameter(filterTypeName);
			String filteringValueName = composeFilteringValueName(columnName);
			String filteringValue = (String) req.getParameter(filteringValueName);
			if (filteringValue != null && filterType != null) {
				if (EQUALS_NUMBER.equalsIgnoreCase(filterType)) {
					filteredColumns.add(columnName.toUpperCase());
					filterTypes.add(EQUALS_NUMBER);
					filteringValues.add(filteringValue);
				} else if (EQUALS_STRING.equalsIgnoreCase(filterType)) {
					filteredColumns.add(columnName.toUpperCase());
					filterTypes.add(EQUALS_STRING);
					filteringValues.add(filteringValue);
				} else if (CONTAINS_STRING.equalsIgnoreCase(filterType)) {
					filteredColumns.add(columnName.toUpperCase());
					filterTypes.add(CONTAINS_STRING);
					filteringValues.add(filteringValue);
				}
			}
		}
	}

	public String composeQueryString() {

		StringBuilder res = new StringBuilder();

		res.append(contextName + "_pageIndex=" + pageIndex);

		if (res.length() > 0) {
			res.append('&');
		}
		res.append(contextName + "_paginate=" + enabled);

		for (int i = 0; i < sortedColumns.size(); i++) {
			res.append('&');
			res.append(composeSortedColumnName((String) sortedColumns.get(i))).append("=")
				.append(sortDirections.get(i));
		}

		String filteredColumnName;
		for (int i = 0; i < filteredColumns.size(); i++) {
			filteredColumnName = (String) filteredColumns.get(i);
			res.append('&');
			res.append(composeFilterTypeName(filteredColumnName) + "=" + filterTypes.get(i) + "&"
				+ composeFilteringValueName(filteredColumnName) + "=" + filteringValues.get(i));
		}

		return res.toString();
	}

	public static String getParameter(String queryString, String key) {
		int i = queryString.indexOf(key);
		if (i >= 0) {
			int j = queryString.indexOf('=', i) + 1, k = queryString.indexOf('&', j);
			if (j > i) {
				if (k > j) {
					return queryString.substring(j, k);
				} else {
					return queryString.substring(j);
				}
			}
		}
		return null;
	}

	public void parseQueryString(String queryString) {
		//1. Restore paging variables from request
		Object requestPageIndex = getParameter(queryString, contextName + "_pageIndex");
		if (requestPageIndex != null) {
			int pageIndex = Integer.parseInt(String.valueOf(requestPageIndex));
			setPageIndex(pageIndex);
		}
		Object requestPaginate = getParameter(queryString, contextName + "_paginate");
		//by default we have paging
		if (requestPaginate != null) {
			if ("false".equalsIgnoreCase((String) requestPaginate)) {
				setEnabled(false);
			} else if ("true".equalsIgnoreCase((String) requestPaginate)) {
				setEnabled(true);
			}
		}

		//2. Restore sort columns
		sortedColumns.clear();
		sortDirections.clear();
		for (int i = 0; i < sortableColumns.size(); i++) {
			String columnName = (String) sortableColumns.get(i);
			String sortColumnName = composeSortedColumnName(columnName);
			String sortDirection = getParameter(queryString, sortColumnName);
			if (sortDirection != null) {
				if (ASCENDING.equalsIgnoreCase(sortDirection)) {
					sortedColumns.add(columnName.toUpperCase());
					sortDirections.add(ASCENDING);
				} else if (DESCENDING.equalsIgnoreCase(sortDirection)) {
					sortedColumns.add(columnName.toUpperCase());
					sortDirections.add(DESCENDING);
				}
			}
		}

		if (sortedColumns.size() == 0) {
			sortedColumns.add(uniqueColumn.toUpperCase());
			sortDirections.add(uniqueColumnSortOrder);
		}

		//3. Restore filtered columns
		filteredColumns.clear();
		filterTypes.clear();
		filteringValues.clear();
		for (int i = 0; i < filterableColumns.size(); i++) {
			String columnName = (String) filterableColumns.get(i);
			String filterTypeName = composeFilterTypeName(columnName);
			String filterType = (String) getParameter(queryString, filterTypeName);
			String filteringValueName = composeFilteringValueName(columnName);
			String filteringValue = (String) getParameter(queryString, filteringValueName);
			if (filteringValue != null && filterType != null) {
				if (EQUALS_NUMBER.equalsIgnoreCase(filterType)) {
					filteredColumns.add(columnName.toUpperCase());
					filterTypes.add(EQUALS_NUMBER);
					filteringValues.add(filteringValue);
				} else if (EQUALS_STRING.equalsIgnoreCase(filterType)) {
					filteredColumns.add(columnName.toUpperCase());
					filterTypes.add(EQUALS_STRING);
					filteringValues.add(filteringValue);
				} else if (CONTAINS_STRING.equalsIgnoreCase(filterType)) {
					filteredColumns.add(columnName.toUpperCase());
					filterTypes.add(CONTAINS_STRING);
					filteringValues.add(filteringValue);
				}
			}
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getPagingType() {
		return pagingType;
	}

	public void setPagingType(int pagingType) {
		this.pagingType = pagingType;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isClient() {
		return pagingType == IPagingContext.ALPHARANGE_PAGING || pagingType == IPagingContext.ALPHABET_PAGING;
	}

	public List getColumnDescriptions() {
		return Collections.unmodifiableList(columnDescriptions);
	}

	public void setColumnDescriptions(List columnComments) {
		this.columnDescriptions = new ArrayList(columnComments);
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageCount() {
		return pageSize < 1 ? 1 : (int) Math.ceil(((double) recordCount) / pageSize);
	}

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	public int getPageSize() {
		return pageSize;
	}

	public String getContextName() {
		return contextName;
	}

	public String getUniqueColumn() {
		return uniqueColumn;
	}

	public void setUniqueColumn(String uniqueColumn) {
		this.uniqueColumn = uniqueColumn;
	}

	public List getSortableColumns() {
		return Collections.unmodifiableList(sortableColumns);
	}

	public List getSortedColumns() {
		return Collections.unmodifiableList(sortedColumns);
	}

	public List getSortDirections() {
		return Collections.unmodifiableList(sortDirections);
	}

	public List getFilterableColumns() {
		return Collections.unmodifiableList(filterableColumns);
	}

	public List getFilteredColumns() {
		return Collections.unmodifiableList(filteredColumns);
	}

	public List getFilterTypes() {
		return Collections.unmodifiableList(filterTypes);
	}

	public List getFilteringValues() {
		return Collections.unmodifiableList(filteringValues);
	}

	public String getFilteringValue(String column) {

		for (int i = 0; i < filteredColumns.size(); i++) {
			if (((String) filteredColumns.get(i)).equalsIgnoreCase(column)) {
				return (String) filteringValues.get(i);
			}
		}

		return "";
	}

	public boolean isFilteredColumn(String column) {

		for (int i = 0; i < filteredColumns.size(); i++) {
			if (((String) filteredColumns.get(i)).equalsIgnoreCase(column)) {
				return true;
			}
		}

		return false;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void addSortedColumn(String sortedColumn, String sortDirection) {
		//1. Checking arguments
		String upperSortColumn = sortedColumn.toUpperCase();
		if (!sortableColumns.contains(upperSortColumn)) {
			throw new IllegalArgumentException("Column name " + sortedColumn + " is not supported ");
		}
		if (sortedColumns.contains(upperSortColumn)) {
			throw new IllegalArgumentException("Column name " + sortedColumn + " is already in sort column list ");
		}
		if (!ASCENDING.equalsIgnoreCase(sortDirection) && !DESCENDING.equalsIgnoreCase(sortDirection)) {
			throw new IllegalArgumentException("Sort order " + sortDirection + " is not supported ");
		}

		//2. Addign column
		sortedColumns.add(upperSortColumn);
		sortDirections.add(sortDirection.equalsIgnoreCase(ASCENDING) ? ASCENDING : DESCENDING);
	}

	public String composePaginate(Boolean flag) {
		if (flag == null)
			return contextName + "_paginate=" + enabled;
		return contextName + "_paginate=" + flag.booleanValue();
	}

	public String composePageSizeQuery(Integer size) {
		if (size == null) {
			return contextName + "_pageSize=" + pageSize;
		}
		return contextName + "_pageSize=" + size;
	}

	public String composePageIndexQuery(int pageIndex) {
		return contextName + "_pageIndex=" + pageIndex;
	}

	public String composePageDescription(int pageIndex) {
		//1. Ignore non-client contexts
		if (pagingType != ALPHARANGE_PAGING || pageIndex < 1 || pageHintStarts.size() <= pageIndex - 1) {
			return null;
		}

		//2. If no sort columns then there is no hint
		if (sortedColumns.size() == 0) {
			return null;
		}
		String columnName = (String) sortedColumns.get(0);
		int index = sortableColumns.indexOf(columnName);
		String columnCaption = (String) (index >= 0 && index < columnDescriptions.size() ? columnDescriptions
			.get(index) : columnName);

		//2. Composing hint using format
		String start = (String) pageHintStarts.get(pageIndex - 1);
		String end = (String) (pageHintEnds.size() > pageIndex - 1 ? pageHintEnds.get(pageIndex - 1) : null);
		return format.format(pageIndex, columnName, columnCaption, start, end);
	}

	public String composeSortedColumnName(String column) {
		return contextName + "_order_by_" + column;
	}

	public String composeActiveSorting() {
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < sortedColumns.size(); i++) {
			res.append(composeSortQuery((String) sortedColumns.get(i), (String) sortDirections.get(i)) + "&");
		}
		return res.length() > 0 ? res.substring(0, res.length() - 1) : res.toString();
	}

	public String composeSortQuery(String column, String order) {
		return composeSortedColumnName(column) + "=" + order;
	}

	public String composeFilteringValueName(String column) {
		return contextName + "_filter_value_" + column;
	}

	public String composeFilterTypeName(String column) {
		return contextName + "_filter_type_" + column;
	}

	public String composeFilterQuery(String column, String type) {

		StringBuilder res = new StringBuilder();

		String filteredColumnName;
		for (int i = 0; i < filteredColumns.size(); i++) {
			if (!(filteredColumnName = (String) filteredColumns.get(i)).equalsIgnoreCase(column)) {
				res.append('&');
				res.append(composeFilterTypeName(filteredColumnName) + "=" + filterTypes.get(i) + "&"
					+ composeFilteringValueName(filteredColumnName) + "=" + filteringValues.get(i));
			}
		}

		if (column != null && type != null && !NONE.equalsIgnoreCase(type)) {
			res.append('&');
			res.append(composeFilterTypeName(column) + "=" + type + "&" + composeFilteringValueName(column) + "=");
		}

		return res.toString();
	}

	public String composeFilterQuery() {

		return composeFilterQuery(null, null);
	}

	private String composeFilterSQL() {

		if (filteredColumns.size() > 0) {
			StringBuffer filter = new StringBuffer();
			for (int i = 0; i < filteredColumns.size(); i++) {
				if (EQUALS_NUMBER.equalsIgnoreCase((String) filterTypes.get(i))) {
					filter.append(filteredColumns.get(i));
					filter.append('=');
					filter.append(filteringValues.get(i));
					if (i + 1 < filteredColumns.size()) {
						filter.append(" AND ");
					}
				} else if (EQUALS_STRING.equalsIgnoreCase((String) filterTypes.get(i))) {
					filter.append(filteredColumns.get(i));
					filter.append("='");
					filter.append(filteringValues.get(i));
					filter.append('\'');
					if (i + 1 < filteredColumns.size()) {
						filter.append(" AND ");
					}
				} else if (CONTAINS_STRING.equalsIgnoreCase((String) filterTypes.get(i))) {
					filter.append("lower(");
					filter.append(filteredColumns.get(i));
					filter.append(") like lower('%");
					filter.append(filteringValues.get(i));
					filter.append("%')");
					if (i + 1 < filteredColumns.size()) {
						filter.append(" AND ");
					}
				}
			}

			return filter.toString();
		}

		return "";
	}

	public String composeCountSQL(String sql) {

		if (filteredColumns.size() > 0) {
			int i = sql.toLowerCase().indexOf("where");
			if (i > 0) {
				StringBuffer filteringSql = new StringBuffer(sql.substring(0, i + 6));
				filteringSql.append('(');
				filteringSql.append(sql.substring(i + 6));
				filteringSql.append(") AND ");
				filteringSql.append(composeFilterSQL());

				sql = filteringSql.toString();

			} else {

				sql += " WHERE " + composeFilterSQL();
			}
		}

		return "SELECT COUNT(*) FROM (" + sql + ") t";
	}

	public String composePageSQL(String sql) {
		//1. Checking pageCount
		StringBuffer sbDirect = new StringBuffer();
//		StringBuffer sbReverse = new StringBuffer();

		//2. Adding sort conidtions
		if (sortedColumns.size() > 0) {
			for (int i = 0; i < sortedColumns.size(); i++) {
				String sortColumn = (String) sortedColumns.get(i);
				sbDirect.append(sortColumn);
//				sbReverse.append(sortColumn);

				String sortOrder = (String) sortDirections.get(i);
				sbDirect.append(" ");
//				sbReverse.append(" ");
				if (ASCENDING.equalsIgnoreCase(sortOrder)) {
					sbDirect.append(ASCENDING);
//					sbReverse.append(DESCENDING);
				} else {
					sbDirect.append(DESCENDING);
//					sbReverse.append(ASCENDING);
				}
				if (i + 1 < sortedColumns.size()) {
					sbDirect.append(",");
//					sbReverse.append(",");
				}
			}
		}
		if (!sortedColumns.contains(uniqueColumn.toUpperCase()) && !JSPHelper.isStringEmpty(uniqueColumn)) {
			if (sortedColumns.size() > 0) {
				sbDirect.append(",");
			}
			sbDirect.append(uniqueColumn);
			sbDirect.append(" ");
			sbDirect.append(uniqueColumnSortOrder);
		}

		if (filteredColumns.size() > 0) {
			int i = sql.toLowerCase().indexOf("where");
			if (i > 0) {
				StringBuffer filteringSql = new StringBuffer(sql.substring(0, i + 6));
				filteringSql.append('(');
				filteringSql.append(sql.substring(i + 6));
				filteringSql.append(") AND ");
				filteringSql.append(composeFilterSQL());

				sql = filteringSql.toString();

			} else {

				sql += " WHERE " + composeFilterSQL();
			}
		}

		if (pagingType == NUMBER_PAGING) {
			int begin = enabled ? pageSize * (pageIndex - 1) < recordCount ? pageSize * (pageIndex - 1) : 0 : 0, end = enabled ? pageSize
				* pageIndex < recordCount ? pageSize * pageIndex : recordCount
				: recordCount;

			String pagingSQL = "SELECT * FROM (SELECT S.*, ROWNUM AS TEMP_ROW_NUMBER FROM (" + sql + " ORDER BY " + sbDirect
				+ ") S WHERE ROWNUM <= " + end + ") WHERE TEMP_ROW_NUMBER > " + begin + " ORDER BY " + sbDirect;
            System.out.println("PAGING SQL = "+pagingSQL);
            return pagingSQL;
        } else {
			return sql + " ORDER BY " + sbDirect;
		}
	}

	public String getAlphaRangePageCode(int pageIndex) {
		if (pageIndex == 0) {
			return "";
		} else {
			char symbol = (char) ('A' + (pageIndex - 1));
			return String.valueOf(symbol);
		}
	}

	public String getAlphaRangePageName(int pageIndex) {
		if (pageIndex == 0) {
			return "<blank>";
		} else {
			char symbol = (char) ('A' + (pageIndex - 1));
			return String.valueOf(symbol);
		}
	}

	public int getAlphaRangePageCount(int pageIndex) {
		String code = getAlphaRangePageCode(pageIndex);
		Integer count = (Integer) pageRecordCounts.get(code);
		return count == null ? 0 : count.intValue();
	}

	/**
	 * @param rs
	 *            recordset to parse record from
	 * @param recordIndex
	 *            0-based record index i.e. first record index should be 0
	 * @return true if record should be shown at page
	 * @throws SQLException
	 */
	public boolean processRecordAtClient(ResultSet rs, int recordIndex) throws SQLException {
		//1. All records should be used for disabled paging
		if (!enabled) {
			return true;
		}

		//2. Processing alpharange mode
		if (pagingType == ALPHARANGE_PAGING) {
			//2.1. If no sort column then we couldn't make hints
			if (sortedColumns.size() > 0) {
				String columnName = (String) sortedColumns.get(0);
				if ((recordIndex % pageSize) == 0) {
					pageHintStarts.add(rs.getString(columnName));
				} else if ((recordIndex % pageSize) == pageSize - 1) {
					pageHintEnds.add(rs.getString(columnName));
				}
			}
			this.recordCount = recordIndex + 1;
			return recordIndex >= (pageIndex - 1) * pageSize && recordIndex < pageIndex * pageSize;
		} else if (pagingType == ALPHABET_PAGING) {
			if (sortedColumns.size() > 0) {
				String columnName = (String) sortedColumns.get(0);
				String columnValue = rs.getString(columnName);
				String code = JSPHelper.isStringEmpty(columnValue) ? " " : columnValue.toUpperCase().substring(0, 1);
				if (code.charAt(0) <= 'A') {
					code = "A";
				} else if (code.charAt(0) >= 'Z') {
					code = "Z";
				}
				Integer count = (Integer) pageRecordCounts.get(code);
				Integer newCount = new Integer(count == null ? 1 : count.intValue() + 1);
				pageRecordCounts.put(code, newCount);
				this.recordCount = recordIndex + 1;
				return code.equalsIgnoreCase(getAlphaRangePageCode(pageIndex));
			} else {
				return false; //will work incorrectly if no sorting defined
			}
		}
		return false;
	}

    public void setUniqueColumnSortOrder(String uniqueColumnSortOrder) {
        this.uniqueColumnSortOrder = uniqueColumnSortOrder;
        //System.out.println("SET "+this.uniqueColumnSortOrder);
    }

    private static class DefaultPageHintFormat implements PageHintFormat {

		public String format(int pageIndex, String columnName, String columnDesc, String start, String end) {
			return "Page " + pageIndex + " : " + (JSPHelper.isStringEmpty(start) ? "&lt;blank&gt;" : start) + " - "
				+ (JSPHelper.isStringEmpty(end) ? "&lt;blank&gt;" : end);
		}
	}

}
