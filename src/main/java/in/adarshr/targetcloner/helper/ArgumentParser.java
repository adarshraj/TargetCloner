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
    private boolean isCompare = false;

    public ArgumentParser(String[] arguments) {
        parseArguments(arguments);
    }

    /**
     * Get the options
     *
     * @return Options
     */
    private static Options getOptions() {
        Options options = new Options();
        options.addOption("c", "compare", false, "Compare the files");
        options.addOption("h", "help", false, "Help");
        return options;
    }

    /**
     * Parse the input arguments
     *
     * @param args arguments
     */
    public void parseArguments(String[] args) {
        Options options = getOptions();

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = commandLineParser.parse(options, args);
            if (cmd.hasOption("c")) {
                setCompare();
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

    /**
     * Get compare
     *
     * @return boolean
     */
    public boolean isCompare() {
        return isCompare;
    }

    /**
     * Set compare
     */
    private void setCompare() {
        this.isCompare = true;
    }

    /**
     * Create help
     *
     * @param options Options
     */
    public void createHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("TargetCloner", options);
    }


}
