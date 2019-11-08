package ru.sberbank.adminconsole.gui.modules;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import ru.sberbank.adminconsole.gui.actions.SettingsAction;
import ru.sberbank.adminconsole.gui.datamodel.SettingsModel;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.adminconsole.model.configuration.Module;

public class SettingsModule extends AbstractSwingModule {
	
	private JPanel modulePanel = null;
	private SettingsAction action;

	public SettingsModule(Module moduleInfo) {
		super(moduleInfo);
	}

	@Override
	public void init() {
		modulePanel = new JPanel(new BorderLayout());
		
		ScrollablePanel settingsPanel = new ScrollablePanel(new GridBagLayout());
		JScrollPane scroll = new JScrollPane(settingsPanel);//,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		modulePanel.add(scroll,BorderLayout.CENTER);		
		
		SettingsModel settingsModel = new SettingsModel(settingsPanel);
		SettingsAction action = new SettingsAction(settingsModel);
		this.action = action;
		
		JButton buttonGet = new JButton(action);
		buttonGet.setText("получить настройки");
		buttonGet.setActionCommand("GET");
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.add(buttonGet);
		modulePanel.add(topButtonPanel,BorderLayout.PAGE_START);
		
		JButton buttonSet = new JButton(action);
		buttonSet.setText("сохранить");	
		buttonSet.setActionCommand("SET");
		JPanel botButtonPanel = new JPanel();
		botButtonPanel.add(buttonSet);
		modulePanel.add(botButtonPanel,BorderLayout.PAGE_END);
		
	}

	@Override
	public JPanel getModulePanel() {
		return modulePanel;
	}
		
	public static class ScrollablePanel extends JPanel implements Scrollable {
		
	    public ScrollablePanel() {
			super();
			// TODO Auto-generated constructor stub
		}

		public ScrollablePanel(boolean isDoubleBuffered) {
			super(isDoubleBuffered);
			// TODO Auto-generated constructor stub
		}

		public ScrollablePanel(LayoutManager layout, boolean isDoubleBuffered) {
			super(layout, isDoubleBuffered);
			// TODO Auto-generated constructor stub
		}

		public ScrollablePanel(LayoutManager layout) {
			super(layout);
			// TODO Auto-generated constructor stub
		}

		public Dimension getPreferredScrollableViewportSize() {
	        return getPreferredSize();
	    }

	    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
	       return 10;
	    }

	    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
	        return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
	    }

	    public boolean getScrollableTracksViewportWidth() {
	        return true;
	    }

	    public boolean getScrollableTracksViewportHeight() {
	        return false;
	    }
	}

	@Override
	public void userHasChangedAppSelection() {
		action.actionPerformed(new ActionEvent(this, 0, "GET"));		
	}

}
