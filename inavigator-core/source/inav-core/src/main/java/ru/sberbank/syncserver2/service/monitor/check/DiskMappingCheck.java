package ru.sberbank.syncserver2.service.monitor.check;

import org.apache.commons.io.FileUtils;

import ru.sberbank.syncserver2.util.MD5Helper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 26.04.14.
 */
public class DiskMappingCheck extends AbstractCheckAction {
    private String filename;
    private String md5;

    public DiskMappingCheck() {
        super();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public List<CheckResult> doCheck() {
        //1. Check if file exists
        File file = new File(filename);
        boolean exists = file.exists();
        if(!exists){
            String txt = "File transporter is not mapped to "+ LOCAL_HOST_NAME +" any more or file "+filename+" is missing for user "+System.getProperty("user.name");
            return Arrays.asList(new CheckResult(false, txt ));
        }

        //2. Check the file has a valid contents
        byte[] contents = null;
        try {
            contents = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
            String txt = "File transporter is not mapped to "+ LOCAL_HOST_NAME +" any more or file "+filename+" was corrupted";
            return Arrays.asList(new CheckResult(false, txt));
        }
        String md5 = null;
        try {
            md5 = MD5Helper.getCheckSumAsString(contents);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(md5!=null && md5.equalsIgnoreCase(this.md5)){
            return Arrays.asList(new CheckResult(true, "File transporter is mapped at "+ LOCAL_HOST_NAME));
        } else {
            return Arrays.asList(new CheckResult(false, "File "+filename+" used for check of file transporter is corrupted"));
        }
    }
    
    @Override
    public String getDescription() {
    	return "Какой-то старый чекер проверки md5 файла";
    }

    public static void main(String[] args) {
        DiskMappingCheck check = new DiskMappingCheck();
        check.filename = "Z:/IN/uat/monitor/etalon.txt";
        check.md5 = "5f74224849a33f53226b6f6e022dbb04";
        ICheckResult result = check.check().get(0);
        System.out.println(result);
    }
    /*
    public static void main(String[] args) {
        String filename = "Z:/IN/uat/monitor/mapping.check".toLowerCase();
        File file = new File(filename);
        CheckResult result = null;
        if(!file.exists()){
            String txt = "File transporter is not mapped to "+ LOCAL_HOST_NAME+" any more or file "+filename+" is missing";
            result = new CheckResult(false, txt );
            System.out.println(result);
            System.exit(0);
        }

        //2. Check the file has a valid contents
        byte[] contents = null;
        try {
            contents = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
            String txt = "File transporter is not mapped to "+ LOCAL_HOST_NAME+" any more or file "+filename+" was corrupted";
            result = new CheckResult(false, txt);
            System.out.println(result);
            System.exit(0);
        }
        String md5 = null;
        try {
            md5 = MD5Helper.getCheckSumAsString(contents);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(md5!=null && md5.equalsIgnoreCase("5f74224849a33f53226b6f6e022dbb04")){
            result = new CheckResult(true, "File transporter is OK at "+LOCAL_HOST_NAME);
        } else {
            result = new CheckResult(false, "File "+filename+" used for check of file transporter is corrupted");
        }
        System.out.println(result);
        System.exit(0);
    } */
}
