package me.apemanzilla.jclminer.miners;

import com.nativelibs4java.opencl.CLDevice;

public class MinerFactory {
	private MinerFactory(){}
	public static Miner createMiner(CLDevice dev, String host) throws MinerInitException {
		if (dev.getType().contains(CLDevice.Type.GPU)) {
			return new GPUMiner(dev, host);
		}
		throw new MinerInitException("Device cannot be used.");
	}
}
