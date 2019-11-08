package ru.sberbank.syncserver2.util;

import java.io.File;

public class AddMD5 {

    public static final String TEMP_FILE_POSTFIX = "TEMP.FILE.POSTFIX";

    public static void main(String[] args) throws Exception {
        System.out.println("usage:");
        System.out.println("java -jar AddMD5.jar dir add/remove");
        System.out.println("    dir         -   directory");
        System.out.println("    add/remove  -   add if add MD5, remove if remove MD5 to/from files in directory dir");
        System.out.println(" ");
        if (args.length == 2) {
            File inputDir = new File(args[0]);
            if (inputDir.isDirectory()) {
                File[] files = inputDir.listFiles();
                int cnt = 0;
                for (File fl : files) {
                    System.out.print("Process file: " + fl);
                    File tempFileInProcess = new File(inputDir, fl.getName() + "." + TEMP_FILE_POSTFIX);
                    String md5 = null;
                    if ("add".equalsIgnoreCase(args[1])) {
                        md5 = FileCopyHelper.copyAndAddMD5(fl, tempFileInProcess);
                    }
                    if ("remove".equalsIgnoreCase(args[1])) {
                        md5 = FileCopyHelper.copyAndRemoveMD5(fl, tempFileInProcess);
                    }
                    if (md5 != null) {
                        FileCopyHelper.reliableDelete(fl);
                        FileCopyHelper.localMove(tempFileInProcess, fl);
                        FileCopyHelper.reliableDelete(tempFileInProcess);
                    } else {
                        System.out.println(String.format("md5 == null for %s!", fl));
                        System.exit(1);
                    }
                    cnt++;
                    System.out.println(" : Ok !");
                }
                System.out.println("Count process files: " + cnt);
            } else {
                System.out.println(inputDir + " is not a dir!");
                System.exit(1);
            }
        } else {
            System.out.println("wrong args!");
            System.exit(1);
        }
        System.exit(0);
    }

}
