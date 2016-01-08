package me.apemanzilla.jclminer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.JavaCL;
import com.sci.skristminer.util.Utils;

import me.apemanzilla.kristapi.KristAPI;
import me.apemanzilla.kristapi.exceptions.SyncnodeDownException;
import me.apemanzilla.kristapi.types.KristAddress;
import me.apemanzilla.jclminer.miners.Miner;
import me.apemanzilla.jclminer.miners.MinerFactory;
import me.apemanzilla.jclminer.miners.MinerInitException;

public final class JCLMiner implements Runnable {

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
	
	private final KristAddress host;
	private final List<Miner> miners;
	
	public JCLMiner(KristAddress host) {
		this.host = host;
		miners = new ArrayList<Miner>();
	}
	
	/**
	 * Initialize CL stuff
	 */
	private void initMiners() {
		// get best device
		CLDevice best = JavaCL.getBestDevice();
		if (isDeviceCompatible(best)) {
			try {
				miners.add(MinerFactory.createMiner(best, this));
				System.out.println("Device " + best.getName().trim() + " is ready for mining.");
			} catch (MinerInitException e) {
				System.err.println(String.format("Failed to create miner for device %s", best.getName().trim()));
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		log("Starting JCLMiner...");
		initMiners();
		for (Miner m : miners) {
			try {
				m.start(KristAPI.getWork(), KristAPI.getBlock());
			} catch (SyncnodeDownException e) {
				e.printStackTrace();
			}
		}
		long hr;
		while (true) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
			hr = 0;
			for (Miner m : miners) {
				hr += m.getAverageHashRate();
				System.out.format("Average rate %s\n", Utils.formatSpeed(hr));
			}
		}
	}

	public KristAddress getHost() {
		return host;
	}
	
}
