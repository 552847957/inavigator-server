package ru.sberbank.syncserver2.service.file.cache.data;


import org.apache.commons.lang3.StringUtils;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 28.02.14.
 */
@XmlRootElement(name = "report-status-list",namespace = "")
public class FileInfoList implements Cloneable{
    private boolean archiver = true;
    private String  version = "1.0";

    private List<FileInfo> statuses = new ArrayList<FileInfo>();

    public FileInfoList() {
    }

    public FileInfoList(List<FileInfo> statuses) {
        this.statuses = statuses;
    }

    @Override
    public Object clone() {
        List<FileInfo> newStatuses = new ArrayList<FileInfo>();
        for (int i = 0; i < statuses.size(); i++) {
            FileInfo fileInfo =  statuses.get(i);
            FileInfo copy = (FileInfo) fileInfo.clone();
            newStatuses.add(copy);
        }
        return  new FileInfoList(newStatuses);
    }

    @XmlAttribute(name = "archiver")
    public boolean isArchiver() {
        return archiver;
    }

    public void setArchiver(boolean archiver) {
        this.archiver = archiver;
    }


    @XmlAttribute(name = "version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    @XmlElement(name = "report-status")
    public List<FileInfo> getReportStatuses() {
        return statuses;
    }

    public void setStatuses(List<FileInfo> statuses) {
        this.statuses = statuses;
    }

    public void replaceAppForAll(String app) {
        for (int i = 0; i < statuses.size(); i++) {
            FileInfo fileInfo =  statuses.get(i);
            fileInfo.setApp(app);
        }
    }

    @Override
    public String toString() {
        return "FileInfoList{" +
                "statuses=" + statuses +
                '}';
    }

    public static void main(String[] args) {
        FileInfo info = new FileInfo();
        info.setId("phonebook_db");
        info.setApp("default");
        info.setCaption(null);
        info.setName("phonebook.sqlite");
        info.setGroup(null);
        info.setDataMD5("0b664ffde24a275fea536471501c35a0");
        info.setLastModified(new Date().toString());
        info.setChunkCount("9");
        FileInfoList result = new FileInfoList(Collections.singletonList(info));
        String xml = XMLHelper.writeXMLToString(result, true, FileInfoList.class, FileInfo.class);
        System.out.println(xml);

    }

    public void update(FileInfoList changes) {
        List infos = changes.getReportStatuses();
        for (int i = 0; i < infos.size(); i++) {
            FileInfo change = (FileInfo) infos.get(i);
            if(change.isRemoved()){
                for (int j = 0; j < statuses.size(); j++) {
                    FileInfo existing = (FileInfo)infos.get(j);
                    if(existing.getId().equals(change.getId())){
                        statuses.remove(i);
                        break;
                    }
                }
            } else {
                boolean found = false;
                for (int j = 0; j < statuses.size(); j++) {
                    FileInfo existing = (FileInfo)statuses.get(j);
                    if(change.getId().equals(existing.getId())){
                        statuses.set(i, change);
                        found = true;
                        break;
                    }
                }
                if(!found){
                    statuses.add(change);
                }
            }
        }
    }

    public void saveTo(File file) {
        //1. Declaring
        String newFileName = file.getAbsolutePath()+".tmp";
        String bakFileName = file.getAbsolutePath()+".bak";
        File bakFile = new File(bakFileName);
        File newFile = new File(newFileName);

        //2. Writing to a tmp file, existing rename to bak and tmp rename to existing
        XMLHelper.writeXML(newFileName, this,true,FileInfoList.class, FileInfo.class);
        FileCopyHelper.reliableDelete(bakFile);
        FileCopyHelper.reliableMove(file, bakFile);//file is deleted only after MD5 matches
        FileCopyHelper.reliableMove(newFile, file);//newFile is deleted only after MD5 matches
        FileCopyHelper.reliableDelete(bakFile);
    }

    public static FileInfoList loadFrom(File file){
        //1. Restoring from backup if it exists
        String bakFileName = file.getAbsolutePath()+".bak";
        File bakFile = new File(bakFileName);
        if(bakFile.exists()){
            try {
                FileCopyHelper.reliableCopy(bakFile, file);
                FileCopyHelper.reliableDelete(bakFile);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //2. Reading
        if(file.exists()){
            return (FileInfoList)XMLHelper.readXML(file, FileInfoList.class, FileInfo.class);
        } else {
            FileInfoList newList = new FileInfoList(new ArrayList());
            return newList;
        }
    }

    public boolean containsSameIdForSameTime(FileInfo fileInfo) {
        for (int i = 0; i < statuses.size(); i++) {
            FileInfo info =  statuses.get(i);
            if(info.getId().equalsIgnoreCase(fileInfo.getId()) && info.getLastModified().equalsIgnoreCase(fileInfo.getLastModified())){
                return true;
            }
        }
        return false;
    }

    public boolean containsSameCaption(FileInfo fileInfo) {
        for (int i = 0; i < statuses.size(); i++) {
            FileInfo info =  statuses.get(i);
            String caption1 = info.getCaption();
            String caption2 = fileInfo.getCaption();
            if(StringUtils.equals(caption1,caption2)){
                return true;
            }
        }
        return false;
    }

}
