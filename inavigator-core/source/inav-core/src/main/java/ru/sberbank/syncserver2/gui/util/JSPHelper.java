package ru.sberbank.syncserver2.gui.util;


import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import ru.sberbank.syncserver2.gui.data.CustomChildItem;
import ru.sberbank.syncserver2.gui.util.format.JSPFormat;
import ru.sberbank.syncserver2.gui.util.format.JSPFormatPool;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

public class JSPHelper {

    public static final String STR_SELECT_VALUE            = "-Выберите значение-";
    public static final String STR_VALUE_IS_NOT_SELECTED   = "Значение не выбрано";
    public static final String STR_YES                     = "Да";
    public static final String STR_NO                      = "Нет";
    public static final String STR_MISTAKE                 = "Ошибка";

    public static boolean isStringEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

    public static String composeCustomSelect(String selectOptions, List childItems, int selectedValue) {
        String s = String.valueOf(selectedValue);
        return composeCustomSelect(selectOptions, childItems, s);
    }

    public static String composeCustomSelect(String selectOptions, List childItems, String selectedValue) {
        return composeCustomSelect(selectOptions, childItems, null, selectedValue, true);
    }

    public static String composeCustomSelect(String selectOptions, List<CustomChildItem> childItemsEnabled,
        List<CustomChildItem> childItemsDisabled, String selectedValue, boolean isEnabled) {

        List<CustomChildItem> childItems = isEnabled? childItemsEnabled : childItemsDisabled;
        StringBuffer sb = new StringBuffer("<select ");
        if (!isEnabled) {
            sb.append("disabled=\"disabled\"");
        }
        sb.append(selectOptions);
        sb.append(">");
        for (int i = 0; i < childItems.size(); i++) {
            CustomChildItem item = childItems.get(i);
            sb.append("<option value=\"" + item.getId() + "\" class=\"tag_" + item.getTag() + "\"" + (StringUtils.equals(item.getId(), selectedValue) ? "selected>" : ">"));
            sb.append(item.getCaption());
            sb.append("</option>");
        }
        sb.append("</select>");
        return sb.toString();
    }

    public static String composeCustomStyledSelect(String selectOptions, List<CustomChildItem> items, List<CustomChildItem> styleItems, int selectedId) {
            String selectedIdStr = String.valueOf(selectedId);
            StringBuilder sb = new StringBuilder("<select ");
            sb.append(selectOptions);
            sb.append(">");
            for (int i = 0; i < items.size(); i++) {
                CustomChildItem item = items.get(i);
                CustomChildItem style = styleItems.get(i);
                sb.append("<option ")
                    .append("style=\"").append(style.getCaption()).append("\" ")
                    .append("value=\"").append(item.getId()).append("\" ")
                    .append(StringUtils.equals(item.getId(), selectedIdStr) ? "selected" : "")
                    .append(">")
                    .append(item.getCaption())
                    .append("</option>");
            }
            sb.append("</select>");
            return sb.toString();
        }

    public static String composeHTMLSelect(String selectOptions, int[] values, String[] texts, int selectedIndex) {
		StringBuffer sb = new StringBuffer("<select ");
		sb.append(selectOptions);
		sb.append(">");
		for (int i = 0; i < Math.max(values.length, texts.length); i++) {
			sb.append("<option value=\"" + values[i] + "\"" + (i == selectedIndex ? "selected>" : ">"));
			sb.append(texts[i]);
			sb.append("</option>");
		}
		sb.append("</select>");
		return sb.toString();
	}

	public static String composeHTMLSelect(String selectOptions, String[] values, String[] texts, int selectedIndex) {
		StringBuffer sb = new StringBuffer("<select ");
		sb.append(selectOptions);
		sb.append(">");
		for (int i = 0; i < Math.max(values.length, texts.length); i++) {
			sb.append("<option value=\"" + values[i] + "\"" + (i == selectedIndex ? "selected>" : ">"));
			sb.append(texts[i]);
			sb.append("</option>");
		}
		sb.append("</select>");
		return sb.toString();
	}

