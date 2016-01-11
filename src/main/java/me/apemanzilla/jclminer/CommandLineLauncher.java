package me.apemanzilla.jclminer;

import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nativelibs4java.opencl.CLDevice;

public final class CommandLineLauncher {
	
	public static void main(String[] args) throws ParseException {
		// Parse CLI options
		Options options = new Options();
		options.addOption("h",	"host",			true,	"The address to send rewards to.");
		options.addOption("l",	"list-devices",	false,	"Show a list of compatible devices and their signatures.");
		options.addOption("p",	"profile",		false,	"Profile each device to find the optimal work range.");
		options.addOption("b",	"best-device",	false,	"Mine on whichever device is deemed 'best.' Default option.");
		options.addOption("a",	"all-devices",	false,	"Mine on all compatible hardware devices.");
		options.addOption("d",	"devices",		true,	"Specifies what work size to use for given device types. Run a profile with -p to get best setting.");
		options.addOption("?",	"help",			false,	"Show usage.");
		CommandLine cmd = new DefaultParser().parse(options, args);
		if (cmd.hasOption('l')) {
			// list devices
			System.out.println("Compatible OpenCL devices:");
			for (Entry<Integer, CLDevice> entry : JCLMiner.deviceIds.entrySet()) {
				int id = entry.getKey();
				CLDevice dev = entry.getValue();
				System.out.format("DEVICE: %s SIGNATURE: %d ID: %d\n", dev.getName().trim(), dev.createSignature().hashCode(), id);
			}
			System.exit(1);
		}
		if (cmd.hasOption('p')) {
			SystemProfiler p = new SystemProfiler(5);
			p.run();
			System.exit(-1);
		}
		if (cmd.hasOption('?') || !cmd.hasOption('h')) {
			// show help
			HelpFormatter hf = new HelpFormatter();
			hf.setOptionComparator(null);
			hf.printHelp("JCLMiner -h [address]", options);
			System.exit(1);
		}
		// Run miner
		if (cmd.getOptionValue('h').length() != 10) {
			System.err.println("Invalid Krist address!");
			System.exit(1);
		}
		JCLMinerConfig config = new JCLMinerConfig(cmd.getOptionValue('h'));
		if (cmd.hasOption('a')) {
			config.selectAllDevices();
		}
		if (cmd.hasOption('d')) {
			String[] inputs = cmd.getOptionValue('d').split(";");
			for (String input : inputs) {
				int sig = Integer.parseInt(input.split(":")[0]);
				int size = Integer.parseInt(input.split(":")[1]);
				config.getWorkSizes().put(sig, size);
			}
		}
		new JCLMiner(config).run();
	}

}
