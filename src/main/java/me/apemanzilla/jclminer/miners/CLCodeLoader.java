package me.apemanzilla.jclminer.miners;

import java.io.IOException;
import java.io.InputStream;

import me.apemanzilla.jclminer.JCLMiner;

/**
 * Used to load packaged CL code. Use the {@link #loadCode(String) loadCode} method.
 * @author apemanzilla
 *
 */
public class CLCodeLoader {
	
	private CLCodeLoader() {}

	/**
	 * Loads CL code from the specified path. If any problem occurs, {@code null} will be returned.
	 * @param path
	 * @return
	 */
	public static String loadCode(String path) {
		try {
			InputStream is = JCLMiner.class.getResourceAsStream(path);
			if (is == null) return null;
			if (is.available() == 0) return null;
			byte[] data = new byte[is.available()];
			is.read(data, 0, is.available());
			return new String(data);
		} catch (IOException e) {
			return null;
		}
	}
}
