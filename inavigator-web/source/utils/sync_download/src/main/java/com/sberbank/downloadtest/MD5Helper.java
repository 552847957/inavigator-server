package com.sberbank.downloadtest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * √–уппа утилитных методов дл€ работы с MD5
 * @author sbt-gordienko-mv
 *
 */
public class MD5Helper {

    public static String getCheckSumAsString(byte[] data) throws Exception {
        MessageDigest complete = MessageDigest.getInstance("MD5");
        complete.update(data);
        byte[] digest = complete.digest();
        return toHexString(digest);
    }

    public static byte[] getCheckSumAsBytes(InputStream is) throws Exception {
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        try {
        do {
            numRead = is.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        return complete.digest();
        } finally {
            is.close();
        }
    }

    public static byte[] getCheckSumAsBytes(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);
        byte[] result = getCheckSumAsBytes(fis);
        fis.close();
        return result;
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getCheckSumAsString(String filename) throws Exception {
        byte[] b = getCheckSumAsBytes(filename);
        return toHexString(b);
    }

    public static String toHexString(byte[] b){
        StringBuilder result = new StringBuilder(b.length);
        for (int i = 0; i < b.length; i++) {
            result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public static void main(String args[]) {
        try {
            System.out.println(getCheckSumAsString("C:/syncserver.war"));
            System.out.println(getCheckSumAsString("C:\\usr\\projects\\SyncServer\\dist\\syncserver.war"));
            // output :
            //  0bb2827c5eacf570b6064e24e0e6653b
            // ref :
            //  http://www.apache.org/dist/
            //          tomcat/tomcat-5/v5.5.17/bin
            //              /apache-tomcat-5.5.17.exe.MD5
            //  0bb2827c5eacf570b6064e24e0e6653b *apache-tomcat-5.5.17.exe
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}