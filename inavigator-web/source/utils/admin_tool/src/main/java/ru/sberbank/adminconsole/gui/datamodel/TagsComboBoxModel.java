package ru.sberbank.adminconsole.gui.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.services.IRemoteDataReceiver;
import ru.sberbank.syncserver2.service.pub.xml.LogsTags;
import ru.sberbank.syncserver2.service.pub.xml.Message;

public class TagsComboBoxModel extends AbstractListModel implements ComboBoxModel, IRemoteDataReceiver<LogsTags> {
	private List<String> objects;
	private Set<String> tags = new HashSet<String>();
	private String selectedObject;

	public TagsComboBoxModel() {
		objects = new ArrayList<String>();
	}

	@Override
	public int getSize() {
		return objects.size();
	}

	@Override
	public Object getElementAt(int index) {
		if ( index >= 0 && index < objects.size() )
            return objects.get(index);
        else
            return null;
	}

	@Override
	public void setSelectedItem(Object anObject) {
		if ((selectedObject != null && !selectedObject.equals( anObject )) ||
	            selectedObject == null && anObject != null) {
	            selectedObject = (String) anObject;
	            fireContentsChanged(this, -1, -1);
	        }		
	}

	@Override
	public Object getSelectedItem() {
		return selectedObject;
	}
	
	public void removeAllElements() {
		if (objects.size() > 0) {
			int firstIndex = 0;
			int lastIndex = objects.size() - 1;
			objects.clear();
			tags.clear();
			selectedObject = null;
			fireIntervalRemoved(this, firstIndex, lastIndex);
		} else {
			selectedObject = null;
		}
	}
	
	public void add(List<String> elementsToAdd) {
		int size = tags.size();
		tags.addAll(elementsToAdd);
		if (tags.size()>size) {
			List<String> newElements = new ArrayList<String>(tags);
			Collections.sort(newElements);			
			objects = newElements;
			fireContentsChanged(this, -1, -1);			
		}		
	}
	
	public List<String> getElements() {
		return objects;
	}

	@Override
	public void submit(LogsTags xml) {
		if (xml.getCode()!=Message.Status.OK)
			return;
		if (xml.getTags()!=null)
			add(xml.getTags());		
	}
	
}
