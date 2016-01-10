package me.apemanzilla.jclminer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.nativelibs4java.opencl.CLDevice;

import me.apemanzilla.jclminer.miners.Miner;
import me.apemanzilla.jclminer.miners.MinerFactory;
import me.apemanzilla.jclminer.miners.MinerInitException;

public class SystemProfiler implements Runnable {

	private final int testTime;
	
	public SystemProfiler(int testTime) {
		this.testTime = testTime * 1000;
	}
	
	@Override
	public void run() {
		System.out.println("Starting system profiler...");
		List<CLDevice> devices = JCLMiner.listCompatibleDevices();
		HashMap<Integer, Integer> perSignature = new HashMap<Integer, Integer>();
		for (CLDevice dev : devices) {
			if (!perSignature.containsKey(dev.createSignature().hashCode()))
			try {
				Miner m = MinerFactory.createMiner(dev, "k5ztameslf");
				System.out.format("Profiling device %s\n", dev.getName().trim());
				long[] results = new long[16];
				for (int i = 0; i < results.length; i++) {
					System.out.format("Test #%d > ", i + 1);
					m.setWorkSize((int)Math.pow(2, i+8));
					m.start(0, "abcdefghijkl");
					try {
						Thread.sleep(testTime);
					} catch (InterruptedException e) {}
					results[i] = m.getAverageHashRate();
					System.out.format("%s\n", MinerUtils.formatSpeed(m.getAverageHashRate()));
					m.stop();
					if (i > 1) {
						if (results[i] < results[i-1]) {
							System.out.println("Performance decrease - stopping tests.");
							break;
						}
					}
				}
				int best = 0;
				for (int i = 0; i < results.length; i++) {
					if (results[i] > best) {
						best = (int) Math.pow(2, i+7);
					}
				}
				perSignature.put(dev.createSignature().hashCode(), best);
			} catch (MinerInitException e) {
				System.err.format("Failed to create miner for device %s\n", dev.getName().trim());
				e.printStackTrace();
			}
		}
		Iterator<Integer> it = perSignature.keySet().iterator();
		String output = "-a -d ";
		while (it.hasNext()) {
			int i = it.next();
			output += String.format("%d:%d;", i, perSignature.get(i));
		}
		System.out.format("Profiling complete! Use the following launch arguments for optimal performance:\n%s\n",output);
	}

}
