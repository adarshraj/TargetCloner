package in.adarshr.targetcloner.helper;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ArgumentParser class
 * Process the input arguments
 */
public class ArgumentParser {
    Logger logger = LoggerFactory.getLogger(ArgumentParser.class);
    public ArgumentParser(String[] arguments) {
        parseArguments(arguments);
    }

    private boolean isCompare;


    /**
     * Parse the input arguments
     * @param args arguments
     */
    public void parseArguments(String[] args) {
        Options options = getOptions();

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = commandLineParser.parse(options, args);
            if (cmd.hasOption("c")) {
                setCompare(true);
            }
            if (cmd.hasOption("h")) {
                createHelp(options);
                System.exit(0);
            }
        } catch (ParseException e) {
            logger.error("Invalid option");
            createHelp(options);
            System.exit(0);
        }
    }

    private void setCompare(boolean compare) {
        this.isCompare = compare;
    }

    public boolean isCompare() {
        return isCompare;
    }

    /**
     * Get the options
     * @return Options
     */
    private static Options getOptions() {
        Options options = new Options();
        options.addOption("c", "compare", false, "Compare the files");
        options.addOption("h", "help", false, "Help");
        return options;
    }

    public void createHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("TargetCloner", options);
    }


}
