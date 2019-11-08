package ru.sberbank.adminconsole.gui.panels;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ru.sberbank.syncserver2.gui.data.Employee;

public class EmployeeForm {
	private Employee employeeEdit;
	private boolean ready = false;
	private AddDialog addDialog;
	private EditDialog editDialog;
	private ChangePasswordDialog changePasswordDialog;
	
	public EmployeeForm(JFrame frame) {
		
		editDialog = new EditDialog(frame);
		changePasswordDialog = new ChangePasswordDialog(frame);
		addDialog = new AddDialog(frame);
	}
	
	
	public boolean showAddDialog(Employee employee) {		
		return addDialog.startDialog(employee);	
	}
	
	public boolean showEditDialog(Employee employee) {
		return editDialog.startDialog(employee);		
	}
	
	public boolean showChangePasswordDialog(Employee employee) {
		return changePasswordDialog.startDialog(employee);		
	}	
	
	private boolean isEmpty(JTextField field) {
		String s = field.getText();
		return s==null || s.trim().equals("");
	}
	
	private class EditDialog extends JDialog implements ActionListener {
		private JTextField email = new JTextField(20);
		private JTextField fio = new JTextField(20);
		private JCheckBox remote = new JCheckBox("Удаленный пользователь");
		private JCheckBox readOnly = new JCheckBox("Только чтение");	
		private JLabel lEmail = new JLabel("E-mail/Логин");
		private JLabel lFio = new JLabel("ФИО");
		public EditDialog(JFrame frame) {
			super(frame, true);
			JButton okButton = new JButton("OK");
			okButton.addActionListener(this);
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);
			JOptionPane optionPanel = new JOptionPane(createEditPanel(),JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION,null,new Object[]{okButton,cancelButton},cancelButton);
			setContentPane(optionPanel);
			setLocationRelativeTo(frame);
			pack();
		}
		public boolean startDialog(Employee employee) {
			setEmployee(employee);
			ready = false;
			setVisible(true);
			return ready;	
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand()=="Cancel") 
				setVisible(false);
			else if (e.getActionCommand()=="OK") {
				//валидация данных
				if (isEmpty(email))
					JOptionPane.showMessageDialog(null, "E-mail не может быть пустым");
				else if (isEmpty(fio))
					JOptionPane.showMessageDialog(null, "ФИО не может быть пустым");
				else {
					updateEmployee();
					ready = true;
					setVisible(false);
				}
			}
			
		}
		private JPanel createEditPanel() {
			JPanel panel = new JPanel(new GridLayout(0, 2));
			panel.add(lEmail);
			panel.add(email);		
			panel.add( lFio );
			panel.add(fio);	
			panel.add(remote);
			panel.add(readOnly);		
			return panel;		
		}	
		private void updateEmployee() {
			employeeEdit.setEmployeeEmail(email.getText());
			employeeEdit.setEmployeeName(fio.getText());
			employeeEdit.setReadOnly(readOnly.isSelected());
			employeeEdit.setRemote(remote.isSelected());
		}
		private void setEmployee(Employee emplyee) {
			employeeEdit = emplyee;
			email.setText(emplyee.getEmployeeEmail());
			fio.setText(emplyee.getEmployeeName());
			readOnly.setSelected(emplyee.isReadOnly());
			remote.setSelected(emplyee.isRemote());
		}
	}
	private class ChangePasswordDialog extends JDialog implements ActionListener {
		private JTextField email = new JTextField(20);
		private JTextField password = new JTextField(20);
		private JTextField passwordAgain = new JTextField(20);
		private JLabel lEmail = new JLabel("E-mail/Логин");
		private JLabel lPass = new JLabel("Пароль");
		private JLabel LPassAgain = new JLabel("Повтор пароля");
		public ChangePasswordDialog(JFrame frame) {
			super(frame, true);
			JButton okButton = new JButton("OK");
			okButton.addActionListener(this);
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);
			JOptionPane optionPanel = new JOptionPane(createPassPanel(),JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION,null,new Object[]{okButton,cancelButton},cancelButton);
			setContentPane(optionPanel);
			setLocationRelativeTo(frame);
			pack();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand()=="Cancel") 
				setVisible(false);
			else if (e.getActionCommand()=="OK") {
				//валидация данных
				if (isEmpty(password))
					JOptionPane.showMessageDialog(null, "Пароль не может быть пустым");
				else if (!password.getText().equals(passwordAgain.getText()))
					JOptionPane.showMessageDialog(null, "Пароли должны совпадать");
				else {
					updateEmployee();
					ready = true;
					setVisible(false);
				}
			}
			
		}
		private JPanel createPassPanel() {
			JPanel panel = new JPanel(new GridLayout(0, 2));
			panel.add(lEmail);
			panel.add(email);
			email.setEditable(false);
			panel.add( lPass );
			panel.add(password);
			panel.add( LPassAgain);
			panel.add(passwordAgain);	
			return panel;		
		}	
		private void updateEmployee() {
			employeeEdit.setEmployeeEmail(email.getText());
			employeeEdit.setEmployeePassword(password.getText());
			employeeEdit.setEmployeePasswordAgain(passwordAgain.getText());
		}
		private void setEmployee(Employee emplyee) {
			employeeEdit = emplyee;
			email.setText(emplyee.getEmployeeEmail());
		}
		public boolean startDialog(Employee employee) {
			setEmployee(employee);
			ready = false;
			setVisible(true);
			return ready;	
		}
	}
	private class AddDialog extends JDialog implements ActionListener {
		private JTextField email = new JTextField(20);
		private JTextField fio = new JTextField(20);
		private JTextField password = new JTextField(20);
		private JTextField passwordAgain = new JTextField(20);
		private JCheckBox remote = new JCheckBox("Удаленный пользователь");
		private JCheckBox readOnly = new JCheckBox("Только чтение");	
		private JLabel lEmail = new JLabel("E-mail/Логин");
		private JLabel lFio = new JLabel("ФИО");
		private JLabel lPass = new JLabel("Пароль");
		private JLabel LPassAgain = new JLabel("Повтор пароля");
		public AddDialog(JFrame frame) {
			super(frame, true);
			JButton okButton = new JButton("OK");
			okButton.addActionListener(this);
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);
			JOptionPane optionPanel = new JOptionPane(createAddPanel(),JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION,null,new Object[]{okButton,cancelButton},cancelButton);
			setContentPane(optionPanel);
			setLocationRelativeTo(frame);
			pack();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand()=="Cancel") 
				setVisible(false);
			else if (e.getActionCommand()=="OK") {
				//валидация данных
				if (isEmpty(email))
					JOptionPane.showMessageDialog(null, "E-mail не может быть пустым");
				else if (isEmpty(fio))
					JOptionPane.showMessageDialog(null, "ФИО не может быть пустым");
				else if (isEmpty(password))
					JOptionPane.showMessageDialog(null, "Пароль не может быть пустым");
				else if (!password.getText().equals(passwordAgain.getText()))
					JOptionPane.showMessageDialog(null, "Пароли должны совпадать");
				else {
					updateEmployee();
					ready = true;
					setVisible(false);
				}
			}
			
		}
		private JPanel createAddPanel() {
			JPanel panel = new JPanel(new GridLayout(0, 2));
			panel.add(lEmail);
			panel.add(email);		
			panel.add( lFio );
			panel.add(fio);		
			panel.add( lPass );
			panel.add(password);
			panel.add( LPassAgain);
			panel.add(passwordAgain);
			panel.add(remote);
			panel.add(readOnly);		
			return panel;		
		}	
		private void updateEmployee() {
			employeeEdit.setEmployeeEmail(email.getText());
			employeeEdit.setEmployeeName(fio.getText());
			employeeEdit.setEmployeePassword(password.getText());
			employeeEdit.setEmployeePasswordAgain(passwordAgain.getText());
			employeeEdit.setReadOnly(readOnly.isSelected());
			employeeEdit.setRemote(remote.isSelected());
		}
		private void setEmployee(Employee emplyee) {
			employeeEdit = emplyee;
			email.setText(emplyee.getEmployeeEmail());
			fio.setText(emplyee.getEmployeeName());
			password.setText(emplyee.getEmployeePassword());
			passwordAgain.setText(emplyee.getEmployeePasswordAgain());
			readOnly.setSelected(emplyee.isReadOnly());
			remote.setSelected(emplyee.isRemote());
		}
		public boolean startDialog(Employee employee) {
			setEmployee(employee);
			ready = false;
			setVisible(true);
			return ready;	
		}
	}
	
}
