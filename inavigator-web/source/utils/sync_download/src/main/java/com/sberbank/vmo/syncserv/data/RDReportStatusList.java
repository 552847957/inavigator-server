package com.sberbank.vmo.syncserv.data;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 13.01.12
 * Time: 10:50
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = "report-status-list",namespace = "")
public class RDReportStatusList {
    private boolean archiver;
    private String  version;
    private String  licenseStorage;
    private String  databaseConfigName;

    private ArrayList<RDReportStatus> statuses = new ArrayList<RDReportStatus>();

    public RDReportStatusList() {
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

    @XmlAttribute(name = "licenseStorage")
    public String getLicenseStorage() {
        return licenseStorage;
    }

    public void setLicenseStorage(String licenseStorage) {
        this.licenseStorage = licenseStorage;
    }


    @XmlAttribute(name = "databaseConfigName")
    public String getDatabaseConfigName() {
        return databaseConfigName;
    }

    public void setDatabaseConfigName(String databaseConfigName) {
        this.databaseConfigName = databaseConfigName;
    }



    @XmlElement(name = "report-status")
    public ArrayList<RDReportStatus> getReportStatuses() {
        return statuses;
    }

    public void setReportStatuses(ArrayList<RDReportStatus> reports) {
        this.statuses = reports;
    }

    @Override
    public String toString() {
        return "RDReportStatusList{" +
                "statuses=" + statuses +
                '}';
    }

    /*
    public  static void fillMD5(String inputFileName, String outputFileName, String reportFolder){
        //1. Reading
        RDReportStatusList statuses = (RDReportStatusList) XMLHelper.readXML(inputFileName, RDReportStatusList.class);

        //2. Filling MD5
        for (int i = 0; i < statuses.statuses.size(); i++) {
            RDReportStatus report = statuses.statuses.get(i);
            String filepath = reportFolder + report.getDataFile();
            String md5 = null;
            try {
                md5 = MD5Helper.getCheckSumAsString(filepath);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            report.setMd5(md5);
        }

        //3. Saving
        XMLHelper.writeXML(outputFileName, statuses);
    }



    public static void main(String[] args) {
        //1. Defining strings
        String statusFolder = "C:/usr/Tomcat 7.0/webapps/test/";
        String reportFolder = "C:/usr/Tomcat 7.0/webapps/test/statuses/";
        String devReportFolder = "C:/usr/Tomcat 7.0/webapps/test/statuses/";

        //2. Updating MD5 for usual statuses
        String listWithoutMD5 = statusFolder + "report_status_list_without_md5.xml";
        String listWithMD5 = statusFolder + "info_server.xml";
        fillMD5(listWithoutMD5, listWithMD5, reportFolder);

        //3. Updating MD5 for stress tests
        listWithoutMD5 = statusFolder + "report_status_list_without_md5_stress_test.xml";
        listWithMD5 = statusFolder + "info_server_stress_test.xml";
        fillMD5(listWithoutMD5, listWithMD5, reportFolder);

        //3. Updating MD5 for dev statuses
        listWithoutMD5 = statusFolder + "report_status_list_without_md5.xml";
        listWithMD5 = statusFolder + "info_server_dev.xml";
        fillMD5(listWithoutMD5, listWithMD5, devReportFolder);
    }

    public static void main(String[] args) {
        RDReportStatus status1 = new RDReportStatus();
        status1.setType("finance.balance");
        status1.setDataFile("data.xml");
        status1.setTemplateFile("template1.xml");
        ArrayList a = new ArrayList();
        a.add(status1);
        RDReportStatusList list = new RDReportStatusList();
        list.setReportStatuses(a);
        XMLHelper.writeXML("C:\\usr\\projects\\projects\\SyncServer\\files\\work\\admin\\list2.xml",list,true,RDReportStatusList.class,RDReportStatus.class);
    }
    */
}

