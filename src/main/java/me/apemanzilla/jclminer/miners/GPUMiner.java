package me.apemanzilla.jclminer.miners;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;

import me.apemanzilla.jclminer.JCLMiner;

public class GPUMiner extends Miner implements Runnable {
	
	private final CLDevice dev;
	private final CLContext ctx;
	private final CLQueue queue;
	private final CLProgram program;
	private final CLKernel kernel;
	
	private String prefix = JCLMiner.generateID();
	
	private Thread controller;

	GPUMiner(CLDevice dev, JCLMiner host) throws MinerInitException {
		this.dev = dev;
		this.ctx = dev.getPlatform().createContext(null, new CLDevice[] {dev});
		this.queue = ctx.createDefaultQueue();
		
		// load code
		String hashCode = CLCodeLoader.loadCode("/sha256.cl");
		String minerCode = CLCodeLoader.loadCode("/krist_miner.cl");
		if (hashCode == null || minerCode == null) {
			throw new MinerInitException("Error loading OpenCL code.");
		}
		program = ctx.createProgram(hashCode, minerCode);
		for (String opt : JCLMiner.cl_build_options) {
			program.addBuildOption(opt);
		}
		kernel = program.createKernel("krist_miner_basic");
	}
	
	@Override
	public void start(long work, String block, String prefix) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasSolution() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSolution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getAverageHashRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getRecentHashRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDeviceName() {
		return dev.getName().trim();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
