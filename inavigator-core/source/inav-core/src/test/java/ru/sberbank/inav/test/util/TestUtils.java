package ru.sberbank.inav.test.util;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

public class TestUtils {

    public static void createDirectory(String outPathName) throws IOException {
        File f;
        String cr = ".";
        StringTokenizer st = new StringTokenizer(outPathName, "/");
        while (st.hasMoreTokens()) {
            cr = cr + "/" + st.nextToken();
            f = new File(cr);
            if (!f.exists()) {
                Assert.assertTrue(f.mkdir());
            }
        }
    }

    public static void createDirectory(String ... outPathName) throws IOException {
        File f;
        String cr = ".";
        for (String st : outPathName){
            cr = cr + "/" + st;
            f = new File(cr);
            if (!f.exists()) {
                Assert.assertTrue(f.mkdir());
            }

        }
    }

}
