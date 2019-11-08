package ru.sberbank.syncserver2.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by sbt-kozhinsky-lb on 21.02.14.
 */
public class FileCopyHelper {
    private static Logger logger = Logger.getLogger(FileCopyHelper.class);

    public static String reliableMove(File src, File dst) {
        boolean deleteDst = true;
        boolean deleteSrc = true;
        boolean checkMD5  = true;
        return reliableCopy(src, dst, deleteDst, deleteSrc,checkMD5);
    }

    public static String unreliableMove(File src, File dst) {
        boolean deleteDst = false;
        boolean deleteSrc = true;
        boolean checkMD5  = true;
        return reliableCopy(src, dst, deleteDst, deleteSrc,checkMD5);
    }

    public static String reliableCopy(File src, File dst) throws InterruptedException{
        boolean deleteDst = true;
        boolean deleteSrc = false;
        boolean checkMD5  = true;
        return  reliableCopy(src, dst, deleteDst, deleteSrc,checkMD5);
    }

    public static void unreliableCopy(File src, File dst) throws InterruptedException{
        boolean deleteDst = true;
        boolean deleteSrc = false;
        boolean checkMD5  = false;
        reliableCopy(src, dst, deleteDst, deleteSrc,checkMD5);
    }

    public static void simpleMove(File src, File dst) {
        boolean deleteDst = true;
        boolean deleteSrc = true;
        boolean checkMD5  = false;
        reliableCopy(src, dst, deleteDst, deleteSrc,checkMD5);
    }

    public static void simpleCopyWithOverride(File src, File dst) {
        boolean deleteDst = true;
        boolean deleteSrc = false;
        boolean checkMD5  = false;
        reliableCopy(src, dst, deleteDst, deleteSrc,checkMD5);
    }

    private static String reliableCopy(File src, File dst, boolean deleteDst, boolean deleteSrc, boolean checkMD5) {
        try {
            for (int i = 0; i<10; i++) {
                //1. Ignore command if no source file exists
                if (!src.exists()) {
                    log("ReliableMove hasn't found " + src);
                    return null;
                }

                //2. Delete target file if target file exists
                if (dst.exists()) {
                    if(deleteDst){
                        log("ReliableMove has deleted " + dst);
                        reliableDelete(dst);
                    } else {
                        return null;
                    }
                }

                //3. Create target directory if required
                File parentDst = dst.getParentFile();
                loggableMkdirs(parentDst);

                //4. Copying file
                try {
                    FileUtils.copyFile(src, dst);
                    log("ReliableMove copied from " + src + " to " + dst);
                    log("ReliableMove: dst (" + dst.getAbsolutePath() + ") size=" + dst.length() + " bytes");
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                //5. Recheck copy quality with MD5 comparison
                if(checkMD5){
                    String srcMD5 = null, dstMD5 = null;
                    try {
                        srcMD5 = MD5Helper.getCheckSumAsString(src.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        dstMD5 = MD5Helper.getCheckSumAsString(dst.getAbsolutePath());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    log("ReliableMove: src MD5 =" + srcMD5);
                    log("ReliableMove: dst MD5 =" + dstMD5);
                    if (ObjectUtils.equals(srcMD5, dstMD5)) {
                        if(deleteSrc){
                            reliableDelete(src);
                        }
                        return srcMD5;
                    }
                } else {
                    if(deleteSrc){
                        reliableDelete(src);
                    }
                    return null;
                }
                
                try {
                	Thread.sleep(2*1000);
                } catch (InterruptedException e) {
                	return null;
                }
            } 
            
            log("Can't copy from " + src + " to " + dst);
            return null;
        } finally {
            log("ReliableMove finished to copy from " + src + " to " + dst);
        }
    }

    public static void reliableDelete(File file) {
        file.delete();
        while(file!=null && file.exists()){
            System.out.println("Start deleting "+file);
            sleepTenSeconds();
            try {
                file.delete();
            } catch (Exception e) {
                System.out.println("Failed deleting "+file);
                e.printStackTrace();
            }
        }
    }

    public static void reliableDeleteFolderContent(File folder) {
        //1. Check folder existens
        if(!folder.exists()){
            return;
        }

        //2. Check if there are any files
        File[] files = folder.listFiles();
        if(files==null || files.length==0){
            return;
        }

        //3. Deleting
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileCopyHelper.reliableDelete(file);
        }
    }

    public static void reliableDeleteFolderAndSubFolders(File folder) {
        //1. Check folder existens
        if(!folder.exists()){
            return;
        }

        //2. Check if there are any files
        File[] files = folder.listFiles();
        if(files==null || files.length==0){
            return;
        }

        //3. Deleting
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file==null){
                continue;
            } else if(files[i].isDirectory()){
                reliableDeleteFolderAndSubFolders(files[i]);
                reliableDelete(file);
            } else {
                reliableDelete(file);
            }
        }

        //4. Dropping folder itself
        folder.delete();
        reliableDelete(folder);
    }

