package me.apemanzilla.jclminer;

import java.text.DecimalFormat;

import com.sci.skristminer.util.Utils;

public final class MinerUtils {

	private static final DecimalFormat format = new DecimalFormat("0.00");

	private MinerUtils() {
	}

	/**
	 * @author apemanzilla
	 */
	public static char[] getChars(final byte[] b) {
		final char[] chars = new char[b.length];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) b[i];
		}
		return chars;
	}

	/**
	 * @author sci4me
	 */
	public static byte[] getBytes(final String s) {
		final byte[] bytes = new byte[s.length()];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) s.charAt(i);
		return bytes;
	}

	/**
	 * @author sci4me
	 */
	public static long hashToLong(final byte[] hash) {
		long ret = 0;
		for (int i = 5; i >= 0; i--)
			ret += (hash[i] & 0xFF) * Math.pow(256, 5 - i);
		return ret;
	}

	/**
	 * @author sci4me
	 */
	public static String formatSpeed(final long rawSpeed) {
		String result;

		if (rawSpeed > 1000000000) {
			final double speed = (double) rawSpeed / 1000000000;
			result = format.format(speed) + " GH/s";
		} else if (rawSpeed > 1000000) {
			final double speed = (double) rawSpeed / 1000000;
			result = format.format(speed) + " MH/s";
		} else if (rawSpeed > 1000) {
			final double speed = (double) rawSpeed / 1000;
			result = format.format(speed) + " KH/s";
		} else {
			result = rawSpeed + " H/s";
		}

		return result;
	}
}
