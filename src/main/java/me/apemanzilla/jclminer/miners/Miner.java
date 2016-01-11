package me.apemanzilla.jclminer.miners;

import java.util.Observable;

/**
 * Represents a Krist miner
 * @author apemanzilla
 *
 */
public abstract class Miner extends Observable {

	protected String prefix;
	
	protected long hashes = 0;
	protected long startTime = 0;
	protected long recentHashes = 0;
	protected long recentTime = 0;
	
	/**
	 * Starts the miner.
	 * @param work The work value - hash must be less than this for a nonce to be valid.
	 * @param block The latest block mined
	 */
	public abstract void start(long work, String block);
	
	/**
	 * Stops the miner.
	 */
	public abstract void stop();
	
	/**
	 * Checks whether the specified miner has a solution. This should not be used repeatedly, the miner should notify observers when it has a solution instead.
	 * @return Whether the miner has a solution.
	 */
	public abstract boolean hasSolution();
	
	/**
	 * Retrieves the solution from the miner, or returns {@code null} if there is no solution yet.
	 * @return The solution nonce, or {@code null}
	 */
	public abstract String getSolution();
	
	/**
	 * Gets the average hash rate of the miner, in hashes per second, since the miner was started.
	 * @return The miner's average hash rate
	 */
	public long getAverageHashRate() {
		if (hashes == 0 || startTime == 0) {
			return 0;
		}
		double seconds = (System.currentTimeMillis() - startTime) / 1000;
		return (long) (hashes / seconds);
	}
	
	/**
	 * Gets the average hashrate since this function was last called.
	 */
	public final long getRecentHashRate() {
		if (hashes == 0 || startTime == 0) {
			return 0;
		} else {
			long h = hashes - recentHashes;
			double t = (System.currentTimeMillis() - recentTime) / 1000;
			recentHashes = hashes;
			recentTime = System.currentTimeMillis();
			long out = (long) (h/t);
			return out < 0 ? 0 : out;
		}
	}
	
	/**
	 * Gets the name of the device the miner is running on.
	 * @return The name of the device
	 */
	public abstract String getDeviceName();
	
	/**
	 * Checks whether the given Miner is using OpenCL.
	 * @return
	 */
	public boolean isOpenCLMiner() {
		return false;
	}
	
	/**
	 * Sets the work size for the given Miner. Only to be used on OpenCL miners.
	 * @param range The work size
	 */
	public void setWorkSize(int size) {}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getPrefix() {
		return prefix;
	}
}