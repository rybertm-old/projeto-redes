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
 * 
 * @apiNote
 * Error codes
 * <ul>
 *  <li>1 - Wrong usage / Insufficient arguments</li>
 *  <li>2 - Unsupported arguments</li>
 * </ul>
 */
public class Client {

    static final Options options = createCLIOptions();
    static final CommandLineParser parser = new DefaultParser();
    static final char DEFAULT_VALUE_SEPARATOR = ' ';
    static final String CAESAR_ENCRYPTION = "CAESAR";

    public static void main(String[] args) {
        Client client = new Client();
        if (args.length < 1) {
            printHelp();
            System.exit(1);
        }
        client.parse(args);
    }

    /**
     * Parses the program arguments into options and runs the respective
     * funcionalities
     * 
     * @param args The arguments to be parsed into commands and options
     */
    private void parse(String[] args) {
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h")) {
                printHelp();
                System.exit(0);
            }
            if (line.hasOption("t")) {
                runImageTest(line);
            }
            if (line.hasOption("c")) {
                // TODO create image
            }
        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            printHelp();
        }
    }

    private static void runImageTest(CommandLine line) {
        ClientConnector connector = new ClientConnector();
        String imagePath = line.getOptionValue("t");
        Integer port = null;
        String portString = line.getOptionValue("p");
        if (portString != null) {
            port = Integer.parseInt(portString);
        }
        String encryption = line.getOptionValue("e");
        if (encryption == null) {
            encryption = CAESAR_ENCRYPTION;
        } else {
            validateEncryption(encryption);
        }
        connector.testImage(imagePath, port, encryption);
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
        Option encryption = Option.builder("e").hasArg().argName("encryption")
                .desc("Defines the encryption method to be used when testing/creating the image. Defaults to CAESAR")
                .longOpt("encryption").valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option port = Option.builder("p").hasArg().argName("port")
                .desc("Defines the port used to connect with the test server. Defaults to 6868").longOpt("port")
                .valueSeparator(DEFAULT_VALUE_SEPARATOR).build();
        Option help = Option.builder("h").desc("Prints this message").longOpt("help").build();

        options.addOption(testImage);
        options.addOption(createImage);
        options.addOption(encryption);
        options.addOption(port);
        options.addOption(help);
        return options;
    }

    /**
     * Prints the command-line interface help message
     */
    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("client", options);
    }

    /**
     * Checks if the encryption is supported by the encoder
     * 
     * @param encryption {@code String} to be tested
     * @return True if the encryption is supported, false otherwise
     */
    private static void validateEncryption(String encryption) {
        if (!encryption.equalsIgnoreCase(CAESAR_ENCRYPTION)) {
            System.err.println("Encryption not supported: " + encryption);
            System.exit(2);
        }
    }
}