package me.apemanzilla.jclminer.miners;

import com.nativelibs4java.opencl.CLDevice;

public class MinerFactory {
	private MinerFactory(){}
	public static Miner createMiner(CLDevice dev) throws MinerInitException {
		throw new MinerInitException("Device cannot be used.");
	}
}
