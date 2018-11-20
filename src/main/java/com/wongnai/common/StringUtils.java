package com.wongnai.common;

/**
 *
 * @author Suparit Krityakien
 */
public final class StringUtils {
	private StringUtils() {
	}

	/**
	 * Checks if given cs is blank/null or not.
	 *
	 * @param cs
	 *            character string
	 * @return {@code true} if the cs is blank or null
	 */
	public static boolean isBlank(String cs) {
		int strLen;
		if (cs != null && (strLen = cs.length()) != 0) {
			for (int i = 0; i < strLen; ++i) {
				if (!Character.isWhitespace(cs.charAt(i))) {
					return false;
				}
			}

			return true;
		} else {
			return true;
		}
	}
}
