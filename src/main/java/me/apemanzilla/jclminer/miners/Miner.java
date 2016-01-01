package me.apemanzilla.jclminer.miners;

import java.util.Observable;

/**
 * Represents a Krist miner
 * @author apemanzilla
 *
 */
public abstract class Miner extends Observable {
	
	/**
	 * Starts the miner.
	 * @param work The work value - hash must be less than this for a nonce to be valid.
	 * @param block The latest block mined
	 * @param prefix The value to append to the beginning of the hash
	 * @param suffix The value to append to the end of the hash
	 */
	public abstract void start(long work, String block, String prefix, String suffix);
	
	/**
	 * Starts the miner with no prefix or suffix.
	 * @param work The work value - hash must be less than this for a nonce to be valid.
	 * @param block The latest block mined
	 */
	public void start(long work, String block) {
		start(work, block, "", "");
	}
	
	/**
	 * Stops the miner.
	 */
	public abstract void stop();
	
	/**
	 * When applicable, releases all resources held by the miner. Should ALWAYS be called when miners are removed for safety.
	 */
	public void release() {
		
	}
	
	/**
	 * Retrieves the solution from the miner, or returns {@code null} if there is no solution yet.
	 * @return The solution nonce, or {@code null}
	 */
	public abstract String getSolution();
	
	/**
	 * Gets the average hash rate of the miner, in hashes per second, since the miner was started.
	 * @return The miner's average hash rate
	 */
	public abstract long getAverageHashRate();
	
	/**
	 * Gets the recent hash rate of the miner
	 * @return The hashes computer in the last second
	 */
	public abstract long getRecentHashRate();
	
	/**
	 * Gets the name of the device the miner is running on.
	 * @return The name of the device
	 */
	public abstract String getDeviceName();
	
	/**
	 * Returns the total number of compute units or cores available to the miner.
	 * @return The number of compute units or cores
	 */
	public abstract int getTotalComputeUnits();
}