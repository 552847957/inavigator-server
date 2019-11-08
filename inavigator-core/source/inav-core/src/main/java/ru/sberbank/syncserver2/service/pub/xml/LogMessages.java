package ru.sberbank.syncserver2.service.pub.xml;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import ru.sberbank.syncserver2.service.log.LogAction;


@XmlRootElement(name = "getLogMessages")
public class LogMessages extends Message implements Cloneable {

	private String tag;	
	
	private List<LogAction> logs;
	private String source;
	private Date dateFrom;
	private Date dateTo;
	private String searchWithMsg;
	private Integer page;
	private Integer maxPage;
	
	public LogMessages() {
	}
	
	public LogMessages(String tag) {
		this.tag = tag;
	}
	
	@XmlElement
	public String getTag() {
		return tag;
	}	

	public void setTag(String tag) {
		this.tag = tag;
	}

	@XmlElementWrapper(name="logs")
	@XmlElementRef()
	public List<LogAction> getLogs() {
		return logs;
	}

	public void setLogs(List<LogAction> logs) {
		this.logs = logs;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}	

	public Integer getMaxPage() {
		return maxPage;
	}

	public void setMaxPage(Integer maxPage) {
		this.maxPage = maxPage;
	}

	public String getSearchWithMsg() {
		return searchWithMsg;
	}

	public void setSearchWithMsg(String searchWithMsg) {
		this.searchWithMsg = searchWithMsg;
	}	
	
	public LogMessages clone() {
		try {
			return (LogMessages) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

}
