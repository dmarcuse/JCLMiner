package me.apemanzilla.jclminer.miners;

import com.nativelibs4java.opencl.CLDevice;

public class MinerFactory {
	private MinerFactory(){}
	public static Miner createMiner(CLDevice dev) throws MinerInitException {
		if (dev.getType().contains(CLDevice.Type.GPU))
			return new GPUMiner(dev);
		// Cannot create miner
		return null;
	}
}
