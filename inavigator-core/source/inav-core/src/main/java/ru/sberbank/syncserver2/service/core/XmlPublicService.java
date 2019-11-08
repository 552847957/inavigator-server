package ru.sberbank.syncserver2.service.core;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ru.sberbank.syncserver2.gui.db.DatabaseManager;

public abstract class XmlPublicService extends AbstractService implements PublicService {
	
	private DatabaseManager database;  
	
	protected abstract Object xmlRequest(HttpServletRequest request, HttpServletResponse response,Object xmlInput);
	protected abstract Class[] getSupportedXmlClasses();

	@Override
	public void request(HttpServletRequest request, HttpServletResponse response) {
		
		if (database == null)
			database = new DatabaseManager(this.getServiceContainer().getServiceManager().getConfigSource());

		try {
			// преобразование входных XML данных в объект
			InputStream is = request.getInputStream();
			JAXBContext jaxbInContext = JAXBContext.newInstance(getSupportedXmlClasses());//"ru.sberbank.syncserver2.service.pub.xml");
			Unmarshaller jaxbUnmarshaller = jaxbInContext.createUnmarshaller();
			Object xmlRequest = jaxbUnmarshaller.unmarshal(is); 
			
			Object xmlResponse = null;
			// Вызов метода обработки запроса
			if (xmlRequest != null)
				xmlResponse = xmlRequest(request, response, xmlRequest);
			
			if (xmlResponse != null) {
			// формирование ответа
				JAXBContext jaxbOutContext = JAXBContext.newInstance(getSupportedXmlClasses());// "ru.sberbank.syncserver2.service.pub.xml");
				Marshaller jaxbMarshaller = jaxbOutContext.createMarshaller();
				jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				jaxbMarshaller.marshal(xmlResponse, response.getOutputStream());
				//jaxbMarshaller.marshal(xmlResponse, System.out);
			} else {
				response.getOutputStream().write("XML method not found or not defined.".getBytes());
			}
		
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				response.getOutputStream().write(("Error was ocuried while executing method.(" + ex.getMessage() + ")").getBytes());
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		}
	}

	@Override
	protected void doStop() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void waitUntilStopped() {
		// TODO Auto-generated method stub

	}



	public DatabaseManager getDatabase() {
		return database;
	}



	public void setDatabase(DatabaseManager database) {
		this.database = database;
	}
	
	

}
