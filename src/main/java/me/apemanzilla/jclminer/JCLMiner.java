package me.apemanzilla.jclminer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.JavaCL;

import me.apemanzilla.jclminer.miners.Miner;
import me.apemanzilla.jclminer.miners.MinerFactory;
import me.apemanzilla.jclminer.miners.MinerInitException;
import me.apemanzilla.kristapi.exceptions.SyncnodeDownException;
import me.apemanzilla.kristapi.types.KristAddress;

public final class JCLMiner extends Observable implements Runnable, Observer {

	public final static Map<Integer, CLDevice> deviceIds = new HashMap<Integer, CLDevice>();
	
	static {
		int id = 0;
		CLPlatform platforms[] = JavaCL.listPlatforms();
		for (CLPlatform plat : platforms) {
			CLDevice devices[] = plat.listAllDevices(false);
			for (CLDevice dev : devices) {
				if (isDeviceCompatible(dev)) {
					deviceIds.put(id, dev);
					id++;
				}
			}
		}
	}
	
	public static final String[] cl_build_options = { "-cl-single-precision-constant", "-cl-denorms-are-zero",
			"-cl-strict-aliasing", "-cl-mad-enable", "-cl-no-signed-zeros", "-cl-unsafe-math-optimizations",
			"-cl-finite-math-only" };

	public static boolean isDeviceCompatible(CLDevice dev) {
		return dev.getType().contains(CLDevice.Type.GPU);
	}
	
	@Deprecated
	public static List<CLDevice> listCompatibleDevices() {
		List<CLDevice> out = new ArrayList<CLDevice>();
		CLPlatform platforms[] = JavaCL.listPlatforms();
		for (CLPlatform plat : platforms) {
			CLDevice[] devices = plat.listAllDevices(false);
			for (CLDevice dev : devices) {
				if (isDeviceCompatible(dev)) {
					out.add(dev);
				}
			}
		}
		return out;
	}
	
	private final JCLMinerConfig config;

	private List<CLDevice> devices;

	private final List<Miner> miners = new ArrayList<Miner>();

	private final KristMiningState state;

	private boolean run = true;

	private long timeStarted = 0;

	// never hurts to be hopeful
	private long blocksSolved = 0;

	private State currentState = State.NOT_RUN;

	/**
	 * Creates a new JCLMiner object. You should create
	 * and modify a JCLMinerConfig option first and pass it
	 * to this constructor.
	 * @param config
	 */
	public JCLMiner(JCLMinerConfig config) {
		this.config = config;
		this.state = new KristMiningState(config.getDaemonRefreshRate());
	}
	
	/**
	 * This method has been replaced by {@link #JCLMiner(JCLMinerConfig)}.
	 * It has been grandfathered
	 * in to work with the changes
	 * but should not be used for new code.
	 * @param address
	 */
	@Deprecated
	public JCLMiner(KristAddress address) {
		this.config = new JCLMinerConfig(address.getAddress());
		this.state = new KristMiningState(2000);
	}
	
	/**
	 * Initialize CL stuff
	 */
	private void initMiners() {
		if (devices == null) {
			// get best device
			CLDevice best = JavaCL.getBestDevice();
			if (isDeviceCompatible(best)) {
				try {
					Miner m = MinerFactory.createMiner(best, this);
					if (config.getWorkSizes().containsKey(best.createSignature().hashCode())) {
						m.setWorkSize(config.getWorkSizes().get(best.createSignature().hashCode()));
						System.out.format("Work size manually overridden for device %s.\n", best.getName());
					}
					miners.add(m);
					System.out.format("Device %s ready.\n", best.getName().trim());
				} catch (MinerInitException e) {
					System.err.println(String.format("Failed to create miner for device %s\n", best.getName().trim()));
					e.printStackTrace();
				}
			}
		} else {
			// use specified devices
			for (CLDevice dev : devices) {
				if (isDeviceCompatible(dev)) {
					try {
						Miner m = MinerFactory.createMiner(dev, this);
						if (config.getWorkSizes().containsKey(dev.createSignature().hashCode())) {
							m.setWorkSize(config.getWorkSizes().get(dev.createSignature().hashCode()));
							System.out.format("Work size manually overridden for device %s.\n", dev.getName());
						}
						miners.add(m);
						System.out.format("Device %s ready.\n", dev.getName().trim());
					} catch (MinerInitException e) {
						System.err.format("Failed to create miner for device %s\n", dev.getName().trim());
						e.printStackTrace();
					}
				} else {
					System.out.format("Specified device %s is incompatible\n", dev.getName().trim());
				}
			}
		}
	}

	// Should NOT be called externally! Only for use by JCLMiner!
	private void startMiners(long work, String block) {
		for (Miner m : miners) {
			m.start(work, block);
		}
	}

	// Should NOT be called externally! Only for use by JCLMiner!
	private void stopMiners() {
		for (Miner m : miners) {
			m.stop();
		}
	}

