package ru.sberbank.syncserver2.service.pub.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;



import ru.sberbank.syncserver2.gui.data.Employee;

@XmlRootElement(name = "getEmployees")
public class GetEmployeesRequest extends Message{
	private List<Employee> employeesList = new ArrayList<Employee>();
	
	public GetEmployeesRequest() {
	}

	@XmlElementWrapper(name="employees")
	@XmlElement
	public List<Employee> getEmployeesList() {
		return employeesList;
	}

	public void setEmployeesList(List<Employee> employeesList) {
		this.employeesList = employeesList;
	}	

}
