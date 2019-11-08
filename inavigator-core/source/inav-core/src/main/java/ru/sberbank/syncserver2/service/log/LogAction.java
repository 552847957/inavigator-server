package ru.sberbank.syncserver2.service.log;

import java.util.Date;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ru.sberbank.syncserver2.gui.util.format.JSPFormatPool;


@XmlRootElement(name = "log")
public class LogAction implements Iterable<String>{
	private Date date;
	private String service;
	private String msg;
	
	public LogAction() {}
	
	public LogAction(Date date, String service, String msg) {
		this.date = date;
		this.service = service;
		this.msg = msg;
	}
	
	@XmlElement
	public Date getDate() {
		return date;
	}
	
	@XmlElement
	public String getService() {
		return service;
	}
	
	@XmlElement
	public String getMsg() {
		return msg;
	}
	
	@Override
	public String toString() {
		return JSPFormatPool.formatDateAndTime(date)+" "+service+" "+msg;		
	}
	
	public void setDate(Date date) {
		this.date = date;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			private int i = 0;

			@Override
			public boolean hasNext() {
				return i<3;
			}

			@Override
			public String next() {
				i++;
				switch (i) {
				case 1: return JSPFormatPool.formatDateAndTime(date);
				case 2: return service;
				case 3: return msg;
				}
				return null;
			}

			@Override
			public void remove() {
			}
			
		};
	}
	
	

}
