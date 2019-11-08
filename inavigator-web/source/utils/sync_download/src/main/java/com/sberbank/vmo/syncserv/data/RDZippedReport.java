package com.sberbank.vmo.syncserv.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 13.02.2012
 * Time: 17:35:54
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name="zipped-report")
public class RDZippedReport {
    private String reportType;
    private String md5;
    private String reportContent;
    private String chunkIndex;
    private byte[] binaryContent;

    public RDZippedReport() {
    }

    public RDZippedReport(String reportType, String reportContent, String md5, String chunkIndex) {
        this.reportType = reportType;
        this.reportContent = reportContent;
        this.md5 = md5;
        this.chunkIndex = chunkIndex;
    }

    public RDZippedReport(byte[] binaryContent, String chunkIndex) {
    	this.chunkIndex = chunkIndex;
    	this.binaryContent = binaryContent;
    }

    @XmlElement(name="report-type")
    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    @XmlElement(name="md5")
    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @XmlElement(name="report-content")
    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    @XmlElement(name="chunk-index")
    public String getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(String chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

	public byte[] getBinaryContent() {
		return binaryContent;
	}

	public void setBinaryContent(byte[] binaryContent) {
		this.binaryContent = binaryContent;
	}
    
    
}
