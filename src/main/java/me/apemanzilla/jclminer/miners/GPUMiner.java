package me.apemanzilla.jclminer.miners;

import java.io.UnsupportedEncodingException;

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
	
	private long timeStarted = 0;
	private long hashes = 0;
	
	private String solution;
	
	private final Object hash_count_lock = new Object();
	
	GPUMiner(CLDevice dev, String address) throws MinerInitException {
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
		this.address = address;
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
		return solution != null;
	}

	@Override
	public String getSolution() {
		return solution != null ? solution : null;
	}

	@Override
	public long getAverageHashRate() {
		synchronized (hash_count_lock) {
			if (hashes > 0) {
				return hashes / ((System.currentTimeMillis() - timeStarted) / 1000);
			} else {
				return 0;
			}
		}
	}

	@Override
	public long getRecentHashRate() {
		return 0;
	}

	@Override
	public String getDeviceName() {
		return dev.getName().trim();
	}

	@Override
	public void run() {
		// TODO make this a lot better
		int range = 90000;
		String start = address + lastBlock + prefix;
		long base = 0;
		byte[] bytes = Utils.getBytes(start);
		Pointer<Byte> startPtr = Pointer.allocateBytes(24);
		for (int i = 0; i < 24; i++) {
			startPtr.set(i,bytes[i]);
		}
		CLBuffer<Byte> startBuf = ctx.createByteBuffer(Usage.Input, startPtr);
		CLBuffer<Long> outBuf = ctx.createLongBuffer(Usage.Output, 12);
		while (!Thread.interrupted()) {
			kernel.setArgs(startBuf, base, work, outBuf);
			CLEvent evt = kernel.enqueueNDRange(queue, new int[] {range});
			Pointer<Long> outPtr = outBuf.read(queue, evt);
			if (outPtr.get(0) != 0) {
				// assemble solution
				long nonce = outPtr.get(0);
				System.out.println(nonce);
				char hex_nonce[] = new char[10];
				for (int i = 0; i < 10; i++) {
					hex_nonce[i] = (char) ((nonce >> ((i) * 5) & 31) + 48);
				}
				solution = prefix + new String(hex_nonce);
				setChanged();
				notifyObservers();
				break;
			}
			base += range;
			synchronized (hash_count_lock) {
				hashes += range;
			}
		}
		
	}
}
