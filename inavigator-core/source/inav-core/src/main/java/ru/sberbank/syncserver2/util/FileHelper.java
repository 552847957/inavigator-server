package ru.sberbank.syncserver2.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 29.11.11
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public class FileHelper {
    public static byte[] readBinary(File file){
        return readBinary(file.getAbsolutePath());
    }

    public static byte[] readBinary(String inputFileName){
        DataInputStream in = null;
        try {
            int len = (int) new File(inputFileName).length();
            byte[] result = new byte[len];
            in = new DataInputStream(new FileInputStream(inputFileName));
            in.read(result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return null;
    }

    public static void writeBinary(byte[] data, String outputFileName){
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream(outputFileName));
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static void writeString(String content, String outputFileName){
        FileWriter out = null;
        try {
            out = new FileWriter(outputFileName);
            out.write(content);;
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static void writeObject(Serializable object, File file) {
        writeObject(object, file.getAbsolutePath());
    }

    public static void writeObject(Serializable object, String outputFileName) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFileName);
            ObjectOutputStream ser = new ObjectOutputStream(new BufferedOutputStream(out, 32767));
            ser.writeObject(object);
            ser.flush();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static Object readObject(File file) {
        return readObject(file.getAbsolutePath());
    }

    public static Object readObject(String inputFileName) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(inputFileName);
            ObjectInputStream ser = new ObjectInputStream(new BufferedInputStream(in, 32767));
            return ser.readObject();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return null;
    }

    public static void createMissingFolders(String ... folders) {
        for (int i = 0; i < folders.length; i++) {
            String folder = folders[i];
            if(folder!=null){
                try {
                    new File(folder).mkdirs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean deflate(File src, File dst) {
        //1. Check if source file exists and destination file does not existis
        if(!src.exists() || dst.exists()) {
            return false;
        }

        //2. Reading and compress
        BufferedInputStream  is = null;
        DeflaterOutputStream os = null;
        byte[] readBuffer = new byte[1024*1024];
        int readBufferLength = 0;
        try {
            is = new BufferedInputStream(new FileInputStream(src), readBuffer.length);
            os = new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(dst)));
            do {
                readBufferLength = IOUtils.read(is, readBuffer);
                os.write(readBuffer, 0, readBufferLength);
            } while (readBufferLength == readBuffer.length);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            close(is);
            close(os);
        }
        return true;
    }

    public static boolean inflate(File src, File dst) {
        //1. Check if source file exists and destination file does not existis
        if(!src.exists() || dst.exists()) {
            return false;
        }
    	long srcTime = src.lastModified();

        //2. Reading and compress
        InflaterInputStream is = null;
        FileOutputStream    os = null;
        byte[] readBuffer = new byte[1024*1024];
        int readBufferLength = 0;
        try {
            is = new InflaterInputStream(new BufferedInputStream(new FileInputStream(src), readBuffer.length));
            os = new FileOutputStream(dst);
            do {
                //2.1. Reading
                readBufferLength = IOUtils.read(is, readBuffer);
                os.write(readBuffer, 0, readBufferLength);
            } while (readBufferLength == readBuffer.length);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            close(is);
            close(os);
        }
        dst.setLastModified(srcTime);
        return true;
    }

    public static void close(Closeable is){
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            //ignore
        }
    }

    public static boolean areFilesExists(long lifeMillis, File ... files){
        //1.Check that all files exists
        for( int i=0;i<files.length; i++){
            if(!files[i].exists()){
                return false;
            }
            if(files[i].lastModified()>System.currentTimeMillis()-lifeMillis){
                return false;
            }
        }
        return true;
    }

    /**
     * Функция для сравнения маленьких файлов. Оригинально написана для сравления файлов блокировок размером 39 байт.
     * На больших файлах может вызвать OutOfMemory
     * @param files
     * @return
     */
    public static boolean areFilesSame(File ... files){
        //1.Check that all files exists
        for( int i=0;i<files.length; i++){
            if(!files[i].exists()){
                return false;
            }
        }

        //2. Check that all files have same length
        long len=files[0].length();
        for( int i=1;i<files.length; i++){
             if(files[i].length()!=len){
                return false;
             }
        }

        //3. Check that all files have same content
        byte firstContent[] = FileHelper.readBinary(files[0]);
        for( int i=1;i<files.length; i++){
            byte nextContent[] = FileHelper.readBinary(files[i]);
            if(!ArrayUtils.isEquals(firstContent,nextContent)){
                return false;
            }
        }
        return true;
    }
    

    public static byte[] zip(byte[] unzippedData){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            deflater.setInput(unzippedData, 0, unzippedData.length);
            byte[] tempBuffer = new byte[unzippedData.length];
            deflater.finish();
            while (!deflater.finished()) {
                int count = deflater.deflate(tempBuffer);
                bos.write(tempBuffer, 0, count);
            }
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    public static byte[] unzip(byte[] zippedData) {
        //1. Reading and compress
        InflaterInputStream is = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream(zippedData.length*2);
        byte[] readBuffer = new byte[1024*1024];
        int readBufferLength = 0;
        try {
            is = new InflaterInputStream(new ByteArrayInputStream(zippedData));
            do {
                //2.1. Reading
                readBufferLength = IOUtils.read(is, readBuffer);
                os.write(readBuffer, 0, readBufferLength);
            } while (readBufferLength == readBuffer.length);
            os.flush();
            return os.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            close(is);
            close(os);
        }
    }
    
    public static void main(String[] args) throws Exception {
        String file1 = "D:\\usr\\projects\\Sberbank\\syncserver2\\cache-build.bat";
        String file2 = "D:\\usr\\projects\\Sberbank\\cache-build.bat";
        System.out.println("SAME = "+areFilesSame(new File(file1), new File(file2)));
        System.out.println("WAIT = "+areFilesExists(120000l,new File(file1), new File(file2)));
        /*
        File src = new File("C:\\Локальные документы\\projects\\i-Navigator\\code\\syncserver.rar");
        File dst = new File("C:\\Локальные документы\\projects\\i-Navigator\\code\\syncserver.rar.zipped");
        File dst2 = new File("C:\\Локальные документы\\projects\\i-Navigator\\code\\syncserver.rar.unzipped");
        dst.delete();
        dst2.delete();
        System.out.println("DEFLATE RESULT= "+deflate(src, dst));
        System.out.println("INFLATE RESULT= "+inflate(dst,dst2));
        System.out.println(MD5Helper.getCheckSumAsString(src.getAbsolutePath()));
        System.out.println(MD5Helper.getCheckSumAsString(dst2.getAbsolutePath()));
        */
    }
}
