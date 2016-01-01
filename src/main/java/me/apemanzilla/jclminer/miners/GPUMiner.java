package me.apemanzilla.jclminer.miners;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.JavaCL;

import me.apemanzilla.jclminer.JCLMiner;
import me.apemanzilla.jclminer.MinerUtils;

final class GPUMiner extends Miner {
	private static String loadCode() throws MinerInitException {
		try {
			InputStream is = JCLMiner.class.getResourceAsStream("/gpu_miner.cl");
			if (is == null) throw new MinerInitException("Failed to read CL code - file missing");
			int pos = 0;
			byte[] data = new byte[is.available()];
			while (pos < data.length) {
				int d = is.read();
				if (d < 1)
					break;
				data[pos] = (byte) d;
				pos++;
			}
			if (pos == 0) {
				throw new MinerInitException("Failed to read CL code - file is empty");
			}
			return new String(data).trim();
		} catch (IOException e) {
			e.printStackTrace();
			throw new MinerInitException("Failed to read CL code - unknown error");
		}
	}
	
	private final CLDevice gpu;
	private final CLContext context;
	private final CLProgram program;
	
	GPUMiner(CLDevice gpu) throws MinerInitException {
		String src = loadCode();
		this.gpu = gpu;
		this.context = JavaCL.createContext(null, gpu);
		this.program = context.createProgram(src);
	}
	
	@Override
	public void start(long work, String block, String prefix, String suffix) {
		

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

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
		return gpu.getName();
	}

	@Override
	public int getTotalComputeUnits() {
		return gpu.getMaxComputeUnits();
	}

	@Override
	public void release() {
		
	}
	
	private final class Controller extends Observable implements Runnable {

		private final CLProgram program;
		private final long work;
		private final byte[] lastBlock;
		private final byte[] prefix;
		private final byte[] suffix;
		//private final CLKernel kernel;
		
		public String solution;
		
		public volatile boolean run;
		
		public Controller(CLProgram program, long work, String lastBlock, String prefix, String suffix) {
			run = true;
			this.program = program;
			this.work = work;
			this.lastBlock = MinerUtils.getBytes(lastBlock);
			this.prefix = MinerUtils.getBytes(prefix);
			this.suffix = MinerUtils.getBytes(suffix);
			
			// Create pointers
			Pointer<Byte>
				lastblockPtr = Pointer.allocateBytes(this.lastBlock.length).order(context.getByteOrder()),
				prefixPtr = Pointer.allocateBytes(this.prefix.length).order(context.getByteOrder()),
				suffixPtr = Pointer.allocateBytes(this.suffix.length).order(context.getByteOrder());
			
			// TODO
			
			// Create arguments and kernel
			
			
		}
		
		@Override
		public void run() {
			while (run) {
				
			}
			
		}
		
	}
}
