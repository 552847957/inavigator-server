package ru.sberbank.syncserver2.service.generator.rubricator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.xml.bind.JAXBException;

import java.io.*;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Leonid Kozhinskiy
 *
 */
public class ChangeSetComposer {
    private File tempFolder;
    private File tempFile;
    private FileOutputStream fos = null;
    private ZipOutputStream  zos = null;
    private FileInfoList statusList = new FileInfoList();

    private static final String META_INF_LIST_XML = "META-INF/list.xml";
    private static byte[] buffer = new byte[1024*1024*4];

    private static Logger logger = Logger.getLogger(ChangeSetComposer.class);

    public ChangeSetComposer(File tempFolder) {
        this.tempFolder = tempFolder;
    }

    public void start() throws IOException {
        String date = DateFormatUtils.format(Calendar.getInstance(), "yyyyMMdd-HHmmss");
        tempFile = new File(tempFolder, "changeset" + date + "."+ AbstractService.LOCAL_HOST_NAME + ".mbr.zip");
        log("Start composing "+tempFile.getName());
        try {
            statusList.getReportStatuses().clear();
            fos = new FileOutputStream(tempFile);
            zos = new ZipOutputStream(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean containsSameCaption(FileInfo fileInfo){
        return statusList.containsSameCaption(fileInfo);
    }

    public void addContent(FileInfo fileInfo, File file) throws IOException{
        //1. Adding changes to status
        log("Start adding "+fileInfo.getId()+" to "+tempFile.getName());
        statusList.getReportStatuses().add(fileInfo);

        //2. Creating new archived file
        FileInputStream fis = null;
        ZipEntry zipEntry = new ZipEntry(fileInfo.getId());
        try {
            zos.putNextEntry(zipEntry);
            fis = new FileInputStream(file);
            synchronized (buffer){
                IOUtils.copyLarge(fis, zos, buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            zos.closeEntry();
            zos.flush();
            IOUtils.closeQuietly(fis);
            log("Finish adding "+fileInfo.getId()+" to "+tempFile.getName());
        }
    }

    public void addFileInfo(FileInfo fileInfo){
        statusList.getReportStatuses().add(fileInfo);
    }


    public void finish() throws IOException,JAXBException{
        //1. Writing final status file
        try {
            zos.putNextEntry(new ZipEntry(META_INF_LIST_XML));
            XMLHelper.writeXML(zos, statusList, true, FileInfoList.class, FileInfo.class);
            zos.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (JAXBException e) {
            e.printStackTrace();
            throw e;
        } finally {
            log("Finish composing "+tempFile.getName()+" with success");
            closeQuietly();
        }
    }

    public void finishOnCancel(String comment) {
        //1. Closing files
        //System.out.println("finishOnCancel - 1");
        if(zos!=null){
            try {
                zos.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("finishOnCancel - 2");
        closeQuietly();
        //System.out.println("finishOnCancel - 3");

        //2. Dropping composed file
        if(tempFile!=null){
            log("Finish composing "+tempFile.getName()+" with "+comment);
            FileCopyHelper.reliableDelete(tempFile);
        }
        //System.out.println("finishOnCancel - 4");
    }

    public File getTempFile() {
        return tempFile;
    }

    public FileInfoList getStatusList() {
        return statusList;
    }

    public void closeQuietly() {
        IOUtils.closeQuietly(zos);
        IOUtils.closeQuietly(fos);

    }

    public boolean containsSameIdForSameTime(FileInfo fileInfo) {
        return statusList.containsSameIdForSameTime(fileInfo);
    }

    public void log(String text){
        logger.info(text);
        //System.out.println(new Date()+" "+text);
    }
}
