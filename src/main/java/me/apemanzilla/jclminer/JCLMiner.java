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
		CLPlatform[] platforms = JavaCL.listPlatforms();
		for (CLPlatform plat : platforms) {
			CLDevice[] devices = plat.listAllDevices(true);
			for (CLDevice dev : devices) {
				try {
					Miner m = MinerFactory.createMiner(dev);
					if (m != null) {
						miners.add(m);
						log("Initialized device '" + dev.getName().trim() + "'");
					}
				} catch (MinerInitException e) {
					log("Failed to initialize device '" + dev.getName().trim() + "': " + e.getMessage());
				}
			}
		}
	}
	
	@Override
	public void run() {
		log("Starting JCLMiner...");
		init();
	}
	
}
