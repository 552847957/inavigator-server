package ru.sberbank.syncserver2.gui.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;


//see https://datatables.net/manual/server-side
public abstract class ShowTableController extends ShowHtmlController {
	protected int numberOfColumns;
		
	public ShowTableController(Class<?> loggerClass) {		
        super(loggerClass);
        numberOfColumns=1;
    }	
	
	public void transmit(HttpServletRequest request, HttpServletResponse response) {
		String s = request.getParameter("order[0][column]");
		int index = s==null?0:Integer.parseInt(s);
    	int direction = s==null?1:(request.getParameter("order[0][dir]").equals("asc")?1:-1);
    	int draw = Integer.parseInt(request.getParameter("draw"));
    	int length = Integer.parseInt(request.getParameter("length"));
		int start = Integer.parseInt(request.getParameter("start"));
		List<String> parameters = getSearchParameters(request);
		if (request.getParameter("getFile")!=null) {	
			SearchResult res = search(request, index, direction, parameters, 0, -1);
			generateFile(request, response, res);
		} else {
			SearchResult res = search(request, index, direction, parameters, start, length);
			
			//write response
			ObjectMapper mapper = new ObjectMapper();
			PrintWriter out=null;
	        try {					        	
				out = response.getWriter();
				mapper.setDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"));	
				if (out!=null) {
					try {
						JsonGenerator g = new JsonFactory().createJsonGenerator(out);
						g.writeStartObject();
						g.writeNumberField("draw", draw);
						g.writeNumberField("recordsTotal", res.getRecordsTotal());
						g.writeNumberField("recordsFiltered", res.getFiltered());
						g.writeFieldName("data");
						mapper.writeValue(g, res.getResults());	
						g.writeEndObject();				
						g.close();
					} catch (Exception e) {
						e.printStackTrace();
					}		
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
	            if (out != null) {
	                out.flush();
	                out.close();
	            }
	        }
		}	
	}
	
	protected List<String> getSearchParameters(HttpServletRequest request) {
		List<String> parameters = new ArrayList<String>();		
		for (int i=0; i<numberOfColumns; i++) {
			if ("true".equals(request.getParameter("columns["+i+"][searchable]"))) {					
				String value = request.getParameter("columns["+i+"][search][value]");
				parameters.add(value);			
			} 
		}
		return parameters;
	}
	
	protected abstract SearchResult search(HttpServletRequest request, int orderCol, int direction, List<String> searchValues, int startIndex, int numberOfMessages);
	protected abstract void generateFile(HttpServletRequest request, HttpServletResponse response, SearchResult searchResult);
	
	protected class SearchResult {
		private List<?> results;
		private int recordsTotal;
		private int filtered;
		public SearchResult(List<?> results, int recordsTotal, int filtered) {
			this.results = results;
			this.recordsTotal = recordsTotal;
			this.filtered = filtered;
		}
		public List<?> getResults() {
			return results;
		}
		public int getRecordsTotal() {
			return recordsTotal;
		}
		public int getFiltered() {
			return filtered;
		}		
	}

}
