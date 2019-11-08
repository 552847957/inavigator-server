package ru.sberbank.adminconsole.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.sberbank.adminconsole.model.configuration.ApplicationConfiguration;
import ru.sberbank.adminconsole.model.configuration.ModuleConfiguration;
import ru.sberbank.adminconsole.services.IConfigurationLoader;

public class ConfigurationLoaderImpl implements IConfigurationLoader{
	
	private static Logger logger = LoggerFactory.getLogger(ConfigurationLoaderImpl.class);
	
	public Object readXml(InputStream is, Class clazz) throws Exception {
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			return unmarshaller.unmarshal(is);
	}
	
	@Override
	public ApplicationConfiguration readApplicationConfiguration() {
		FileInputStream fis = null;
		try {
    		fis = new FileInputStream("./configuration.xml");
    		return (ApplicationConfiguration)readXml(fis,ApplicationConfiguration.class);
    	} catch (Exception ex) {
    		logger.error("Application(Servers) configuration loaded failed!",ex);
			return null;
    	} finally {
    		try {
    			fis.close();
    		} catch (Exception ex) {}
    				
    	}
	}

	@Override
	public ModuleConfiguration readModuleConfiguration() {
		InputStream is = null;
		try {
    		is = this.getClass().getResourceAsStream("/modules.xml");
    		return (ModuleConfiguration)readXml(is,ModuleConfiguration.class);
    	} catch (Exception ex) {
    		logger.error("Module configuration loaded failed!",ex);
			return null;
    	} finally {
    		try {
    			is.close();
    		} catch (Exception ex) {}
    				
    	}
	}

}

