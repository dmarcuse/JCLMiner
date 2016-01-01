package me.apemanzilla.jclminer.miners;

import java.io.IOException;
import java.io.InputStream;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

import me.apemanzilla.jclminer.JCLMiner;

final class GPUMiner extends Miner implements Runnable {

	private static String loadCL() throws MinerInitException {
		try {
			InputStream is = JCLMiner.class.getResourceAsStream("/gpu_miner.cl");
			if (is == null) throw new MinerInitException("Missing CL code");
			if (is.available() == 0) throw new MinerInitException("Empty CL code file");
			byte[] data = new byte[is.available()];
			is.read(data, 0, is.available());
			return new String(data).trim();
		} catch (IOException e) {
			throw new MinerInitException("Unknown IO error");
		}
	}
	
	private final CLDevice dev;
	private final String code;
	
	private long hashcount = 0;
	
	private String block;
	private String prefix;
	private long work;
	
	private Thread worker;
	
	GPUMiner(CLDevice dev) throws MinerInitException {
		this.dev = dev;
		this.code = loadCL();
	}
	
	@Override
	public void start(long work, String block, String prefix) {
		if (worker == null) {
			worker = new Thread(this);
			this.block = block;
			this.prefix = prefix;
			this.work = work;
			worker.start();
		}
	}

	@Override
	public void stop() {
		worker.interrupt();
		// wait for worker to stop
		try {
			worker.join();
			worker = null;
		} catch (InterruptedException e) {}
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
		// initialization
		CLContext context = JavaCL.createContext(null, dev);
		CLQueue queue = context.createDefaultQueue();
		CLProgram program = context.createProgram(code);
		CLKernel kernel = program.createKernel("mine");
		
		boolean run = true;
		int step = 1; // increment with each loop, multiply by cu to get base nonce
		int cu = dev.getMaxComputeUnits();
		
		// allocate arguments
		//  char[] prefix
		//  int start
		//  long work
		//  int[] output
		CLBuffer<Byte> prefix_buf = context.createByteBuffer(Usage.Input, block.length() + prefix.length());
		CLBuffer<Integer> output_buf = context.createIntBuffer(Usage.Output, cu);
		int start;
		
		// run
		while (run && !worker.isInterrupted()) {
			start = cu * step;
			// TODO bind args and queue kernel
			step++;
		}
	}

}
