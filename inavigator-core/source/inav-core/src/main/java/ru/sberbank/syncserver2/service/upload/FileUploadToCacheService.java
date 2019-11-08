package ru.sberbank.syncserver2.service.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.file.cache.SingleFileLoader;

/**
 * Сервис по загрузке файлов в файлкеш. ( сервис используется по URL /upload.do)
 * @author sbt-gordienko-mv
 *
 */
public class FileUploadToCacheService extends AbstractService implements PublicService {

	public String  quottesFileLoader =  "quottesFileLoader";
	
	/**
	 * Если FileItem является файлом, то сохраняем его во временной входящей папке для последующей загрузки в кеш сервер 
	 * @param inboxFile
	 * @param item
	 */
	public void storeFileToInputFolder(File inboxFile,FileItem item) {
		// если элемент не является файлом, то ничего не делаем
    	if (item.getContentType() == null) return;
    	FileOutputStream fos = null;
    	InputStream is = null;
    	try {
        	File f = new File(inboxFile,item.getName());
        	fos = new FileOutputStream(f);
        	int nextByte = -1;
        	is = item.getInputStream();
        	while((nextByte = is.read()) != -1) {
        		fos.write(nextByte);
        	}
    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
        	try {
				fos.close();
			} catch (IOException e) {}
        	try {
        		is.close();
			} catch (IOException e) {}
    	}		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void request(HttpServletRequest request, HttpServletResponse response) {
	SingleFileLoader singleFileLoader = (SingleFileLoader)(ServiceManager.getInstance().findServiceByBeanCode(quottesFileLoader)).getService();
		File inboxFile = new File(singleFileLoader.getInboxFolder());
		
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items;
        try {
        	// читаем список файлов пришедших к нам
			items = upload.parseRequest(request);
	        for(FileItem item : items) {
	        	storeFileToInputFolder(inboxFile, item);
	        }
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doStop() {
	}

	@Override
	protected void waitUntilStopped() {
	}
	
	public String getQuottesFileLoader() {
		return quottesFileLoader;
	}

	public void setQuottesFileLoader(String quottesFileLoader) {
		this.quottesFileLoader = quottesFileLoader;
	}
	
}
