package ru.sberbank.syncserver2.gui.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import ru.sberbank.syncserver2.gui.data.AuditRecord;
import ru.sberbank.syncserver2.gui.data.AuthContext;
import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.gui.util.format.JSPFormatPool;
import ru.sberbank.syncserver2.service.log.CSVImpl;
import ru.sberbank.syncserver2.service.log.GeneratorLogFile;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class AuditController extends ShowTableController{
		
	public AuditController() {
		super(AuditController.class);
		numberOfColumns=6;
	}

	protected String getOrder(int index, int direction) {	
		String dir = direction>0?"ASC":"DESC";	
		switch (index) {
		case 0: return " ORDER BY EVENT_TIME "+dir;
		case 2: return " ORDER BY USER_EMAIL "+dir;
		case 3: return " ORDER BY MODULE "+dir;
		case 4: return " ORDER BY EVENT_TYPE "+dir;
		}
		return "";
	}
	
	protected String getWhere(List<String> parameters) {
		String res = "WHERE ";
		boolean isAdded = false; 
		for (int i=0; i<numberOfColumns; i++) {
			String s = parameters.get(i);
			if (s!=null && !s.equals("")) {
				if (isAdded)
					res+=" AND ";
				switch(i) {
				case 0:
					String[] dates = s.split("&");
					if (dates.length>0 && !dates[0].equals("")) {
						isAdded = true;
						res+="EVENT_TIME > CONVERT(DATETIME, '"+dates[0]+"', 104)";
						if (dates.length>1)
							res+=" AND EVENT_TIME < CONVERT(DATETIME, '"+dates[1]+"', 104)";
					} else
						if (dates.length>1) {
							res+="EVENT_TIME < CONVERT(DATETIME, '"+dates[1]+"', 104)";
							isAdded = true;
						}
					break;
				case 1:
					res+="USER_EMAIL LIKE '%"+s+"%'";
					isAdded = true;
					break;
				case 2:
					res+="MODULE LIKE '%"+s+"%'";
					isAdded = true;
					break;
				case 3:
					res+="EVENT_TYPE LIKE '%"+s+"%'";	
					isAdded = true;
				case 4:
					res+="HOST LIKE '%"+s+"%'";	
					isAdded = true;
				}
			}
		}		
		return isAdded?res:"";
	}
	
	@Override
	protected SearchResult search(HttpServletRequest request, int orderCol,
			int direction, List<String> searchValues, int startIndex,
			int numberOfMessages) {
		String where = getWhere(searchValues);
		String order = getOrder(orderCol, direction);
		List<? extends AuditRecord> actions;
		if (numberOfMessages<0) {
			actions = AuditHelper.getCompleteRecords(where, order, false);
			int[] size = AuditHelper.getSizeOfTable(where);
			return new SearchResult(actions, size[0],size[1]);
		} else {
			actions = AuditHelper.getRecords(where, order, startIndex, numberOfMessages);
			int[] size = AuditHelper.getSizeOfTable(where);
			return new SearchResult(actions, size[0],size[1]);
		}		
	}

	@Override
	protected void generateFile(HttpServletRequest request,
			HttpServletResponse response, SearchResult searchResult) {
		GeneratorLogFile logFile = new CSVImpl();
		logFile.generateFile(response, (List<CompleteAuditRecord>)searchResult.getResults());
	}
	
	
	@Override
	protected ModelAndView showForm(HttpServletRequest request,
			HttpServletResponse response, BindException errors)
			throws Exception {
		String servletPath = HttpRequestUtils.getFulRequestPath(request);
		if (servletPath.contains("description.audit.gui")) {
			try {
				int id = Integer.parseInt(request.getParameter("id"));
				List<CompleteAuditRecord> record = AuditHelper.getCompleteRecords("WHERE EVENT_ID="+Integer.toString(id),"",true);
				String s = composeHtmlView(record.get(0));
				PrintWriter out=null;
		        try {					        	
					out = response.getWriter();
					if (out!=null) {					
						JsonGenerator g = new JsonFactory().createJsonGenerator(out);
						g.writeString(s);	
						g.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
		            if (out != null) {
		                out.flush();
		                out.close();
		            }
		        }	
				return null;
			} catch (NumberFormatException e1) {
	            logger.debug("Missing id - server logic problem");
	            return new ModelAndView("index");
	        }
		}
		if ("true".equals(request.getParameter("table"))) {
			transmit(request, response);
	        return null;
		}
		ModelAndView mv = new ModelAndView("audit");		
		return mv;
	}
	
	@Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o,
		BindException e) throws Exception {
		String servletPath = HttpRequestUtils.getFulRequestPath(request);
		if (servletPath.contains("delete.audit.gui")) {
			String password = request.getParameter("password");
			AuthContext ctx = (AuthContext) request.getSession().getAttribute("user");
			String msg = "Неверный пароль";
			if (AuditHelper.isValidUser(ctx.getEmployee().getEmployeeEmail(), password)) {
				msg = "Записи удалены";
				List<String> parameters = new ArrayList<String>();		
				for (int i=0; i<numberOfColumns; i++) {
					if ("true".equals(request.getParameter("columns["+i+"][searchable]"))) {
						String value = request.getParameter("columns["+i+"][search][value]");
						parameters.add(value);	
					} 
				}
				String[] dates = parameters.get(0).split("&");
				if (dates.length>1 && !dates[0].equals("")) {
					String where = getWhere(parameters);
					AuditHelper.deleteRecords(where);
					AuditHelper.write(request, "Удаление записей", "Удалены записи с "+dates[0]+" по "+dates[1]+getCondition(parameters),AuditHelper.AUDIT);
				} else {
					msg = "Необходимо указать обе даты";
				}				
			}
			PrintWriter out=null;
	        try {					        	
				out = httpServletResponse.getWriter();
				if (out!=null) {					
					out.println("<div class='box-modal' id='descrition'>");
					out.println("<div class='box-modal_close arcticmodal-close'>закрыть</div>");
					out.println(msg);
					out.println("</div>");					
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
	            if (out != null) {
	                out.flush();
	                out.close();
	            }
	        }
			return null;
		}	
		return new ModelAndView("audit");
			
	}
	
	private String getCondition(List<String> params) {
		String res="";
		for (int i=1; i<numberOfColumns; i++) {
			String s = params.get(i);
			if (s!=null && !s.equals("")) {
				if (res.length()>0) res+=",";
				switch(i) {
				case 1:
					res+=" Email содержит '"+s+"'";
					break;
				case 2:
					res+=" Модуль содержит '"+s+"'";
					break;
				case 3:
					res+=" Сообщение содержит '"+s+"'";	
				case 4:
					res+=" от Хоста '"+s+"'";
				}
			}
		}
		if (res.length()>0)
			res = ", удовлетворяющие условию:"+res;
		res+=".";
		return res; 
	}
	
	private String composeHtmlView(CompleteAuditRecord record) {
		//see http://arcticlab.ru/arcticmodal
		String s = "<div class='box-modal' id='descrition'>"+
		"<div class='box-modal_close arcticmodal-close'>закрыть</div>"+
		"<h2> Запись аудита № "+record.getEventId()+"</h2>"+
		"<table id='modalTable'>"+
			"<tr><td colspan='2'><b>Дата и время:</b> "+JSPFormatPool.formatDateAndTime(record.getDate())+"</td></tr>"+
			"<tr><td><b>Хост:</b> "+record.getHost()+"</td><td><b>Модуль:</b> "+record.getModule()+"</td></tr>"+
			"<tr><td><b>Email:</b> "+record.getEmail()+"</td><td><b>IP адрес:</b> "+record.getIpAddress()+"</td></tr>"+ 
			"<tr><td colspan='2'><b>Сообщение:</b> "+record.getEventType()+"</td></tr>"+
			"<tr><td colspan='2'> <br><b>Подробности:</b></td></tr>"+
			"<tr><td colspan='2'>"+record.getDescription().replace("\n", "<br>")+"<br></td></tr>"+			
		"</table>"+
		"</div>";
		return s;
	}	


}
