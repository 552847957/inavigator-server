package ru.sberbank.syncserver2.gui.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestHelper {

	public static String toDigest(String s) throws NoSuchAlgorithmException {

		MessageDigest d = MessageDigest.getInstance("MD5");

		d.update(s.getBytes());

		byte[] b = d.digest();

		StringBuilder r = new StringBuilder(2 * b.length);

		int h, l;
		for (int i = 0, c = b.length; i < c; ++i) {
			h = (b[i] >>> 4) & 0x0F;
			l = b[i] & 0x0F;

			r.append((char) (h < 10 ? h + '0' : h - 10 + 'A'));
			r.append((char) (l < 10 ? l + '0' : l - 10 + 'A'));
		}

		return r.toString();
	}

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String hash = toDigest("123456");
        System.out.println(hash);
    }
}