    private static void sleepTenSeconds() {
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
    }

    private static void log(String txt){
        logger.trace(txt);
    }

    public static void moveIfDestinationDoesNotExist(File src, File dst){
        if(!dst.exists()){
            src.renameTo(dst);
        } else {
            sleepTenSeconds();
        }
    }

    public static void createReadLock(File networkFile) {
        try {
            new File(networkFile+".READLOCK").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void localMove(File src, File dst) {
        //1. Ignore command if no source file exists
        if (!src.exists()) {
            log("simpleMoveWithoutCopy hasn't found " + src);
            return;
        }

        //2. Delete target file if target file exists
        if (dst.exists()) {
            reliableDelete(dst);
        }

        //3. Moving
        src.renameTo(dst);
    }

    public static void loggableMkdirs(File file){
        boolean success = file.exists() || file.mkdirs();
        if(!success){
            try {
                throw new IOException("Error while creating folder " + file.getAbsolutePath() + " . Please check permissions granted to " + System.getProperty("user.name"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String copyAndAddMD5(File src, File dst) throws Exception{
        //1. Calculating md5
        //System.out.println("STARTING copyAndAddMD5 ");
        byte[] md5 = null;
        try {
            md5=MD5Helper.getCheckSumAsBytes(src.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        //2. Writing md5 and copy file content
        FileOutputStream out = null;
        FileInputStream in = null;
        try {
            //2.1. Creating parent for destination if necessary
            File parent = dst.getParentFile();
            loggableMkdirs(parent);

            //2. Creating output and writing md5
            out = new FileOutputStream(dst);
            DataOutputStream outData = new DataOutputStream(out);
            //System.out.println("copyAndAddMD5.len =  "+md5.length);
            outData.writeInt(md5.length);
            outData.write(md5);
            outData.flush();

            //3. Writing main file content
            byte[] buffer = new byte[1024*128];
            in = new FileInputStream(src);
            IOUtils.copyLarge(in,out,buffer);
            out.flush();
            out.close();
            return MD5Helper.toHexString(md5);
        } catch (Exception e) {
            e.printStackTrace();
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
            reliableDelete(dst);
            throw e;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    public static String copyAndRemoveMD5(File src, File dst) throws Exception{
        //1. Writing md5 and copy file content
        //System.out.println("STARTING copyAndRemoveMD5 ");
        FileOutputStream out = null;
        FileInputStream in = null;
        try {
            //1. Reading md5
            in = new FileInputStream(src);
            DataInputStream inData =  new DataInputStream(in);
            int len = inData.readInt();
            if(len>100){
                System.out.println("copyAndRemoveMD5 reports : file "+src.getAbsolutePath()+" is corrupred because lenght of md5 is too big");
                len = (int) Math.min((long) 39, src.length()); //allow to read invalid data and it means that file will be reported as corrupted
            }
            byte[] headerMD5 = new byte[len];
            inData.read(headerMD5);

            //2. Writing dst file
            //2.1. Creating folder if necessary
            File parent = dst.getParentFile();
            loggableMkdirs(parent);

            //2.2. Writing dst file
            byte[] buffer = new byte[1024*128];
            out = new FileOutputStream(dst);
            IOUtils.copyLarge(in,out,buffer);
            out.flush();
            out.close();

            //2.3. Compare md5
            byte[] contentMD5 = null;
            try {
                contentMD5 = MD5Helper.getCheckSumAsBytes(dst.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //2.4. Return MD5 on success and null on fail
            if(headerMD5!=null && contentMD5!=null && ArrayUtils.isEquals(headerMD5,contentMD5)){
                return MD5Helper.toHexString(headerMD5);
            } else {
                reliableDelete(dst);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
            reliableDelete(dst);
            throw e;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    public static void reliableRename(File src, File dst) {
        do {
            reliableDelete(dst);
            if(src.renameTo(dst) || !src.exists()){ //!src.exists finish endless loop if necessary
                return;
            }
            sleepTenSeconds();
        } while (true);
    }

    public static void main(String[] args) throws Exception {
        FileCopyHelper.reliableDeleteFolderAndSubFolders(new File("C:\\usr\\cache\\syncleo.bak"));
        /*
        System.out.println("START TESTING");
        File src = new File("C:\\tmp\\RENTGEN.jpg");
        File dst = new File("C:\\tmp\\RENTGEN.jpg.md5");
        File src2 = new File("C:\\tmp\\RENTGEN.jpg.2");
        String originalMD5 = MD5Helper.getCheckSumAsString(src.getAbsolutePath());
        System.out.println("Original MD5="+originalMD5);
        copyAndAddMD5(src,dst);
        copyAndRemoveMD5(dst,src2);
        String newMD5 = MD5Helper.getCheckSumAsString(src2.getAbsolutePath());
        System.out.println("After copy MD5="+newMD5);
        */
    }

}
