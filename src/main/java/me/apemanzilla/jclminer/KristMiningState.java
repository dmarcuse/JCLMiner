package me.apemanzilla.jclminer;

import java.util.Observable;

import me.apemanzilla.kristapi.KristAPI;
import me.apemanzilla.kristapi.exceptions.SyncnodeDownException;

/**
 * Constantly keeps track of the state of Krist for mining
 * e.g. last block and work value
 * @author apemanzilla
 *
 */
public class KristMiningState extends Observable implements Runnable {

	private String lastBlock;
	private long work;
	
	private boolean blockChanged;
	private boolean workChanged;
	
	private final Object lock = new Object();
	
	/**
	 * Refresh rate in milliseconds.
	 */
	private int refreshRate;
	
	public KristMiningState(int refreshRate) {
		this.refreshRate = refreshRate;
	}
	
	public boolean didBlockChange() {
		synchronized (lock) {
			if (blockChanged) {
				blockChanged = false;
				return true;
			}
			return false;
		}
	}
	
	public boolean didWorkChange() {
		synchronized (lock) {
			if (workChanged) {
				workChanged = false;
				return true;
			}
			return false;
		}
	}
	
	public String getBlock() {
		synchronized (lock) {
			return lastBlock;
		}
	}
	
	public long getWork() {
		synchronized (lock) {
			return work;
		}
	}
	
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				String block = KristAPI.getBlock();
				if (lastBlock != block) {
					long work = KristAPI.getWork();
					if (this.work != work) {
						synchronized (lock) {
							workChanged = true;
							this.work = work;
						}
					}
					synchronized (lock) {
						blockChanged = true;
						lastBlock = block;
					}
					setChanged();
					notifyObservers();
				}
				Thread.sleep(refreshRate);
			} catch (InterruptedException e) {
				break;
			} catch (SyncnodeDownException e) {
				// TODO 
			}
		}
	}

}