	private String getSolution() {
		for (Miner m : miners) {
			if (m.hasSolution())
				return m.getSolution();
		}
		return null;
	}

	private void resetPrefixes() {
		for (Miner m : miners) {
			m.setPrefix(generatePrefix());
		}
	}

	private String generateStatusMessage(String block, long hashrate, long blocks, double blocksPerMinute) {
		String hashrateStr = StringUtils.center(MinerUtils.formatSpeed(hashrate), 15);
		String blockStr = StringUtils.center(String.format("%d blocks", blocks), 15);
		String bpmStr = StringUtils.center(String.format("%.2f blocks/minute", blocksPerMinute), 25);
		return hashrateStr + "|" + blockStr + "|" + bpmStr;
	}

	private void updateState(State newState) {
		currentState = newState;
		setChanged();
		notifyObservers();
	}

	public long getAverageHashrate() {
		long hr = 0;
		for (Miner m : miners) {
			hr += m.getAverageHashRate();
		}
		return hr;
	}

	public long getRecentHashrate() {
		long hr = 0;
		for (Miner m : miners) {
			hr += m.getRecentHashRate();
		}
		return hr;
	}

	public long secondsSinceStarted() {
		return (System.currentTimeMillis() - timeStarted) / 1000;
	}

	public double getBlocksPerMinute() {
		double m = (double) secondsSinceStarted() / 60;
		if (m != 0 && blocksSolved != 0) {
			return (double) blocksSolved / m;
		}
		return 0;
	}

	public State getState() {
		return currentState;
	}

	/**
	 * To stop running the miner, interrupt the thread.
	 */
	@Override
	public void run() {

		updateState(State.INITIALIZING);

		System.out.println("Starting JCLMiner...");
		initMiners();
		// prepare krist state daemon
		Thread stateDaemon = new Thread(state);
		stateDaemon.setDaemon(true);
		stateDaemon.start();
		// wait for daemon to retrieve first block
		while (state.getBlock() == null || state.getWork() == 0) {
		}
		// add self as observer
		state.addObserver(this);
		for (Miner m : miners) {
			m.addObserver(this);
		}
		// main loop
		timeStarted = System.currentTimeMillis();
		outerLoop: while (!Thread.interrupted()) {

			updateState(State.STARTING_MINERS);

			System.out.format("Mining for block: '%s' Work: %d\n", state.getBlock(), state.getWork());
			startMiners(state.getWork(), state.getBlock());
			run = true;

			updateState(State.MINING);

			innerLoop: while (run) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break outerLoop;
				}
				if (!run)
					break innerLoop;
				System.out.println(generateStatusMessage(state.getBlock(), getAverageHashrate(), blocksSolved,
						getBlocksPerMinute()));
			}

			updateState(State.STOPPING_MINERS);

			stopMiners();
			String sol = getSolution();
			if (sol != null) {

				updateState(State.SUBMITTING);

				System.out.format("Submitting solution '%s' > ", sol);
				try {
					String encoded = URLEncoder.encode(sol, "ISO-8859-1");
					boolean success = false;
					while (!success) {
						String oldBlock = state.getBlock();
						if (config.getAddress().submitBlock(encoded)) {
							// make sure it was actually accepted...
							long t = System.currentTimeMillis();
							while (state.getBlock().equals(oldBlock)) {
								if (System.currentTimeMillis() - t > 10 * 1000) {
									break;
								}
							}
							if (!state.getBlock().equals(oldBlock)) {
								success = true;
								blocksSolved++;
								System.out.println("Success!");
							}
						} else {
							System.out.println("Rejected.");
							break;
						}
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (SyncnodeDownException e) {
					e.printStackTrace();
				}
			}
			resetPrefixes();
		}
	}

	public KristAddress getAddress() {
		return config.getAddress();
	}

	@Override
	public void update(Observable o, Object arg) {
		run = false;
	}

	public String generatePrefix() {
		return String.format("%02x", new Random().nextInt(256));
	}

	public void useDevices(List<CLDevice> devices) {
		this.devices = devices;
	}
	
	/**
	 * This method should not be used. You should configure worksizes in the
	 * JCLMinerConfig object used when constructing JCLMiner instead. It has been
	 * grandfathered in to work with the changes, but should not be used with the new code.
	 * @param deviceWorkSizes
	 */
	@Deprecated
	public void setWorkSizes(Map<Integer, Integer> deviceWorkSizes) {
		config.setWorkSizes(deviceWorkSizes);
	}

	public enum State {
		/**
		 * Object initialized, but not run.
		 */
		NOT_RUN,
		/**
		 * When initializing miners
		 */
		INITIALIZING,
		/**
		 * Starting miners before mining
		 */
		STARTING_MINERS,
		/**
		 * Miners running
		 */
		MINING,
		/**
		 * Stopping miners for a block change/submission, etc
		 */
		STOPPING_MINERS,
		/**
		 * Submitting a solution
		 */
		SUBMITTING,
		/**
		 * Error occurred
		 */
		ERROR
	}

}
