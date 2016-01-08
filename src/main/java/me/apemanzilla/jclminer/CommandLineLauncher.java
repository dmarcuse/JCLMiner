package me.apemanzilla.jclminer;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nativelibs4java.opencl.CLDevice;

import me.apemanzilla.kristapi.types.KristAddress;

public final class CommandLineLauncher {
	
	public static void main(String[] args) throws ParseException {
		// Parse CLI options
		Options options = new Options();
		options.addOption("h",	"host",			true,	"The address to send rewards to.");
		options.addOption("l",	"list-devices",	false,	"Show a list of compatible devices and their IDs");
		options.addOption("b",	"best-device",	false,	"Mine on whichever device is deemed 'best.' Default option.");
		options.addOption("a",	"all-devices",	false,	"Mine on all compatible hardware devices.");
		options.addOption("d",	"devices",		true,	"Specifies which devices, and optionally, what range, to mine on. By default, will attempt to choose optimal settings.");
		options.addOption("?",	"help",			false,	"Show usage.");
		CommandLine cmd = new DefaultParser().parse(options, args);
		if (cmd.hasOption('l')) {
			// list devices
			List<CLDevice> devices = JCLMiner.listCompatibleDevices();
			System.out.println("Compatible OpenCL devices:");
			for (CLDevice dev : devices) {
				System.out.format("Name: %s ID: %s\n", dev.getName().trim(), dev.createSignature().hashCode());
			}
			System.exit(1);
		}
		if (cmd.hasOption('?') || !cmd.hasOption('h')) {
			// show help
			HelpFormatter hf = new HelpFormatter();
			hf.setOptionComparator(null);
			hf.printHelp("JCLMiner -h [address]", options);
			System.exit(1);
		}
		if (cmd.hasOption('d')) {
			// NYI
			System.out.println("Not yet implemented.");
			System.exit(1);
		}
		// Run miner
		JCLMiner m = new JCLMiner(KristAddress.auto(cmd.getOptionValue('h')));
		if (cmd.hasOption('a')) {
			m.useDevices(JCLMiner.listCompatibleDevices());
		}
		m.run();
	}

}
