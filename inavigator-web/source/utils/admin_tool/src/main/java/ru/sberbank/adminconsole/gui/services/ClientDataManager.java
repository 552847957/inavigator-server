package ru.sberbank.adminconsole.gui.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.sberbank.adminconsole.gui.datamodel.LogsTableModel;
import ru.sberbank.adminconsole.gui.datamodel.ServiceTreeModel;
import ru.sberbank.adminconsole.gui.datamodel.TagsComboBoxModel;
import ru.sberbank.adminconsole.gui.modules.AbstractSwingModule;
import ru.sberbank.adminconsole.gui.panels.ServersTreePane;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.adminconsole.model.configuration.Module;

public class ClientDataManager {	
	private Map<String, Set<Application>> servers = new HashMap<String, Set<Application>>();
	private Map<String,AbstractSwingModule> systemModules = new LinkedHashMap<String,AbstractSwingModule>();
	private Map<Integer, Application> identifiers = new HashMap<Integer, Application>();
	private int appCount = 0;
	private Application mainApplication;

	private ServersTreePane treePanel;
	private TagsComboBoxModel tagsModel;
	private LogsTableModel table;
	private ServiceTreeModel serviceModel;
	private JTabbedPane tabbedPane;	

	private static Logger logger = LoggerFactory.getLogger(ClientDataManager.class);
	
	public ClientDataManager() {
		super();
	}	

	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}
	public Application getMainApplication() {
		return mainApplication;
	}


	public void setTabbedPane(JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
	}


	public ServiceTreeModel getServiceModel() {
		return serviceModel;
	}


	public void setServiceModel(ServiceTreeModel serviceModel) {
		this.serviceModel = serviceModel;
	}


	public LogsTableModel getTable() {
		return table;
	}

	public void setTable(LogsTableModel table) {
		this.table = table;
	}

	public void setTree(ServersTreePane tree) {
		this.treePanel = tree;
	}

	public void setTagsModel(TagsComboBoxModel tagsModel) {
		this.tagsModel = tagsModel;
	}	

	public TagsComboBoxModel getTagsModel() {
		return tagsModel;
	}

	public Set<String> getCategories() {
		return servers.keySet();
	}
	
	public Set<Application> getServers(String category) {
		return servers.get(category);
	}
	
	public void unselectAllApplications() {
		treePanel.unselectAll();
	}
	
	private static class ManagerHolder {
		private static final ClientDataManager INSTANCE = new ClientDataManager();
	}
	
	public static ClientDataManager getInstance() {
		return ManagerHolder.INSTANCE;
	}
	
	public  Collection<Application> getAllApplications() {
		return identifiers.values();
	}	
	
	public List<Application> getSelectedServers() {
		return treePanel.getSelectedServers();
	}
	
	public void setSelectedApplication(Application a) {
		treePanel.setSelectedApplication(a);
	}
	
	public int getApplicationsSelectionCount() {
		return treePanel.getSelectionCount();
	}
	
	public void updateServersStatus() {
		treePanel.updateIcons();
	}
	
	public Application getApplicationById(Integer id) {
		return identifiers.get(id);
	}	
	

	public void setServers(List<Application> list) {
		appCount = list.size();
		Set<String> categories = new HashSet<String>();
		for (Application server: list) {
			categories.add(server.getCategory());
		}
		for (String s: categories) {
			servers.put(s,new HashSet<Application>());
		}
		Integer id = 1;
		for (Application server: list) {
			if (server.isMain()) mainApplication = server;
			server.setId(id);
			servers.get(server.getCategory()).add(server);
			identifiers.put(id++, server);
		}		
	}
	
	public int getApplicationCount() {
		return appCount;
	}
	
	public Collection<AbstractSwingModule> getSystemModules() {
		return systemModules.values();
	}

	public AbstractSwingModule getModuleByCode(String code) {
		return systemModules.get(code);
	}
	
	private AbstractSwingModule createSystemModule(Module moduleInfo) {
		try {
			return (AbstractSwingModule)Class.forName(moduleInfo.getClassName()).getConstructor(Module.class).newInstance(moduleInfo);
		} catch (Exception ex) {
			logger.error("Cannot create module " + moduleInfo.getTitle(),ex);
			return null;
		}
	}
	
	public void setSystemModules(List<Module> systemModules) {
		for(Module moduleInfo:systemModules) {
			if (moduleInfo.getCode() == null || moduleInfo.getCode().equals(""))
				throw new RuntimeException("Error! System module code is empty!");
			this.systemModules.put(moduleInfo.getCode(),createSystemModule(moduleInfo));
		}
	}	

}
