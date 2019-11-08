package ru.sberbank.adminconsole.gui.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;

import ru.sberbank.adminconsole.gui.panels.EmployeeForm;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.gui.services.SwingRemoteRequestSender;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.adminconsole.services.IRemoteDataReceiver;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.service.pub.xml.EditEmployeeRequest;
import ru.sberbank.syncserver2.service.pub.xml.GetEmployeesRequest;
import ru.sberbank.syncserver2.service.pub.xml.Message;
import ru.sberbank.syncserver2.service.pub.xml.Message.Status;
import ru.sberbank.syncserver2.service.pub.xml.SyncEmployeesRequest;

public class EmployeeAction implements ActionListener, IRemoteDataReceiver<GetEmployeesRequest>{
	private DefaultListModel listModel;
	private JList list;
	private Application mainApp;
	private EmployeeForm employeeForm;
	private Collection<Application> applications;
	private SyncEmployee syncEmployeeReceiver;
	private EmployeeUpdater employeeUpdater;

	public EmployeeAction(JList list, DefaultListModel model, JFrame frame) {
		this.list = list;
		listModel = model;
		mainApp = ClientDataManager.getInstance().getMainApplication();
		applications = ClientDataManager.getInstance().getAllApplications();
		employeeForm = new EmployeeForm(frame);
		syncEmployeeReceiver = new SyncEmployee();
		employeeUpdater = new EmployeeUpdater();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Employee employee = (Employee)list.getSelectedValue();
		if (e.getActionCommand().equals("GET")) {
			runGetTask();
		} else if (e.getActionCommand().equals("ADD")) {	
			employee = new Employee();
			if (employeeForm.showAddDialog(employee)) {				
				EditEmployeeRequest request = new EditEmployeeRequest();
				request.setCommand(EditEmployeeRequest.ADD);
				request.setEmployee(employee);
				for (Application a: applications) {
					SwingRemoteRequestSender<EditEmployeeRequest> task = new SwingRemoteRequestSender<EditEmployeeRequest>(a, request, a==mainApp?employeeUpdater:null);
					task.startTask();
				}						
			}			
		}  else if (e.getActionCommand().equals("EDIT")) {	
			if (employee==null) return;
			String oldEmail = employee.getEmployeeEmail();
			if (employeeForm.showEditDialog(employee)) {
				EditEmployeeRequest request = new EditEmployeeRequest();
				request.setCommand(EditEmployeeRequest.EDIT);
				request.setEmployee(employee);
				request.setEmail(oldEmail);
				for (Application a: applications) {
					SwingRemoteRequestSender<EditEmployeeRequest> task = new SwingRemoteRequestSender<EditEmployeeRequest>(a, request, null);
					task.startTask();
				}
			}			
		}  else if (e.getActionCommand().equals("CHANGE_PASSWORD")) {	
			if (employee==null) return;
			if (employeeForm.showChangePasswordDialog(employee)) {
				EditEmployeeRequest request = new EditEmployeeRequest();
				request.setCommand(EditEmployeeRequest.CHANGE_PASSWORD);
				request.setEmployee(employee);
				for (Application a: applications) {
					SwingRemoteRequestSender<EditEmployeeRequest> task = new SwingRemoteRequestSender<EditEmployeeRequest>(a, request, null);
					task.startTask();
				}
			}			
		}  else if (e.getActionCommand().equals("DELETE")) {
			if (employee==null) return;
			EditEmployeeRequest request = new EditEmployeeRequest();
			request.setCommand(EditEmployeeRequest.DELETE);
			request.setEmail(employee.getEmployeeEmail());
			for (Application a: applications) {
				SwingRemoteRequestSender<EditEmployeeRequest> task = new SwingRemoteRequestSender<EditEmployeeRequest>(a, request, a==mainApp?employeeUpdater:null);
				task.startTask();
			}	
		}  else if (e.getActionCommand().equals("SYNC")) {
			SyncEmployeesRequest request = new SyncEmployeesRequest();
			request.setCommand(SyncEmployeesRequest.GET);
			SwingRemoteRequestSender<SyncEmployeesRequest> task = new SwingRemoteRequestSender<SyncEmployeesRequest>(mainApp, request, syncEmployeeReceiver);
			task.startTask();
		} 
		//System.out.println("unknown command "+e.getActionCommand());	
	}
	private void runGetTask() {
		SwingRemoteRequestSender<GetEmployeesRequest> task = new SwingRemoteRequestSender<GetEmployeesRequest>(mainApp, new GetEmployeesRequest(), this);
		task.startTask();
	}

	@Override
	public void submit(GetEmployeesRequest xml) {
		if (xml.getCode()!=Status.OK) return;
		listModel.clear();
		for (Employee empl: xml.getEmployeesList()) {
			listModel.addElement(empl);
		}
		
	}
	
	private class SyncEmployee implements IRemoteDataReceiver<SyncEmployeesRequest> {

		@Override
		public void submit(SyncEmployeesRequest xml) {
			xml.setCommand(SyncEmployeesRequest.SET);
			for (Application a: applications) {
				if (a!=mainApp) {
					SwingRemoteRequestSender<SyncEmployeesRequest> task = new SwingRemoteRequestSender<SyncEmployeesRequest>(a, xml, null);
					task.startTask();
				}
			}				
		}		
	}
	
	private class EmployeeUpdater implements IRemoteDataReceiver<EditEmployeeRequest> {
		@Override
		public void submit(EditEmployeeRequest xml) {
			if (xml.getCode()==Status.OK)
				runGetTask();			
		}		
	}
	

}
