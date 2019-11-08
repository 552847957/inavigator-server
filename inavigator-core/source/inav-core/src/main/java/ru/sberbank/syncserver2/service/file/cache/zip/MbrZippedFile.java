/**
 *
 */
package ru.sberbank.syncserver2.service.file.cache.zip;

import org.apache.commons.io.IOUtils;
import org.omg.IOP.IORHelper;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Yuliya Solomina
 *
 */
public class MbrZippedFile {

	/**
	 *
	 */
	private static final String META_INF_LIST_XML = "META-INF/list.xml";
	private static final String REMOVED_FLAG = "file_removed";
	private File file;
	private ZipFile zip;

	/**
	 * @param file
	 * @throws IOException
	 */
	public MbrZippedFile(File file) throws IOException {
		this.file = file;
		this.zip = new ZipFile(file);
	}

	public FileInfoList getFileInfoList() throws JAXBException, IOException {
        FileInfoList result = null;
        InputStream is = null;
        try {
            is = getEntryStream(META_INF_LIST_XML);
            result = (FileInfoList) XMLHelper.readXML(is, FileInfoList.class, FileInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        return result;

	}

    public boolean extractTo(String entryName, File file) {
        //1. Declaring
        InputStream is = null;
        FileOutputStream os = null;

        //2. Inflating
        try {
            is = getEntryStream(entryName);
            os = new FileOutputStream(file);
            IOUtils.copy(is, os);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
        return false;
    }

	/**
	 * @param entryName
	 * @return
	 * @throws IOException
	 */
	public InputStream getEntryStream(String entryName) throws IOException {
		ZipEntry entry = zip.getEntry(entryName);
		if (entry != null) {
			return zip.getInputStream(entry);
		}

		throw new IOException("Cannot find zip entry " + entryName);
	}

    public void close() throws IOException {
        if(zip!=null){
            zip.close();
        }
    }

}
