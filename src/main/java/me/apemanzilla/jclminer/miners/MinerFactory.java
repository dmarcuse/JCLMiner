package me.apemanzilla.jclminer.miners;

import com.nativelibs4java.opencl.CLDevice;

import me.apemanzilla.jclminer.JCLMiner;

public class MinerFactory {
	private MinerFactory(){}
	public static Miner createMiner(CLDevice dev, JCLMiner host) throws MinerInitException {
		if (dev.getType().contains(CLDevice.Type.GPU)) {
			return new GPUMiner(dev, host.getHost().getAddress());
		}
		throw new MinerInitException("Device cannot be used.");
	}
}
