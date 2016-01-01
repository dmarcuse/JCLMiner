package me.apemanzilla.jclminer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import me.apemanzilla.kristapi.types.KristAddress;

public final class CommandLineLauncher {
	
	public static void main(String[] args) throws ParseException {
		// Parse CLI options
		Options options = new Options();
		options.addOption("h",	"host",		true,	"The address to send rewards to.");
		options.addOption("?",	"help",		false,	"Show usage.");
		CommandLine cmd = new DefaultParser().parse(options, args);
		if (cmd.hasOption('?') || !cmd.hasOption('h')) {
			new HelpFormatter().printHelp("JOCLMiner -h [address]", options);
			System.exit(1);
		}
		// Run miner
		JCLMiner m = new JCLMiner(KristAddress.auto(cmd.getOptionValue('h')));
		m.run();
	}

}
