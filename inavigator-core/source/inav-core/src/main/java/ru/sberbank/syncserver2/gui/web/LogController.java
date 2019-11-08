package ru.sberbank.syncserver2.gui.web;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang3.StringUtils;

import ru.sberbank.syncserver2.gui.util.format.JSPFormatPool;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.log.CSVImpl;
import ru.sberbank.syncserver2.service.log.GeneratorLogFile;
import ru.sberbank.syncserver2.service.log.LogAction;
import ru.sberbank.syncserver2.service.log.TagBuffers;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

public class LogController extends ShowTableController {
	protected static final List<Comparator<LogAction>> COMPARATORS;
	protected static final List<Filter> FILTERS;
	static {		
		COMPARATORS = new ArrayList<Comparator<LogAction>>();
		FILTERS = new ArrayList<Filter>();	
		COMPARATORS.add(new Comparator<LogAction>() {
			@Override
			public int compare(LogAction o1, LogAction o2) {
				if (o1.getDate().after(o2.getDate())) 
					return 1;
				if (o1.getDate().before(o2.getDate()))
					return -1;
				return 0;
			}
			
		});
		COMPARATORS.add(new Comparator<LogAction>() {
			@Override
			public int compare(LogAction o1, LogAction o2) {
				return o1.getService().compareTo(o2.getService());
			}
			
		});
		COMPARATORS.add(new Comparator<LogAction>() {
			@Override
			public int compare(LogAction o1, LogAction o2) {
				return o1.getMsg().compareTo(o2.getMsg());
			}
			
		});
		FILTERS.add(new Filter() {			
			@Override
			public boolean test(LogAction entry, String s) {
				String[] dates = s.split("&");
				if (dates[0].length()>15) {
					dates[0] = dates[0].substring(0, 16);
				}
				Date date1 = JSPFormatPool.parseDate(dates[0]);
				Date date2 = JSPFormatPool.parseDate(dates[1].substring(1));
				
				boolean ans = true;
				if (date1!=null) 
					ans = ans && entry.getDate().after(date1);
				if (date2!=null) 
					ans = ans && entry.getDate().before(date2); 
				return ans;
			}
		});
		FILTERS.add(new Filter() {			
			@Override
			public boolean test(LogAction entry, String s) {	
				return StringUtils.containsIgnoreCase(entry.getService(),s);
				
			}
		});
		FILTERS.add(new Filter() {			
			@Override
			public boolean test(LogAction entry, String s) {	
				return StringUtils.containsIgnoreCase(entry.getMsg(),s);
				
			}
		});
	}

	public LogController() {
		super(LogController.class);
		numberOfColumns=3;
	}

	@Override
	protected ModelAndView showForm(HttpServletRequest request,
			HttpServletResponse response, BindException errors)
			throws Exception {
		String servletPath = HttpRequestUtils.getFulRequestPath(request);
        if(servletPath.contains("tags.logs.gui") ){
            return showTagList(request, response, errors);
        } else if(servletPath.contains("actions.logs.gui") ){
            return showActionList(request, response, errors);
        } else {
            return new ModelAndView("index");
        }
	}

	private ModelAndView showActionList(HttpServletRequest request,
			HttpServletResponse response, BindException errors) {
		//1. Get list of all actions
		if ("true".equals(request.getParameter("json"))) {
			transmit(request, response);
	        return null;
		} else {
        String tag = request.getParameter("tag");      
		ModelAndView mv = new ModelAndView("listLogActions");
		mv.addObject("tag", tag);
		return mv;
		}
	}

	private ModelAndView showTagList(HttpServletRequest request,
			HttpServletResponse response, BindException errors) {
		//1. Get list of all tags
		ServiceManager serviceManager = ServiceManager.getInstance();
        List<String> tags = TagBuffers.listTags();
        List<String> services = serviceManager.getAllServiceCodes();
        ServiceFirstComparator comparator = new ServiceFirstComparator(services);
        Collections.sort(tags, comparator);

        //2. Filter for page
        int pageIndex;
        try {
            String s = request.getParameter("pageIndex");
            pageIndex = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            pageIndex = 0;
        }
        int startIndex = pageIndex*50;
        int endIndex = Math.min((pageIndex+1)*50,tags.size());
        List pageTags = Collections.EMPTY_LIST;
        if(startIndex<tags.size()){
            pageTags = tags.subList(startIndex, endIndex);
        }

        //3. Display
        ModelAndView mv = new ModelAndView("listLogTags");
		mv.addObject("tags", pageTags);
        mv.addObject("pageIndex",pageIndex);
		return mv;
	}

	private static class ServiceFirstComparator implements Comparator<String> {
        private Set<String> services;

        private ServiceFirstComparator(List<String> services) {
            this.services = new HashSet<String>(services);
        }

        @Override
        public int compare(String s1, String s2) {
            s1 = services.contains(s1) ? "1.services."+s1:"2.files"+s1;
            s2 = services.contains(s2) ? "1.services."+s1:"2.files"+s2;
            return s1.compareTo(s2);
        }
    }
	
	protected void order(List<LogAction> entries, int index, int direction) {	
		if (direction==-1) {
			Collections.sort(entries, Collections.reverseOrder(COMPARATORS.get(index)));
		} else {
			Collections.sort(entries, COMPARATORS.get(index));
		}	
	}
	
	protected List<LogAction> getFiltered(List<String> parameters, List<LogAction> entries) {
		List<LogAction> filtered = new ArrayList<LogAction>();
		for (int j=0; j<entries.size(); j++) {
			LogAction entry = entries.get(j);
			boolean isFit = false;			
			int i=0;
			while (i<numberOfColumns && (isFit = parameters.get(i).equals("") || FILTERS.get(i).test(entry, parameters.get(i)))) {
				i++;				
			}
			if (isFit) {
				filtered.add(entry);
			}
		}
		return filtered;
	}
	
	
	protected interface Filter {
		public boolean test(LogAction entry, String s);		
	}


	@Override
	protected SearchResult search(HttpServletRequest request, int orderCol,
			int direction, List<String> searchValues, int startIndex,
			int numberOfMessages) {
		String tag = request.getParameter("tag");
		List<LogAction> actions = TagBuffers.listActions(tag);
		int size = actions.size();
		actions = getFiltered(searchValues, actions);
		order(actions, orderCol, direction);
		int filtered = actions.size();
		if (numberOfMessages>0) {
			int endIndex = Math.min(startIndex+numberOfMessages, filtered);
			if (startIndex<endIndex)
				actions = actions.subList(startIndex, endIndex);
			else 
				actions = Collections.emptyList();
		}
		return new SearchResult(actions, size,filtered);
	}

	@Override
	protected void generateFile(HttpServletRequest request,
			HttpServletResponse response, SearchResult searchResult) {
		GeneratorLogFile logFile = new CSVImpl();		
    	logFile.generateFile(response, (List<LogAction>) searchResult.getResults());
		
	}
	
}