	public static String composeHTMLSelect(String selectOptions, String[] values, String[] texts, String emptyValue,
		String emptyText, int selectedIndex) {
		StringBuffer sb = new StringBuffer("<select ");
		sb.append(selectOptions);
		sb.append(">\"<option value=\"" + emptyValue + "\" " + (selectedIndex == -1 ? "selected>" : ">"));
		sb.append(emptyText);
		sb.append("</option>");
		for (int i = 0; i < Math.max(values.length, texts.length); i++) {
			sb.append("<option value=\"" + values[i] + "\"" + (i == selectedIndex ? "selected>" : ">"));
			sb.append(texts[i]);
			sb.append("</option>");
		}
		sb.append("</select>");
		return sb.toString();
	}

    public static String composeHTMLSelect(String selectOptions, String[] values, String[] texts, String emptyValue,
       String emptyText, String selectedValue) {
        //1. Looking for selected value
        int selectedIndex = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(selectedValue)) {
                selectedIndex = i;
                break;
            }
        }

        //2. Compose combo
        return composeHTMLSelect(selectOptions, values, texts, emptyValue, emptyText, selectedIndex);
    }

    public static String composeHTMLSelect(String selectOptions, String[] values, String[] texts, String selectedValue) {
		//1. Looking for selected value
		int selectedIndex = -1;
		for (int i = 0; i < values.length; i++) {
			if (values[i].equalsIgnoreCase(selectedValue)) {
				selectedIndex = i;
				break;
			}
		}

		//2. Compose combo
		return composeHTMLSelect(selectOptions, values, texts, selectedIndex);
	}

    public static String composeHTMLRadios(String name, String[] values, String[] texts, String selectedValue) {
        return composeHTMLRadios(name, values, texts, selectedValue, false);
    }

    public static String composeHTMLRadios(String name, String[] values, String[] texts, String selectedValue, boolean vertical) {
        //1. Compose text
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < Math.min(values.length, texts.length); i++) {
            sb.append("<input type=\"radio\" name=\""+name+"\" value=\""+values[i]+"\" ");
            if(values[i].equalsIgnoreCase(selectedValue)){
                sb.append(" checked");
            }
            sb.append(" > ");
            sb.append(texts[i]);
            if(vertical){
                sb.append("<br>");
            }
        }
        return sb.toString();
    }

    public static String composeHTMLRadios(String name, List childItems, int selectedValue, boolean vertical) {
        return composeHTMLRadios(name, childItems, String.valueOf(selectedValue), vertical);
    }

    public static String composeHTMLRadios(String name, List childItems, char selectedValue, boolean vertical) {
        return composeHTMLRadios(name, childItems, String.valueOf(selectedValue), vertical);
    }

    public static String composeHTMLRadios(String name, List childItems, String selectedValue, boolean vertical) {
        //1. Looking for selected value
        int selectedIndex = -1;
        for (int i = 0; i < childItems.size(); i++) {
            CustomChildItem item = (CustomChildItem) childItems.get(i);
            if (item.getId().equalsIgnoreCase(selectedValue)) {
                selectedIndex = i;
                break;
            }
        }

        //2. Compose text
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < childItems.size(); i++) {
            CustomChildItem item = (CustomChildItem) childItems.get(i);
            sb.append("<input type=\"radio\" name=\""+name+"\" value=\""+item.getId()+"\" ");
            if(i==selectedIndex){
                sb.append(" checked > ");
            } else {
                sb.append(" > ");
            }
            sb.append(item.getCaption());
            if(vertical){
                sb.append("<br>");
            }
        }
        return sb.toString();
    }

    public static String composeHTMLRadios(String name, String[] values, String[] texts, int selectedValue) {
        String val = String.valueOf(selectedValue);
        return composeHTMLRadios(name, values, texts, val);
    }

    public static String composeHTMLRadios(String name, String[] values, String[] texts, int selectedValue, boolean vertical) {
        String val = String.valueOf(selectedValue);
        return composeHTMLRadios(name, values, texts, val, vertical);
    }

    public static String composeHTMLRadios(String name, int[] values, String[] texts, String selectedValue) {
        String[] strValues = new String[values.length];
        for (int i = 0; i < strValues.length; i++) {
            strValues[i] = String.valueOf(values[i]);
        }
        return composeHTMLRadios(name, strValues, texts, selectedValue, false);
    }

    public static String composeHTMLRadios(String name, int[] values, String[] texts, int selectedValue) {
        String val = String.valueOf(selectedValue);
        return composeHTMLRadios(name, values, texts, val);
    }

    public static String composeCheckboxes(String htmlName, List childItems, String htmlValue, boolean vertical) {
        StringBuffer sb = new StringBuffer("");
        List checkedIds = JSPHelper.isStringEmpty(htmlValue) ? Collections.EMPTY_LIST :Arrays.asList(StringUtils.split(htmlValue,','));
        for (int i = 0; i < childItems.size(); i++) {
            CustomChildItem item = (CustomChildItem) childItems.get(i);
            boolean checked = checkedIds.contains(item.getId());
            sb.append("<nobr>");
            sb.append("<input type=\"checkbox\" value=\""+item.getId()+"\" name=\"" + htmlName + "\" " + (checked ? "checked>" : ">"));
            sb.append(item.getCaption());
            sb.append("</nobr>");
            if(vertical){
                sb.append("<br>");
            }
        }
        sb.append("");
        return sb.toString();
    }

    public static String composeCheckboxes(String htmlName, List childItems, String htmlValue, List bold, int maxHorizontal) {
        StringBuffer sb = new StringBuffer("");
        List checkedIds = JSPHelper.isStringEmpty(htmlValue) ? Collections.EMPTY_LIST :Arrays.asList(StringUtils.split(htmlValue,','));
        for (int i = 0; i < childItems.size(); i++) {
            CustomChildItem item = (CustomChildItem) childItems.get(i);
            boolean checked = checkedIds.contains(item.getId());
            sb.append("<nobr>");
            sb.append("<input type=\"checkbox\" value=\""+item.getId()+"\" name=\"" + htmlName + "\" " + (checked ? "checked>" : ">"));
            if(bold.contains(item.getCaption())){
                sb.append("<font size=+1><b>");
                sb.append(item.getCaption());
                sb.append("</b></font>");
            } else {
                sb.append(item.getCaption());
            }
            sb.append("</nobr>");
            if(i>0 && i%maxHorizontal==0){
                sb.append("<br>");
            }
        }
        sb.append("");
        return sb.toString();
    }

    public static String composeCheckboxes(String htmlName, String action, String[] items, String[] checkedItems, int maxHorizontal) {
        StringBuffer sb = new StringBuffer("");
        List checkedItemsList = Arrays.asList(checkedItems);
        sb.append("<table>");
        for (int i = 0; i < items.length; i++) {
            boolean checked = checkedItemsList.contains(items[i]);
            if (i%maxHorizontal == 0) {
                sb.append("<tr>");
            }
            sb.append("<td>");
            sb.append("<input type=\"checkbox\" value=\""+items[i]+"\" name=\"" + htmlName + "\" " + action + (checked ? " checked>" : ">"));
            sb.append(items[i]);
            sb.append("</td>");
            if((i+1)%maxHorizontal==0){
                sb.append("</tr>");
            }
        }
        sb.append("</table>");
        return sb.toString();
    }


    public static String composeAllCheckedCheckboxes(String htmlName, List childItems, boolean vertical) {
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < childItems.size(); i++) {
            CustomChildItem item = (CustomChildItem) childItems.get(i);
            sb.append("<nobr>");
            sb.append("<input type=\"checkbox\" value=\""+item.getId()+"\" name=\"" + htmlName + "\" checked >");
            sb.append(item.getCaption());
            sb.append("</nobr>");
            if(vertical){
                sb.append("<br>");
            }
        }
        sb.append("");
        return sb.toString();
    }

    public static String composeCheckboxesForSpring(String htmlName, List childItems, String htmlValue, boolean vertical) {
        StringBuffer sb = new StringBuffer("");
        List checkedIds = JSPHelper.isStringEmpty(htmlValue) ? Collections.EMPTY_LIST : Arrays.asList(StringUtils.split(htmlValue, ','));
        for (int i = 0; i < childItems.size(); i++) {
            CustomChildItem item = (CustomChildItem) childItems.get(i);
            boolean checked = checkedIds.contains(item.getId());
            sb.append("<nobr>");
            sb.append("<input type=\"checkbox\" value=\""+item.getId()+"\" name=\"" + htmlName+"["+item.getId()+"]" + "\" " + (checked ? "checked>" : ">"));
            sb.append(item.getCaption());
            sb.append("</nobr>");
            if(vertical){
                sb.append("<br>");
            }
        }
        sb.append("");
        return sb.toString();
    }

    public static String getGlobalErrorsMsg(Errors errors) {
        List<ObjectError> fieldErrors = errors.getGlobalErrors();
        StringBuilder sb = new StringBuilder();
        Iterator<ObjectError> i = fieldErrors.iterator();
        while (i.hasNext()) {
            sb.append(i.next().getCode());
            if (i.hasNext())
                sb.append(";");
        }
        return sb.toString();
    }

    public static String getAllFieldErrors(Errors errors, String fieldName) {
		List fieldErrors = errors.getFieldErrors(fieldName);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fieldErrors.size(); i++) {
			FieldError e = (FieldError) fieldErrors.get(i);
			if ("typeMismatch".equals(e.getCode())) {
			    sb.append("Неверное значение поля");
			} else {
	            sb.append(e.getCode());
			}
			if (i < fieldErrors.size()) {
				sb.append("<p>");
			}
		}
		return sb.toString();
	}

	public static String getAllFieldErrors(Errors errors, String[] fieldNames) {
		StringBuffer sb = new StringBuffer();
		for (int f = 0; f < fieldNames.length; f++) {
			String fieldName = fieldNames[f];
			List fieldErrors = errors.getFieldErrors(fieldName);
			for (int i = 0; i < fieldErrors.size(); i++) {
				FieldError e = (FieldError) fieldErrors.get(i);
				sb.append(e.getCode());
				if (i < fieldErrors.size()) {
					sb.append("<p>");
				}
			}
		}
		return sb.toString();
	}

	public static String getAllFieldErrors(Errors errors, String fieldName, String defaultFieldDesc) {
		List fieldErrors = errors.getFieldErrors(fieldName);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fieldErrors.size(); i++) {
			FieldError e = (FieldError) fieldErrors.get(i);
			if (e.isBindingFailure()) {
				sb.append(defaultFieldDesc);
			} else {
				sb.append(e.getCode());
			}
			if (i < fieldErrors.size()) {
				sb.append("<p>");
			}
		}
		return sb.toString();
	}
    public static String getAllFieldErrors(Errors errors, String fieldName, String defaultFieldDesc, String prefix) {
        List<FieldError> fieldErrors = errors.getFieldErrors(fieldName);
        StringBuffer sb = new StringBuffer();
        for (FieldError e : fieldErrors) {
            sb.append(prefix).append(": ");
            if (e.isBindingFailure()) {
                sb.append(defaultFieldDesc);
            } else {
                sb.append(e.getCode());
            }
            sb.append("<br/>");
        }
        return sb.toString();
    }

	public static String getAllFieldErrors(Errors errors, String[] fieldNames, String[] defaultFieldDescs) {
		StringBuffer sb = new StringBuffer();
		for (int f = 0; f < fieldNames.length; f++) {
			String fieldName = fieldNames[f];
			List fieldErrors = errors.getFieldErrors(fieldName);
			for (int i = 0; i < fieldErrors.size(); i++) {
				FieldError e = (FieldError) fieldErrors.get(i);
				if (e.isBindingFailure()) {
					sb.append(defaultFieldDescs[i]);
				} else {
					sb.append(e.getCode());
				}
				if (i < fieldErrors.size()) {
					sb.append("<p>");
				}
			}
		}
		return sb.toString();
	}

	public static String[] alignSelectTexts(String[] texts, int length) {
		StringBuffer[] aligned = new StringBuffer[texts.length];
		for (int i = 0; i < aligned.length; i++) {
			aligned[i] = new StringBuffer(texts[i]);
			for (int j = texts[i].length(); j < length; j++) {
				aligned[i].append("&nbsp;");
			}
		}
		String[] result = new String[texts.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = aligned[i].toString();
		}
		return result;
	}

	public static String convertCharToCode(String tmp) {
		if (tmp == null)
			return "";
		StringBuffer res = new StringBuffer(100);
		StringTokenizer st = new StringTokenizer(tmp, "<>$\"'()~", true);
		while (st.hasMoreTokens()) {
			tmp = st.nextToken();
			if (tmp.equals("("))
				res.append("&#40;");
			else if (tmp.equals(")"))
				res.append("&#41;");
			else if (tmp.equals("~"))
				res.append("&#126;");
			else if (tmp.equals("\""))
				res.append("&quot;");
			else if (tmp.equals("'"))
				res.append("&#39;");
			else if (tmp.equals("<"))
				res.append("&lt;");
			else if (tmp.equals(">"))
				res.append("&gt;");
			else if (tmp.equals("$"))
				res.append("&#36;");
			else
				res.append(tmp);
		}
		return res.toString();
	}

	public static int getIntegerParameter(HttpServletRequest request, String name, int defaultValue) {
		try {
			return Integer.parseInt(request.getParameter(name));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

    public static Integer getIntegerParameter(HttpServletRequest request, String name) {
        Integer value = null;

        try {
            value = Integer.parseInt(request.getParameter(name));
        } catch (NumberFormatException e) {
        }

        return value;
    }

    public static Date getDateParameter(HttpServletRequest request, String name, DateFormat format) {
        Date result = null;
        try {
            String value = request.getParameter(name);
            if (value != null) {
                result =  format.parse(value);
            }
        } catch (ParseException e) {
        }

        return result;
    }

    public static double getDoubleParameter(HttpServletRequest request, String name, double defaultValue) {
        try {
            String value = request.getParameter(name);
            return value==null ? defaultValue : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBooleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
        return Boolean.parseBoolean(request.getParameter(name));
    }

    public static Integer[] getIntegerParameterArray(HttpServletRequest request, String name) {
        try {
            String[] values = request.getParameterValues(name);
            Integer[] intValues = new Integer[values == null ? 0 : values.length];
            for (int i = 0; i < intValues.length; i++) {
                intValues[i] = Integer.parseInt(values[i]);
            }
            return intValues;
        } catch (NumberFormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return new Integer[0];
    }


    public static int[] composeIntArrayFromBeans(List values, Class c, String methodName){
        int[] result = new int[values.size()];
        try {
            Method m = c.getMethod(methodName);
            for (int i = 0; i < values.size(); i++) {
                Object o = values.get(i);
                Integer value = (Integer) m.invoke(o);
                result[i] = value.intValue();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }

    public static String[] composeStringArrayFromBeans(List values, Class c, String methodName){
        String[] result = new String[values.size()];
        try {
            Method m = c.getMethod(methodName);
            for (int i = 0; i < values.size(); i++) {
                Object o = values.get(i);
                Object value = m.invoke(o);
                result[i] = String.valueOf(value);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }

    public static String[] composeStringArrayFromIntArray(int[] values){
        String[] result  = new String[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = String.valueOf(values[i]);
        }
        return result;
    }

    public static String[] composeStringArrayFromIntArrayAndBean(int[] values, Class c, String methodName){
        String[] result  = new String[values.length];
        try {
            Method m = c.getMethod(methodName, Integer.TYPE);
            for (int i = 0; i < values.length; i++) {
               Object text = m.invoke(null, values[i]);
               result[i] = String.valueOf(text);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }

    public static String replaceQueryString(String url, String id, String value){
        //1. Processing simple case when there is no query string in url
        int questionIndex = url.indexOf('?');
        if(questionIndex==-1 ||questionIndex==url.length()-1){
            return url + "?"+id+"="+value;
        }

        //2. Processing complex case
        boolean replaced = false;
        String servlet = url.substring(0, questionIndex);
        String qs = url.substring(questionIndex+1);
        String[] params = qs.split("&");
        for (int i = 0; i < params.length; i++) {
            if(params[i].startsWith(id+"=")){
                params[i] = id+"=" + value;
                replaced = true;
            }
        }

        //3. Composing back
        if(!replaced){
            return url+"&"+id+"="+value;
        }
        StringBuilder sb = new StringBuilder(servlet);
        sb.append("?");
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
            if(i+1<params.length){
                sb.append("&");
            }
        }
        return sb.toString();
    }

    public static String encodeUrls(String src){
        String afterHTTP = encodeUrls(src, "http");
        String afterHTTPS = encodeUrls(afterHTTP, "https");
        return afterHTTPS;
    }

    private static String encodeUrls(String src, String protocol){
        StringBuffer sb = new StringBuffer();
        int start = src.indexOf(" "+protocol+"://");
        int previous = 0;
        while(start>0){
            int end = src.indexOf(' ',start+7);
            if(end<0){
                end = src.length();
            }
            sb.append(src.substring(previous, start));
            String link = src.substring(start,end);
            sb.append("<a href=\"").append(link).append("\">").append(link).append("</a>");
            previous=end;
            start = src.indexOf(previous);
        }
        sb.append(src.substring(previous));
        return sb.toString();
    }

    public static String composeStringFromIntArray(int[] values){
        StringBuffer sb = new StringBuffer(3*values.length*2);
        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            if(i>0){
                sb.append(",");
            }
            sb.append(value);
        }
        return sb.toString();
    }

    public static String composeActionButton(String name, String action){
        String txt = "<input type=\"button\" class=\"gray\" value=\""+name+"\" onclick=\"javascript:"+action+"\">";
        return txt;
    }

    public static String composeActionButton(String name, String action, String gray){
        String txt = "<input type=\"button\" class=\"" + gray +"\" alt=\" "+name+ "\" title=\""+name+"\" value=\""+name+"\" onclick=\"javascript:"+action + "\">" +
//                "       <img src=\"../images/left_sat50.png\"/>"+
                "</input>";
        return txt;
    }

    public static Object getSecond(TreeSet set) {
        if(set==null || set.size()<2){
            return null;
        }
        Iterator iterator = set.iterator();
        iterator.next();
        return iterator.next();
    }

    public static List<CustomChildItem> composeMonthList(int count, boolean descending){
        //1. Set calendar to current month which is not included
        JSPFormat format = JSPFormatPool.get();
        Date thisMonth = format.getFirstDayOfMonth(new Date());
        Calendar calendar = format.getDateFormatCalendar();
        calendar.setTime(thisMonth);

        //2. Adding month by month in decreasing order
        ArrayList<CustomChildItem> result = new ArrayList<CustomChildItem>(count);
        while(count>0){
            //2.1. Format
            calendar.add(Calendar.MONTH, -1);
            Date prevMonth = calendar.getTime();
            String firstDay = format.formatDate(prevMonth);
            String month    = format.formatMonthExt(prevMonth);
            result.add(new CustomChildItem(firstDay, month));

            //2.2. Go next
            count--;
        }

        //3. Resorting if required
        if(!descending){
            ArrayList<CustomChildItem> resortedResult = new ArrayList<CustomChildItem>();
            for (int i = result.size()-1; i>=0; i--){
                CustomChildItem item = result.get(i);
                resortedResult.add(item);
            }
            result = resortedResult;
        }

        //3. Finish work on month list
        JSPFormatPool.release(format);
        return result;
    }

    /**
     * Composes week periods
     *
     * @param count - number of weeks list should be composed of
     * @param weekFirstDay - the day when the week starts. See {@link Calendar#DAY_OF_WEEK}. E.g. Sunday = 1.
     * @return
     */
    public static List<CustomChildItem> composeWeekList(int count, int weekFirstDay) {
        List<CustomChildItem> weekList = new LinkedList<CustomChildItem>();

        JSPFormat format = JSPFormatPool.get();

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, weekFirstDay);
        c.add(Calendar.WEEK_OF_YEAR, -1);
        for (int i = 0; i < count; i++) {
            String firstDay = format.formatDate(c.getTime());
            c.add(Calendar.DAY_OF_YEAR, 6);
            String lastDay = format.formatDate(c.getTime());
            weekList.add(new CustomChildItem(firstDay, firstDay + " - " + lastDay));
            c.add(Calendar.DAY_OF_YEAR, -13);
        }

        JSPFormatPool.release(format);

        return weekList;
    }

    public void testComposeWeekList() {
        System.out.println(composeWeekList(3, 2));
    }

    public static String convertYesNoField(String yesNoValue) {
        if ("Y".equals(yesNoValue)) {
            return STR_YES;
        } else if ("N".equals(yesNoValue)) {
            return STR_NO;
        } else {
            return STR_MISTAKE + " (" + yesNoValue + ")";
        }
    }

    public static List<CustomChildItem> addEmptyOption(List<CustomChildItem> list) {
        return addEmptyOption(list, STR_SELECT_VALUE);
    }

//    public static List<CustomChildItem> addOption(List<CustomChildItem> list, String id, String name) {
//        return addEmptyOption(list, STR_SELECT_VALUE);
//    }

    public static List<CustomChildItem> addNotSelectedOption(List<CustomChildItem> list) {
        return addEmptyOption(list, STR_VALUE_IS_NOT_SELECTED);
    }

    public static List<CustomChildItem> addEmptyOption(List<CustomChildItem> list, String value) {
        return addOption(list, "-1", value);
    }

    public static List<CustomChildItem> addOption(List<CustomChildItem> list, String id, String value) {
        LinkedList<CustomChildItem> resultList = new LinkedList<CustomChildItem>();

        resultList.add(new CustomChildItem(id, value));
        if (list != null) {
            resultList.addAll(list);
        }

        return resultList;
    }

    public static List<CustomChildItem> removeOption(List<CustomChildItem> list, String id) {
        LinkedList<CustomChildItem> resultList = new LinkedList<CustomChildItem>();

        for (CustomChildItem item : list) {
            if (!ObjectUtils.equals(item.getId(), id)) {
                resultList.add(item);
            }
        }

        return resultList;
    }

    public static String getCustomChildName(List<CustomChildItem> items, Object id) {
        if (items == null) {
            return STR_VALUE_IS_NOT_SELECTED;
        }

        String key = id == null? null : String.valueOf(id);
        int index = -1;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equalsIgnoreCase(key)) {
                index = i;
            }
        }
        if (index == -1) {
            return STR_VALUE_IS_NOT_SELECTED;
        }

        CustomChildItem value = items.get(index);

        return value == null? "" : value.getCaption();
    }

    public static List<CustomChildItem> format(String format, List<CustomChildItem> items) {
        if (items == null) {
            return null;
        }

        for (CustomChildItem item : items) {
            if (item != null) {
                String caption = item.getCaption();
                item.setCaption(String.format(format, caption));
            }
        }

        return items;
    }
}
