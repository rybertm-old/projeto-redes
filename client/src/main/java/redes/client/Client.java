package redes.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Robert Broketa
 * @author Hiago Rios
 */
public class Client {

    static final Options options = createCLIOptions();
    static final CommandLineParser parser = new DefaultParser();
    static final char DEFAULT_VALUE_SEPARATOR = ' ';

    public static void main(String[] args) {
        Client client = new Client();
        if (args.length < 1) {
            printHelp();
            System.exit(1);
        }
        client.run(args);
    }

    /**
     * Parses the program arguments into options and runs the respective
     * funcionalities
     * 
     * @param args The arguments to be parsed into commands and options
     */
    private void run(String[] args) {
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("t")) {
                ClientConnector connector = new ClientConnector();
                String imagePath = line.getOptionValue("t");
                Integer port = null;
                String portString = line.getOptionValue("p");
                if (portString != null) {
                    port = Integer.parseInt(portString);
                }
                connector.testImage(imagePath, port);
            }
        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            printHelp();
        }
    }

    /**
     * Creates the accepted CLI options
     * 
     * @return The CLI {@code Options} object
     */
    private static Options createCLIOptions() {
        Options options = new Options();

        Option testImage = Option.builder("t").hasArg().argName("image-path")
                .desc("Checks the specified image for a hidden message and prints it").longOpt("test-image")
                .valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option createImage = Option.builder("c").hasArg().argName("image-path")
                .desc("Creates a copy of the specified image containing the given message").longOpt("create-image")
                .valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option port = Option.builder("p").hasArg().argName("port")
                .desc("Defines the port used to connect with the test server").longOpt("port")
                .valueSeparator(DEFAULT_VALUE_SEPARATOR).build();

        options.addOption(testImage);
        options.addOption(createImage);
        options.addOption(port);
        return options;
    }

    /**
     * Prints the command-line interface help message
     */
    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("client", options);
    }
}