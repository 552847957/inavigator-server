package com.sberbank.vmo.syncserv.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 13.01.12
 * Time: 10:44
 * To change this template use File | Settings | File Templates.
 */
@XmlType(name="report-status",propOrder = {"templateMD5","dataMD5","publishStatus","templateFile","dataFile","lastModified","type","chunkCount"})
public class RDReportStatus {
	
    private String type;
    private String dataFile;
    private String templateFile;
    private String publishStatus;
    private String dataMD5;
    private String templateMD5;
    private String chunkCount;
    private String lastModified;

    @XmlAttribute(name = "id")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlAttribute(name = "name")
    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    @XmlAttribute(name = "template")
    public String getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    @XmlAttribute(name = "publishStatus")
    public String getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
    }

    @XmlAttribute(name = "dataMD5")
    public String getDataMD5() {
        return dataMD5;
    }

    public void setDataMD5(String dataMD5) {
        this.dataMD5 = dataMD5;
    }

    @XmlAttribute(name = "templateMD5")
    public String getTemplateMD5() {
        return templateMD5;
    }

    public void setTemplateMD5(String templateMD5) {
        this.templateMD5 = templateMD5;
    }

    @XmlAttribute(name = "lastModified")
    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    @XmlAttribute(name = "chunk-count")
    public String getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(String chunkCount) {
        this.chunkCount = chunkCount;
    }

    @Override
    public String toString() {
        return "RDReportStatus{" +
                "type='" + type + '\'' +
                ", dataFile='" + dataFile + '\'' +
                ", templateFile='" + templateFile + '\'' +
                ", publishStatus='" + publishStatus + '\'' +
                ", dataMD5='" + dataMD5 + '\'' +
                ", templateMD5='" + templateMD5 + '\'' +
                ", chunkCount='" + chunkCount + '\'' +
                '}';
    }
}
