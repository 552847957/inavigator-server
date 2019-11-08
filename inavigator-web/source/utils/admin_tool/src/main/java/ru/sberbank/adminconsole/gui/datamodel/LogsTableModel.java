package ru.sberbank.adminconsole.gui.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import ru.sberbank.adminconsole.services.IRemoteDataReceiver;
import ru.sberbank.syncserver2.service.log.LogAction;
import ru.sberbank.syncserver2.service.pub.xml.LogMessages;
import ru.sberbank.syncserver2.service.pub.xml.Message;

public class LogsTableModel extends AbstractTableModel implements IRemoteDataReceiver<LogMessages>{
	private List<LogAction> data = new ArrayList<LogAction>();
	private List<String> sources = new ArrayList<String>();
	private int maxPage = 0;
	private JLabel mPage;	
	private JTextField curPage;
	
	@Override
	public String getColumnName(int col) {
		switch (col) {
			case 0: return "Дата и время";
			case 1: return "Источник";
			case 2: return "Сообщение";
			case 3: return "Сервер";
			default: return "";
		}	
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		LogAction log;
		String server;
		try {
			log = data.get(rowIndex);
			server = sources.get(rowIndex);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			log = new LogAction();
			server = "";
		}
		switch (columnIndex) {
			case 0: return log.getDate();
			case 1: return log.getService();
			case 2: return log.getMsg();
			case 3: return server;
			default: return "";
		}			
	}
	
	public void clear() {
		sources.clear();
		data.clear();
		maxPage = 0;
		fireTableDataChanged();
	}
	
	public void add(Collection<LogAction> c, String server) {
		data.addAll(c);
		for (int i=0;i<c.size();i++) {
			sources.add(server);
		}
		fireTableDataChanged();
	}
	
	public void setMaxPageLabel(JLabel maxPage) {
		mPage = maxPage;
	}
	
	public void setCurPage(JTextField field) {
		curPage = field;
	}

	@Override
	public void submit(LogMessages xml) {
		if (xml.getCode()!=Message.Status.OK)
			return;
		if (Integer.valueOf(curPage.getText()).equals(xml.getPage())) {
			add(xml.getLogs(),xml.getSource());
			maxPage = Math.max(maxPage, xml.getMaxPage());
			mPage.setText(Integer.toString(maxPage));			
		}
	}
	
}	