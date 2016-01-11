package me.apemanzilla.jclminer.miners;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLException;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;

import me.apemanzilla.jclminer.JCLMiner;
import me.apemanzilla.jclminer.MinerUtils;

public class GPUMiner extends Miner implements Runnable {
	
	private final CLDevice dev;
	private final CLContext ctx;
	private final CLQueue queue;
	private final CLProgram program;
	private final CLKernel kernel;
	
	private String lastBlock;
	private String address;
	private long work;
	
	private Thread controller;
	
	private int range = 1024 * 4;
	
	private String solution;
	
	GPUMiner(CLDevice dev, String address, String prefix) throws MinerInitException {
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
		setPrefix(prefix);
	}
	
	@Override
	public void start(long work, String lastBlock) {
		hashes = 0;
		solution = null;
		this.lastBlock = lastBlock;
		this.work = work;
		if (controller != null) {
			controller.interrupt();
		}
		controller = new Thread(this);
		startTime = System.currentTimeMillis();
		controller.start();
	}

	@Override
	public void stop() {
		if (controller == null && controller.isAlive()) {
			while(controller.isAlive()) {
				controller.interrupt();
			}
		}
	}

	@Override
	public boolean hasSolution() {
		return solution != null;
	}

	@Override
	public String getSolution() {
		if (solution != null) {
			String t = solution;
			solution = null;
			return t;
		}
		return null;
	}

	@Override
	public long getAverageHashRate() {
		if (hashes > 0) {
			return hashes / ((System.currentTimeMillis() - startTime) / 1000);
		} else {
			return 0;
		}
	}

	@Override
	public String getDeviceName() {
		return dev.getName().trim();
	}
	
	public boolean isOpenCLMiner() {
		return true;
	}

	public void setWorkSize(int size) {
		this.range = size;
	}
	
	@Override
	public void run() {
		// TODO make this a lot better
		int range = this.range;
		long base = 0;
		Pointer<Byte> addressPtr = Pointer.allocateBytes(10).order(ctx.getByteOrder());
		byte[] addressBytes = MinerUtils.getBytes(address);
		for (int i = 0; i < 10; i ++) {
			addressPtr.set(i, addressBytes[i]);
		}
		Pointer<Byte> blockPtr = Pointer.allocateBytes(12).order(ctx.getByteOrder());
		byte[] blockBytes = MinerUtils.getBytes(lastBlock);
		for (int i = 0; i < 12; i++) {
			blockPtr.set(i, blockBytes[i]);
		}
		Pointer<Byte> prefixPtr = Pointer.allocateBytes(2).order(ctx.getByteOrder());
		byte[] prefixBytes = MinerUtils.getBytes(prefix);
		for (int i = 0; i < 2; i++) {
			prefixPtr.set(i, prefixBytes[i]);
		}
		CLBuffer<Byte>
				addressBuf = ctx.createByteBuffer(Usage.Input, addressPtr),
				blockBuf = ctx.createByteBuffer(Usage.Input, blockPtr),
				prefixBuf = ctx.createByteBuffer(Usage.Input, prefixPtr);
		CLBuffer<Byte> outputBuf = ctx.createByteBuffer(Usage.Output, 34);
		while (!Thread.interrupted()) {
			kernel.setArgs(addressBuf, blockBuf, prefixBuf, base, work, outputBuf);
			try {
				CLEvent evt = kernel.enqueueNDRange(queue, new int[] {range});
				Pointer<Byte> outputPtr = outputBuf.read(queue, evt);
				evt.release();
				if (outputPtr.get(0) != 0 && !Thread.interrupted()) {
					// try solution
					byte[] output = new byte[34];
					for (int i = 0; i < 34; i++) {
						output[i] = outputPtr.get(i);
					}
					long score = MinerUtils.hashToLong(MinerUtils.digest(output));
					if (score <= work) {
						byte[] c = new byte[12];
						System.arraycopy(output, 22, c, 0, 12);
						solution = new String(MinerUtils.getChars(c));
						setChanged();
						notifyObservers();
						outputPtr.release();
						break;
					}
				}
				outputPtr.release();
				base += range;
				hashes += range;
			} catch (CLException e) {
				controller.interrupt();
			}
		}
		addressBuf.release();
		blockBuf.release();
		prefixBuf.release();
		outputBuf.release();
	}
}
