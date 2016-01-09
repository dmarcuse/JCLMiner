package me.apemanzilla.jclminer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.JavaCL;
import com.sci.skristminer.util.Utils;
import me.apemanzilla.kristapi.exceptions.SyncnodeDownException;
import me.apemanzilla.kristapi.types.KristAddress;
import me.apemanzilla.jclminer.miners.Miner;
import me.apemanzilla.jclminer.miners.MinerFactory;
import me.apemanzilla.jclminer.miners.MinerInitException;

public final class JCLMiner implements Runnable, Observer {

	public static final String[] cl_build_options = {};
	
	public static boolean isDeviceCompatible(CLDevice dev) {
		return dev.getType().contains(CLDevice.Type.GPU);
	}
	
	public static String generateID() {
		return String.format("%02x", new Random().nextInt(256));
	}
	
	public static List<CLDevice> listCompatibleDevices() {
		List<CLDevice> out = new ArrayList<CLDevice>();
		CLPlatform platforms[] = JavaCL.listPlatforms();
		for (CLPlatform plat : platforms) {
			CLDevice[] devices = plat.listAllDevices(false);
			for (CLDevice dev : devices) {
				if(isDeviceCompatible(dev)) {
					out.add(dev);
				}
			}
		}
		return out;
	}
	
	private static void log(String message) {
		System.out.println(message);
	}
	
	private List<CLDevice> devices;
	
	private final KristAddress host;
	private final List<Miner> miners;
	
	private final KristMiningState state;
	
	public JCLMiner(KristAddress host) {
		this.host = host;
		this.state = new KristMiningState(3000);
		miners = new ArrayList<Miner>();
	}
	
	/**
	 * Initialize CL stuff
	 */
	private void initMiners() {
		if (devices == null) {
			// get best device
			CLDevice best = JavaCL.getBestDevice();
			if (isDeviceCompatible(best)) {
				try {
					miners.add(MinerFactory.createMiner(best, this));
					System.out.format("Device %s ready.\n", best.getName().trim());
				} catch (MinerInitException e) {
					System.err.println(String.format("Failed to create miner for device %s\n", best.getName().trim()));
					e.printStackTrace();
				}
			}
		} else {
			// use specified devices
			for (CLDevice dev : devices) {
				if (isDeviceCompatible(dev)) {
					try {
						Miner m = MinerFactory.createMiner(dev, this);
						miners.add(m);
						System.out.format("Device %s ready.\n", dev.getName().trim());
					} catch (MinerInitException e) {
						System.err.format("Failed to create miner for device %s\n", dev.getName().trim());
						e.printStackTrace();
					}
				} else {
					System.out.format("Specified device %s is incompatible\n", dev.getName().trim());
				}
			}
		}
	}
	
	public void useDevices(List<CLDevice> devices) {
		this.devices = devices;
	}
	
	private boolean stop;
	
	// blocks solved
	private long blocks = 0;
	// time started
	private long started = 0;
	
	private void startMiners() {
		System.out.format("Mining for block: %s Work: %d\n", state.getBlock().trim(), state.getWork());
		for (Miner m : miners) {
			m.start(state.getWork(), state.getBlock());
		}
	}
	
	private long getAverageHashRate() {
		long hr = 0;
		for (Miner m : miners) {
			hr += m.getAverageHashRate();
		}
		return hr;
	}
	
	private void stopMiners() {
		for (Miner m : miners) {
			m.stop();
		}
	}
	
	private String findSolution() {
		for (Miner m : miners) {
			if (m.hasSolution()) {
				return m.getSolution();
			}
		}
		return null;
	}
	
	private long secondsSinceStarted() {
		return (System.currentTimeMillis() - started) / 1000;
	}
	
	private double blocksPerMinute() {
		double m = (double) secondsSinceStarted() / 60;
		if (m != 0 && blocks != 0) {
			return (double) blocks / m;
		}
		return 0;
	}
	
	@Override
	public void run() {
		log("Starting JCLMiner...");
		initMiners();
		state.addObserver(this);
		Thread stateDaemon = new Thread(state);
		stateDaemon.setDaemon(true);
		stateDaemon.start();
		for (Miner m : miners) {
			m.addObserver(this);
		}
		while(state.getBlock() == null || state.getWork() == 0) {}
		// main loop
		System.out.println("Starting miners...");
		started = System.currentTimeMillis();
		while (true) {
			startMiners();
			while (!stop) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {}
				if (!stop) System.out.format("%s | %d blocks solved | %.2f blocks per minute\n", Utils.formatSpeed(getAverageHashRate()), blocks, blocksPerMinute());
			}
			stopMiners();
			String sol = findSolution();
			if (sol != null) {
				try {
					System.out.format("Submitting solution '%s' > ", sol);
					String currBlock = state.getBlock();
					if (host.submitBlock(URLEncoder.encode(sol,"ISO-8859-1"))) {
						blocks++;
						System.out.println("Success!");
						// wait for block to change
						while (state.getBlock() == currBlock) {}
					} else {
						System.out.println("Solution rejected.");
					}
				} catch (SyncnodeDownException e) {
					System.err.format("Failed to submit solution '%s' - syncnode down\n",sol);
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					System.err.format("Failed to encode solution '%s'\n", sol);
					e.printStackTrace();
				}
			}
			stop = false;
		}
	}

	public KristAddress getHost() {
		return host;
	}

	@Override
	public void update(Observable o, Object arg) {
		stop = true;
	}
	
}
