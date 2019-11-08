package ru.sberbank.adminconsole.gui.modules;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ru.sberbank.adminconsole.gui.actions.EmployeeAction;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.adminconsole.model.configuration.Module;
import ru.sberbank.syncserver2.gui.data.Employee;

public class EmployeeModule extends AbstractSwingModule {
	private JPanel modulePanel = new JPanel(new BorderLayout());
	private JList list;
	private String labelText = "Список администраторов:";

	public EmployeeModule(Module moduleInfo) {
		super(moduleInfo);
	}

	@Override
	public void init() { 
		DefaultListModel listModel = new DefaultListModel();
        list = new JList(listModel) {
            //Subclass JList to workaround bug 4832765, which can cause the
            //scroll pane to not let the user easily scroll up to the beginning
            //of the list.  An alternative would be to set the unitIncrement
            //of the JScrollBar to a fixed value. You wouldn't get the nice
            //aligned scrolling, but it should work.
            public int getScrollableUnitIncrement(Rectangle visibleRect,
                                                  int orientation,
                                                  int direction) {
                int row;
                if (orientation == SwingConstants.VERTICAL &&
                      direction < 0 && (row = getFirstVisibleIndex()) != -1) {
                    Rectangle r = getCellBounds(row, row);
                    if ((r.y == visibleRect.y) && (row != 0))  {
                        Point loc = r.getLocation();
                        loc.y--;
                        int prevIndex = locationToIndex(loc);
                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);

                        if (prevR == null || prevR.y >= r.y) {
                            return 0;
                        }
                        return prevR.height;
                    }
                }
                return super.getScrollableUnitIncrement(
                                visibleRect, orientation, direction);
            }
        };

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        
        list.setCellRenderer(new EmployeeCellRender());
        list.setVisibleRowCount(-1);
        EmployeeAction action = new EmployeeAction(list, listModel,(JFrame)SwingUtilities.getRoot(modulePanel));
        final JButton getButton = new JButton("Обновить");
        getButton.setActionCommand("GET");
        getButton.addActionListener(action);
        //
        final JButton addButton = new JButton("Добавить");
        addButton.setActionCommand("ADD");
        addButton.addActionListener(action);
        
        final JButton editButton = new JButton("Изменить");
        editButton.setActionCommand("EDIT");
        editButton.addActionListener(action);
        
        final JButton changePassButton = new JButton("Сменить пароль");
        changePassButton.setActionCommand("CHANGE_PASSWORD");
        changePassButton.addActionListener(action);
        
        final JButton deleteButton = new JButton("Удалить");
        deleteButton.setActionCommand("DELETE");
        deleteButton.addActionListener(action);
        
        final JButton syncButton = new JButton("Синхронизовать");
        syncButton.setActionCommand("SYNC");
        syncButton.addActionListener(action);
        
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editButton.doClick(); //emulate button click
                }
            }
        });
        JScrollPane listScroller = new JScrollPane(list);
        //listScroller.setPreferredSize(new Dimension(150, 150));
        listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(labelText);
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,10)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(getButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(addButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(editButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(changePassButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(deleteButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(syncButton);
        buttonPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        
        modulePanel.add(listPane, BorderLayout.CENTER);
        modulePanel.add(buttonPane, BorderLayout.PAGE_END);
        
	}

	@Override
	public JPanel getModulePanel() {
		return modulePanel;
	}

	private class EmployeeCellRender extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			Employee empl = (Employee) value;
			label.setText(empl.getEmployeeEmail()+" | "+empl.getEmployeeName());
			return label;
		}
		
	}

	@Override
	public void userHasChangedAppSelection() {
		// NToDo		
	}
}
