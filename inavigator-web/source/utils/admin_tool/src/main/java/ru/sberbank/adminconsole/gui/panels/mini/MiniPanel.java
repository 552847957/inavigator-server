package ru.sberbank.adminconsole.gui.panels.mini;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ru.sberbank.adminconsole.gui.listeners.LinkServiceAndLogsMouseListener;


public class MiniPanel extends JPanel {
	protected JLabel jName;
	protected final String name;
	protected Action action;

	public MiniPanel(String name) {
		super(new FlowLayout(FlowLayout.LEADING));
		this.name = name;
		jName = new JLabel(name);
		jName.addMouseListener(new LinkServiceAndLogsMouseListener());
		add(jName);
		setSize(new Dimension(200, 300));
		setBackground(Color.WHITE);
	}
	
	public void addButtons(Action a) {
		action = a;
		JButton buttonStart = new JButton(a);
		buttonStart.setText("start");
		buttonStart.setActionCommand("START");
		JButton buttonStop = new JButton(a);
		buttonStop.setText("stop");
		buttonStop.setActionCommand("STOP");		
		add(buttonStart);
		add(buttonStop);
	}
	
	public String getName() {
		return name;
	}
	
	public void click(ActionEvent e) {
		action.actionPerformed(e);		
	}

}
