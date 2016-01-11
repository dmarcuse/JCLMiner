package me.apemanzilla.jclminer.miners;

import com.nativelibs4java.opencl.CLDevice;

import me.apemanzilla.jclminer.JCLMiner;

public class MinerFactory {
	private MinerFactory(){}
	public static Miner createMiner(CLDevice dev, JCLMiner host) throws MinerInitException {
		return createMiner(dev, host.getAddress().getAddress(), host.generatePrefix());
	}
	
	public static Miner createMiner(CLDevice dev, String address, String prefix) throws MinerInitException {
		if (dev.getType().contains(CLDevice.Type.GPU)) {
			return new GPUMiner(dev, address, prefix);
		}
		throw new MinerInitException("Device cannot be used.");
	}
}
