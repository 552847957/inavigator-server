package ru.sberbank.syncserver2.service.monitor.data;


public class RequestInfo {
	protected final String name;
	protected final String request;
	
	public RequestInfo(String name, String request) {
		this.name= name;
		this.request = request;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRequest() {
		return request;
	}
		
	@Override
	public String toString() {		
		return request;
	}
}
