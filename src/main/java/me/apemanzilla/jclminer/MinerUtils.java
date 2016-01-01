package me.apemanzilla.jclminer;

public final class MinerUtils {
	
	private MinerUtils(){}
	
	/**
	 * @author sci4me
	 */
    public static byte[] getBytes(final String s)
    {
        final byte[] bytes = new byte[s.length()];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) s.charAt(i);
        return bytes;
    }
    
    /**
     * @author sci4me
     */
    public static long hashToLong(final byte[] hash)
    {
        long ret = 0;
        for (int i = 5; i >= 0; i--)
            ret += (hash[i] & 0xFF) * Math.pow(256, 5 - i);
        return ret;
    }
}
