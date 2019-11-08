package ru.sberbank.adminconsole.gui.services;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ru.sberbank.adminconsole.services.IRemoteDataReceiver;
import ru.sberbank.syncserver2.service.pub.xml.Authentication;

public class AuthorizationService {
	
	private static LoginPanel loginPanel = new LoginPanel();
	private static LoginDialog loginDialog;
	
	private static class LoginPanel extends JPanel {
		private JTextField login = new JTextField();
		private JTextField pass = new JTextField();
		private JLabel message = new JLabel("Введите логин и пароль",SwingConstants.CENTER);
		public LoginPanel() {
			super(new GridLayout(0,1));
			add(new JLabel("логин",SwingConstants.CENTER));
			add(login);
			add(new JLabel("пароль",SwingConstants.CENTER));
			add(pass);	
			add(message);
		}
		public String getLogin() {
			return login.getText();	
		}
		public String getPassword() {
			return pass.getText();	
		}
		public void lock() {
			login.setEnabled(false);
			pass.setEnabled(false);			
		}
		public void unlock() {
			login.setEnabled(true);
			pass.setEnabled(true);
		}
	}
	private static class AuthorizationAction extends AbstractAction {
		private AccessChecker checker = new AccessChecker();
		@Override
		public void actionPerformed(ActionEvent e) {
			AuthorizationService.lock();
			loginPanel.message.setText("авторизация...");
			Authentication auth = new Authentication(loginPanel.getLogin(), loginPanel.getPassword());
			RemoteAuthorizer.authenticate(checker, auth);					
		}		
	}
	private static class LoginDialog extends JDialog {
		private JButton authButton = new JButton(new AuthorizationAction());
		{
			authButton.setText("Вход");
		}
		private JOptionPane optionPanel = new JOptionPane(loginPanel,JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION,null,new Object[]{authButton},null);
		public LoginDialog(JFrame frame) {
			super(frame, true);	
			setContentPane(optionPanel);
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	        addWindowListener(new WindowAdapter() {
	                public void windowClosing(WindowEvent we) {	
	                	System.exit(0);
	            }
	        });
	        getRootPane().setDefaultButton(authButton);
	        pack();
	        setLocation(600, 400);        
		}
		public void lock() {
			authButton.setEnabled(false);
		}
		public void unlock() {
			authButton.setEnabled(true);
		}
	}
	
	public static class AccessChecker implements IRemoteDataReceiver<Boolean> {
		private String msg="";
		public void setMsg(String msg) {
			this.msg = msg;
		}
		@Override
		public void submit(Boolean access) {
			unlock();
			if (access) {
				loginDialog.setVisible(false);
			} else {
				loginPanel.message.setText(msg);
			}			
		}		
	}
	
	public static void showAuthorizationWindow(JFrame frame) {
		loginDialog = new LoginDialog(frame);
		loginDialog.setVisible(true);
	}
	
	private static void lock() {
		loginPanel.lock();
		loginDialog.lock();
	}
	private static void unlock() {
		loginPanel.unlock();
		loginDialog.unlock();
	}

}
