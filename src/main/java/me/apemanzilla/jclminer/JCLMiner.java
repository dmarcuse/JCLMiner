package me.apemanzilla.jclminer;

import java.util.ArrayList;
import java.util.List;

import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.JavaCL;

import me.apemanzilla.kristapi.types.KristAddress;
import me.apemanzilla.jclminer.miners.Miner;
import me.apemanzilla.jclminer.miners.MinerFactory;
import me.apemanzilla.jclminer.miners.MinerInitException;

public final class JCLMiner implements Runnable {

	public static final String[] cl_build_options = {};
	
	public static boolean isDeviceCompatible(CLDevice dev) {
		return dev.getType().contains(CLDevice.Type.GPU);
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
	private void init() {
		// get best device
		CLDevice best = JavaCL.getBestDevice();
		if (isDeviceCompatible(best)) {
			try {
				miners.add(MinerFactory.createMiner(best));
			} catch (MinerInitException e) {
				System.err.println(String.format("Failed to create miner for device %s", best.getName().trim()));
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		log("Starting JCLMiner...");
		init();
	}
	
}
