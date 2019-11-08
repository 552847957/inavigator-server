package ru.sberbank.syncserver2.service.generator.single.data;

import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 22.05.2012
 * Time: 10:33:26
 * To change this template use File | Settings | File Templates.
 */
public class XMLEscapeHelper {
    public static final char substitute = ' ';
    private static final HashSet<Character> illegalChars;

    static {
        final String escapeString = "\u0000\u0001\u0002\u0003\u0004\u0005" +
            "\u0006\u0007\u0008\u000B\u000C\u000E\u000F\u0010\u0011\u0012" +
            "\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C" +
            "\u001D\u001E\u001F\uFFFD\uFFFE\uFFFF";

        illegalChars = new HashSet<Character>();
        for (int i = 0; i < escapeString.length(); i++) {
            illegalChars.add(escapeString.charAt(i));
        }
    }

    public static boolean isIllegal(char c) {
        return illegalChars.contains(c);
    }

 /**
     * Substitutes all illegal characters in the given string by the value of
     * {@link XMLEscapeHelper#substitute}. If no illegal characters
     * were found, no copy is made and the given string is returned.
     *
     * @param string
     * @return
     */
    public static String escapeCharacters(String string) {

        char[] copy = null;
        boolean copied = false;
        for (int i = 0; i < string.length(); i++) {
            if (isIllegal(string.charAt(i))) {
//                System.out.println("ILLEGAL: "+string.charAt(i));
                if (!copied) {
                    copy = string.toCharArray();
                    copied = true;
                }
                copy[i] = substitute;
//            } else {
//                System.out.println("LEGAL: "+string.charAt(i)+"  "+(int)string.charAt(i));
            }
        }
        return copied ? new String(copy) : string;
    }
}
