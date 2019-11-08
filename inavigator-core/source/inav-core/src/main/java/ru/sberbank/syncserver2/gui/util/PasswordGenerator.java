package ru.sberbank.syncserver2.gui.util;

import java.security.SecureRandom;

public class PasswordGenerator {

	private final static char[] CONSONANTS = new char[] { 'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p',
		'r', 's', 't', 'v', 'w', 'x', 'y', 'z' };

	private final static char[] VOWELS = new char[] { 'a', 'e', 'i', 'o', 'u' };

	private final static char[] DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	public static String generatePassword() {
		SecureRandom random = new SecureRandom();
		int i, letterQuantity = 5 + random.nextInt(2), digitQuantity = 1 + random.nextInt(2), shift = random.nextInt(2);
		char[] result = new char[letterQuantity + digitQuantity];

		if (shift % 2 == 0) {
			result[0] = Character.toUpperCase(CONSONANTS[random.nextInt(CONSONANTS.length)]);
		} else {
			result[0] = Character.toUpperCase(VOWELS[random.nextInt(VOWELS.length)]);
		}

		for (i = 1; i < letterQuantity; ++i) {
			if ((i + shift) % 2 == 0) {
				result[i] = CONSONANTS[random.nextInt(CONSONANTS.length)];
			} else {
				result[i] = VOWELS[random.nextInt(VOWELS.length)];
			}
		}

		for (i = 0; i < digitQuantity; ++i) {
			result[i + letterQuantity] = DIGITS[random.nextInt(DIGITS.length)];
		}

		//return new String(result);
        return "pass";
	}

}
