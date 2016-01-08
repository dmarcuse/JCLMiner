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
}