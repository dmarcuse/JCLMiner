package me.apemanzilla.jclminer.miners;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.sci.skristminer.util.Utils;

import me.apemanzilla.jclminer.JCLMiner;

public class GPUMiner extends Miner implements Runnable {
	
	private final JCLMiner host;
	
	private final CLDevice dev;
	private final CLContext ctx;
	private final CLQueue queue;
	private final CLProgram program;
	private final CLKernel kernel;
	
	private String lastBlock;
	private String address;
	private String prefix = JCLMiner.generateID();
	private long work;
	
	private Thread controller;

	private Object hash_ct_lock = new Object();
	
	private long timeStarted = 0;
	private long hashes = 0;
	
	GPUMiner(CLDevice dev, JCLMiner host) throws MinerInitException {
		this.host = host;
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
		address = host.getHost().getAddress();
	}
	
	@Override
	public void start(long work, String lastBlock) {
		hashes = 0;
		this.lastBlock = lastBlock;
		this.work = work;
		if (controller != null) {
			controller.interrupt();
		}
		controller = new Thread(this);
		timeStarted = System.currentTimeMillis();
		controller.start();
	}

	@Override
	public void stop() {
		if (controller == null) {
			controller.interrupt();
		}
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
		return hashes / ((System.currentTimeMillis() - timeStarted) / 1000);
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
		// TODO make this a lot better
		int range = 65536;
		String start = lastBlock + address + prefix;
		long base = 0;
		byte[] bytes = Utils.getBytes(start);
		Pointer<Byte> startPtr = Pointer.allocateBytes(24);
		for (int i = 0; i < 24; i++) {
			startPtr.set(i,bytes[i]);
		}
		CLBuffer<Byte> startBuf = ctx.createByteBuffer(Usage.Input, startPtr);
		CLBuffer<Integer> outBuf = ctx.createIntBuffer(Usage.Output, 1);
		while (!Thread.interrupted()) {
			kernel.setArgs(startBuf, base, work, outBuf);
			CLEvent evt = kernel.enqueueNDRange(queue, new int[] {range});
			base += range;
			hashes += range;
			Pointer<Integer> outPtr = outBuf.read(queue, evt);
			if (outPtr.get(0) == 1) {
				System.out.println("SOLVED");
				break;
			}
		}
		
	}
	
}
