package ru.sberbank.syncserver2.service.pub.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


import ru.sberbank.syncserver2.gui.data.Employee;

@XmlRootElement(name="doSyncEmployees")
public class SyncEmployeesRequest extends Message{
	private List<Employee> employees;
	public static final String GET = "GET";
	public static final String SET = "SET";
	private String command;
	@XmlElement
	public List<Employee> getEmployees() {
		return employees;
	}
	public void setEmployees(List<Employee> employees) {
		this.employees = employees;
	}
	@XmlElement
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}	

}
