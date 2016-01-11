package me.apemanzilla.jclminer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.apemanzilla.kristapi.types.KristAddress;

/**
 * Configuration manager for JCLMiner
 * @author apemanzilla
 *
 */
public class JCLMinerConfig {

	public JCLMinerConfig(String kristAddr, int rr) {
		this.address = KristAddress.auto(kristAddr);
		this.daemonRefreshRate = rr;
		this.deviceIds = new ArrayList<Integer>();
		this.selectBestDevice();
	}
	
	public JCLMinerConfig(String kristAddr) {
		this(kristAddr, 2000);
	}

	/**
	 * Address to mine Krist for
	 */
	private final KristAddress address;
	
	public KristAddress getAddress() {
		return address;
	}

	private Map<Integer, Integer> workSizes = new HashMap<Integer, Integer>();
	
	/**
	 * Should not be used when possible - should instead manually add items to the map using {@link #getWorkSizes() getWorkSizes}.
	 * @param workSizes
	 */
	@Deprecated
	public void setWorkSizes(Map<Integer, Integer> workSizes) {
		this.workSizes = workSizes;
	}

	public Map<Integer, Integer> getWorkSizes() {
		return workSizes;
	}

	/**
	 * Time between requests for lastblock and getwork in milliseconds
	 */
	private final int daemonRefreshRate;
	
	public int getDaemonRefreshRate() {
		return daemonRefreshRate;
	}
	
	private final List<Integer> deviceIds;
	
	/**
	 * Selects only the best device
	 */
	public void selectBestDevice() {
		deviceIds.clear();
		int best = -1;
		int id = -1;
		Iterator<Integer> it = JCLMiner.deviceIds.keySet().iterator();
		while (it.hasNext()) {
			int i = it.next();
			if (JCLMiner.deviceIds.get(i).getMaxComputeUnits() > best) {
				id = i;
			}
		}
		deviceIds.add(id);
	}
	
	/**
	 * Selects all available devices
	 */
	public void selectAllDevices() {
		deviceIds.clear();
		deviceIds.addAll(JCLMiner.deviceIds.keySet());
	}
}
