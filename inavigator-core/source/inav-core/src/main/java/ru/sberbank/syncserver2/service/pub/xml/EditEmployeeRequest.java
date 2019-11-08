package ru.sberbank.syncserver2.service.pub.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ru.sberbank.syncserver2.gui.data.Employee;

@XmlRootElement(name="editEmployee")
public class EditEmployeeRequest extends Message{
	private String email;
	private Employee employee;
	private int command;
	public final static int ADD = 0;
	public final static int EDIT = 1;
	public final static int CHANGE_PASSWORD = 2;
	public final static int DELETE = 3;
	@XmlElement
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@XmlElement
	public Employee getEmployee() {
		return employee;
	}
	public void setEmployee(Employee employee) {
		this.employee = employee;	
	}
	@XmlElement
	public int getCommand() {
		return command;
	}
	public void setCommand(int command) {
		this.command = command;
	}
	

}
